/*
 * Copyright (C) 2016 grandcentrix GmbH
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

import net.grandcentrix.thirtyinch.BindViewInterceptor;
import net.grandcentrix.thirtyinch.Removable;
import net.grandcentrix.thirtyinch.TiActivity;
import net.grandcentrix.thirtyinch.TiConfiguration;
import net.grandcentrix.thirtyinch.TiLog;
import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.TiView;
import net.grandcentrix.thirtyinch.callonmainthread.CallOnMainThreadInterceptor;
import net.grandcentrix.thirtyinch.distinctuntilchanged.DistinctUntilChangedInterceptor;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import java.util.List;

/**
 * This delegate allows sharing the activity code between the {@link TiActivity} and {@code
 * TiActivityPlugin}. The {@link TiActivity} could be easily implemented by adding the {@code
 * TiActivityPlugin} but the resulting dependency to CompositeAndroid is not a good way since it is
 * currently in an early stage.
 * <p>
 * It also allows 3rd party developers do add this delegate to other Activities using composition.
 */
public class TiActivityDelegate<P extends TiPresenter<V>, V extends TiView>
        implements InterceptableViewBinder<V> {

    @VisibleForTesting
    static final String SAVED_STATE_PRESENTER_ID = "presenter_id";

    /**
     * flag indicating the started state of the Activity between {@link Activity#onStart()} and
     * {@link Activity#onStop()}.
     */
    private volatile boolean mActivityStarted = false;

    private TiLoggingTagProvider mLogTag;

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

    private final DelegatedTiActivity<P> mTiActivity;

    private final PresenterViewBinder<V> mViewBinder;

    private TiViewProvider<V> mViewProvider;

    public TiActivityDelegate(final DelegatedTiActivity<P> activityProvider,
            final TiViewProvider<V> viewProvider,
            final TiPresenterProvider<P> presenterProvider,
            final TiLoggingTagProvider logTag) {
        mTiActivity = activityProvider;
        mViewProvider = viewProvider;
        mPresenterProvider = presenterProvider;
        mLogTag = logTag;
        mViewBinder = new PresenterViewBinder<>(logTag);
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

    public void onCreate_afterSuper(final Bundle savedInstanceState) {

        // try recover presenter via lastNonConfigurationInstance
        // this works most of the time
        mPresenter = mTiActivity.getRetainedPresenter();
        if (mPresenter == null) {
            TiLog.v(mLogTag.getLoggingTag(),
                    "could not recover a Presenter from getLastNonConfigurationInstance()");
        } else {
            TiLog.v(mLogTag.getLoggingTag(),
                    "recovered Presenter from lastCustomNonConfigurationInstance " + mPresenter);
        }

        // try to recover with the PresenterSavior
        if (savedInstanceState != null) {
            final String recoveredPresenterId = savedInstanceState
                    .getString(SAVED_STATE_PRESENTER_ID);

            if (mPresenter == null) {
                if (recoveredPresenterId != null) {
                    // recover with Savior
                    // this should always work.
                    TiLog.v(mLogTag.getLoggingTag(),
                            "try to recover Presenter with id: " + recoveredPresenterId);
                    //noinspection unchecked
                    mPresenter = (P) PresenterSavior.INSTANCE.recover(recoveredPresenterId);
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
                PresenterSavior.INSTANCE.free(recoveredPresenterId);
                mPresenterId = PresenterSavior.INSTANCE.safe(mPresenter);
            }
        }

        if (mPresenter == null) {
            // could not recover, create a new presenter
            mPresenter = mPresenterProvider.providePresenter();
            TiLog.v(mLogTag.getLoggingTag(), "created Presenter: " + mPresenter);
            final TiConfiguration config = mPresenter.getConfig();
            if (config.shouldRetainPresenter() && config.useStaticSaviorToRetain()) {
                mPresenterId = PresenterSavior.INSTANCE.safe(mPresenter);
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
    }

    public void onDestroy_afterSuper() {
        final TiConfiguration config = mPresenter.getConfig();

        boolean destroyPresenter = false;
        if (mTiActivity.isActivityFinishing()) {
            // Probably a backpress and not a configuration change
            // Activity will not be recreated and finally destroyed, also destroyed the presenter
            destroyPresenter = true;
            TiLog.v(mLogTag.getLoggingTag(),
                    "Activity is finishing, destroying presenter " + mPresenter);
        }

        if (!destroyPresenter &&
                !config.shouldRetainPresenter()) {
            // configuration says the presenter should not be retained, a new presenter instance
            // will be created and the current presenter should be destroyed
            destroyPresenter = true;
            TiLog.v(mLogTag.getLoggingTag(),
                    "presenter configured as not retaining, destroying " + mPresenter);
        }

        if (!destroyPresenter
                && !config.useStaticSaviorToRetain()
                && !mTiActivity.isActivityChangingConfigurations()
                && mTiActivity.isDontKeepActivitiesEnabled()) {
            // configuration says the PresenterSavior should not be used. Retaining the presenter
            // relays on the Activity nonConfigurationInstance which is always null when
            // "don't keep activities" is enabled.
            // a new presenter instance will be created and the current presenter should be destroyed
            destroyPresenter = true;
            TiLog.v(mLogTag.getLoggingTag(),
                    "the PresenterSavior is disabled and \"don\'t keep activities\" is activated. "
                            + "The presenter can't be retained. Destroying " + mPresenter);
        }

        if (destroyPresenter) {
            mPresenter.destroy();
            PresenterSavior.INSTANCE.free(mPresenterId);
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
        mTiActivity.postToMessageQueue(new Runnable() {
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
