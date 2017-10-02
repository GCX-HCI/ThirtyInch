/*
 * Copyright (C) 2017 grandcentrix GmbH
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.grandcentrix.thirtyinch.internal;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import java.util.List;
import net.grandcentrix.thirtyinch.BindViewInterceptor;
import net.grandcentrix.thirtyinch.Removable;
import net.grandcentrix.thirtyinch.TiActivity;
import net.grandcentrix.thirtyinch.TiConfiguration;
import net.grandcentrix.thirtyinch.TiLog;
import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.TiView;
import net.grandcentrix.thirtyinch.callonmainthread.CallOnMainThreadInterceptor;
import net.grandcentrix.thirtyinch.distinctuntilchanged.DistinctUntilChangedInterceptor;

/**
 * This delegate allows sharing the activity code between the {@link TiActivity} and {@code
 * TiActivityPlugin}. The {@link TiActivity} could be easily implemented by adding the {@code
 * TiActivityPlugin} but the resulting dependency to CompositeAndroid is not a good way since it is
 * currently in an early stage.
 * <p>
 * It also allows 3rd party developers do add this delegate to other Activities using composition.
 */
public class TiActivityDelegate<P extends TiPresenter<V>, V extends TiView>
        implements InterceptableViewBinder<V>, PresenterAccessor<P, V> {

    @VisibleForTesting
    static final String SAVED_STATE_PRESENTER_ID = "presenter_id";

    /**
     * flag indicating the started state of the Activity between {@link Activity#onStart()} and
     * {@link Activity#onStop()}.
     */
    private volatile boolean mActivityStarted = false;

    private final TiLoggingTagProvider mLogTag;

    /**
     * The presenter to which this activity will be attached as view when in the right state.
     */
    private P mPresenter;

    /**
     * The id of the presenter this view got attached to. Will be stored in the savedInstanceState
     * to find the same presenter after the Activity got recreated.
     */
    private String mPresenterId;

    private final TiPresenterProvider<P> mPresenterProvider;

    private final TiPresenterSavior mSavior;

    private final DelegatedTiActivity mTiActivity;

    private Removable mUiThreadBinderRemovable;

    private final PresenterViewBinder<V> mViewBinder;

    private final TiViewProvider<V> mViewProvider;

    public TiActivityDelegate(final DelegatedTiActivity activityProvider,
            final TiViewProvider<V> viewProvider,
            final TiPresenterProvider<P> presenterProvider,
            final TiLoggingTagProvider logTag,
            final TiPresenterSavior savior) {
        mTiActivity = activityProvider;
        mViewProvider = viewProvider;
        mPresenterProvider = presenterProvider;
        mLogTag = logTag;
        mViewBinder = new PresenterViewBinder<>(logTag);
        mSavior = savior;
    }

    @NonNull
    @Override
    public Removable addBindViewInterceptor(@NonNull final BindViewInterceptor interceptor) {
        return mViewBinder.addBindViewInterceptor(interceptor);
    }

    @Nullable
    @Override
    public V getInterceptedViewOf(@NonNull final BindViewInterceptor interceptor) {
        return mViewBinder.getInterceptedViewOf(interceptor);
    }

    @NonNull
    @Override
    public List<BindViewInterceptor> getInterceptors(
            @NonNull final Filter<BindViewInterceptor> predicate) {
        return mViewBinder.getInterceptors(predicate);
    }

    @Override
    public P getPresenter() {
        return mPresenter;
    }

    /**
     * Invalidates the cache of the latest bound view. Forces the next binding of the view to run
     * through all the interceptors (again).
     */
    @Override
    public void invalidateView() {
        mViewBinder.invalidateView();
    }

    public void onConfigurationChanged_afterSuper(final Configuration newConfig) {
        // make sure the new view will be wrapped again
        mViewBinder.invalidateView();
    }

    @SuppressWarnings("unchecked")
    public void onCreate_afterSuper(final Bundle savedInstanceState) {

        // try to recover with the PresenterSavior
        if (savedInstanceState != null) {
            final String recoveredPresenterId =
                    savedInstanceState.getString(SAVED_STATE_PRESENTER_ID);

            if (mPresenter == null) {
                if (recoveredPresenterId != null) {
                    // recover with Savior
                    // this should always work.
                    TiLog.v(mLogTag.getLoggingTag(),
                            "try to recover Presenter with id: " + recoveredPresenterId);
                    mPresenter = (P) mSavior
                            .recover(recoveredPresenterId, mTiActivity.getHostingContainer());
                    TiLog.v(mLogTag.getLoggingTag(),
                            "recovered Presenter from savior " + mPresenter);
                } else {
                    TiLog.v(mLogTag.getLoggingTag(), "could not recover a Presenter from savior");
                }
            }

            if (mPresenter == null) {
                TiLog.i(mLogTag.getLoggingTag(), "could not recover the Presenter "
                        + "although it's not the first start of the Activity. This is normal when "
                        + "configured as .setRetainPresenterEnabled(false).");
            } else {
                // save recovered presenter with new id. No other instance of this activity,
                // holding the presenter before, is now able to remove the reference to
                // this presenter from the savior
                mSavior.free(recoveredPresenterId, mTiActivity.getHostingContainer());
                mPresenterId = mSavior.save(mPresenter, mTiActivity.getHostingContainer());
            }
        }

        if (mPresenter == null) {
            // could not recover, create a new presenter
            mPresenter = mPresenterProvider.providePresenter();
            if (mPresenter.getState() != TiPresenter.State.INITIALIZED) {
                throw new IllegalStateException("Presenter not in initialized state. "
                        + "Current state is " + mPresenter.getState() + ". "
                        + "Presenter provided with #providePresenter() cannot be reused. "
                        + "Always return a fresh instance!");
            }
            TiLog.v(mLogTag.getLoggingTag(), "created Presenter: " + mPresenter);
            final TiConfiguration config = mPresenter.getConfig();
            if (config.shouldRetainPresenter()) {
                mPresenterId = mSavior.save(mPresenter, mTiActivity.getHostingContainer());
            }
            mPresenter.create();
        }

        final TiConfiguration config = mPresenter.getConfig();
        if (config.isCallOnMainThreadInterceptorEnabled()) {
            addBindViewInterceptor(new CallOnMainThreadInterceptor());
        }

        if (config.isDistinctUntilChangedInterceptorEnabled()) {
            addBindViewInterceptor(new DistinctUntilChangedInterceptor());
        }

        //noinspection unchecked
        final UiThreadExecutorAutoBinder uiThreadAutoBinder =
                new UiThreadExecutorAutoBinder(mPresenter, mTiActivity.getUiThreadExecutor());

        // bind ui thread to presenter when view is attached
        mUiThreadBinderRemovable = mPresenter.addLifecycleObserver(uiThreadAutoBinder);
    }

    public void onDestroy_afterSuper() {

        // unregister observer and don't leak it
        if (mUiThreadBinderRemovable != null) {
            mUiThreadBinderRemovable.remove();
            mUiThreadBinderRemovable = null;
        }

        boolean destroyPresenter = false;
        if (mTiActivity.isActivityFinishing()) {
            destroyPresenter = true;
            TiLog.v(mLogTag.getLoggingTag(),
                    "Activity is finishing, destroying presenter " + mPresenter);
        }

        if (!destroyPresenter &&
                !mPresenter.getConfig().shouldRetainPresenter()) {
            // configuration says the presenter should not be retained, a new presenter instance
            // will be created and the current presenter should be destroyed
            destroyPresenter = true;
            TiLog.v(mLogTag.getLoggingTag(),
                    "presenter configured as not retaining, destroying " + mPresenter);
        }

        if (destroyPresenter) {
            mPresenter.destroy();
            mSavior.free(mPresenterId, mTiActivity.getHostingContainer());
        } else {
            TiLog.v(mLogTag.getLoggingTag(), "not destroying " + mPresenter
                    + " which will be reused by the next Activity instance, recreating...");
        }
    }

    public void onSaveInstanceState_afterSuper(final Bundle outState) {
        outState.putString(SAVED_STATE_PRESENTER_ID, mPresenterId);
    }

    public void onStart_afterSuper() {
        mActivityStarted = true;
        // post to the UI queue to delay bindView until all queued work has finished
        mTiActivity.getUiThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                // check if still started. It happens that onStop got already called, specially
                // when the Activity is not the top Activity and a configuration change happens
                if (mActivityStarted) {
                    mViewBinder.bindView(mPresenter, mViewProvider);
                }
            }
        });
    }

    public void onStop_afterSuper() {
        mPresenter.detachView();
    }

    public void onStop_beforeSuper() {
        mActivityStarted = false;
    }
}
