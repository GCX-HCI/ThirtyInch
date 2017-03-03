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

import net.grandcentrix.thirtyinch.BindViewInterceptor;
import net.grandcentrix.thirtyinch.PresenterAction;
import net.grandcentrix.thirtyinch.Removable;
import net.grandcentrix.thirtyinch.TiConfiguration;
import net.grandcentrix.thirtyinch.TiDialogFragment;
import net.grandcentrix.thirtyinch.TiFragment;
import net.grandcentrix.thirtyinch.TiLog;
import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.TiView;
import net.grandcentrix.thirtyinch.callonmainthread.CallOnMainThreadInterceptor;
import net.grandcentrix.thirtyinch.distinctuntilchanged.DistinctUntilChangedInterceptor;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This delegate allows sharing the fragment code between the {@link TiFragment},
 * {@link TiDialogFragment}, {@code TiFragmentPlugin} and {@code TiDialogFragmentPlugin}.
 * <p>
 * It also allows 3rd party developers do add this delegate to other Fragments using composition.
 */
public class TiFragmentDelegate<P extends TiPresenter<V>, V extends TiView>
        implements InterceptableViewBinder<V>, PresenterAccessor<P, V> {

    private static final String SAVED_STATE_PRESENTER_ID = "presenter_id";

    /**
     * enables debug logging during development
     */
    private static final boolean ENABLE_DEBUG_LOGGING = false;

    private volatile boolean mActivityStarted = false;

    private final TiLoggingTagProvider mLogTag;

    private LinkedBlockingQueue<PresenterAction<P>> mPostponedPresenterActions =
            new LinkedBlockingQueue<>();

    private P mPresenter;

    private String mPresenterId;

    private final TiPresenterProvider<P> mPresenterProvider;

    private final DelegatedTiFragment mTiFragment;

    private Removable mUiThreadBinderRemovable;

    private final PresenterViewBinder<V> mViewBinder;

    private final TiViewProvider<V> mViewProvider;

    public TiFragmentDelegate(final DelegatedTiFragment fragmentProvider,
            final TiViewProvider<V> viewProvider,
            final TiPresenterProvider<P> presenterProvider,
            final TiLoggingTagProvider logTag) {
        mTiFragment = fragmentProvider;
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

    @SuppressWarnings("UnusedParameters")
    public void onCreateView_beforeSuper(final LayoutInflater inflater,
            @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState) {
        mViewBinder.invalidateView();
    }

    @SuppressWarnings("unchecked")
    public void onCreate_afterSuper(final Bundle savedInstanceState) {
        if (mPresenter == null && savedInstanceState != null) {
            // recover with Savior
            // this should always work.
            final String recoveredPresenterId = savedInstanceState
                    .getString(SAVED_STATE_PRESENTER_ID);
            if (recoveredPresenterId != null) {
                TiLog.v(mLogTag.getLoggingTag(),
                        "try to recover Presenter with id: " + recoveredPresenterId);
                mPresenter = (P) PresenterSavior.INSTANCE.recover(recoveredPresenterId);
                if (mPresenter != null) {
                    // save recovered presenter with new id. No other instance of this activity,
                    // holding the presenter before, is now able to remove the reference to
                    // this presenter from the savior
                    PresenterSavior.INSTANCE.free(recoveredPresenterId);
                    mPresenterId = PresenterSavior.INSTANCE.safe(mPresenter);
                }
                TiLog.v(mLogTag.getLoggingTag(), "recovered Presenter " + mPresenter);
            }
        }

        if (mPresenter == null) {
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

        if (config.shouldRetainPresenter()) {
            mTiFragment.setFragmentRetainInstance(true);
        }

        //noinspection unchecked
        final UiThreadExecutorAutoBinder uiThreadAutoBinder =
                new UiThreadExecutorAutoBinder(mPresenter, mTiFragment.getUiThreadExecutor());

        // bind ui thread to presenter when view is attached
        mUiThreadBinderRemovable = mPresenter.addLifecycleObserver(uiThreadAutoBinder);

        // send all queued actions since the Activity got created to the presenter after it got
        // created/reattached.
        sendPostponedActionsToPresenter(getPresenter());
    }

    public void onDestroyView_beforeSuper() {
        mPresenter.detachView();
    }

    public void onDestroy_afterSuper() {
        //FIXME handle attach/detach state

        // unregister observer and don't leak it
        if (mUiThreadBinderRemovable != null) {
            mUiThreadBinderRemovable.remove();
            mUiThreadBinderRemovable = null;
        }

        logState();

        boolean destroyPresenter = false;
        if (mTiFragment.isHostingActivityFinishing()) {
            // Probably a backpress and not a configuration change
            // Activity will not be recreated and finally destroyed, also destroyed the presenter
            destroyPresenter = true;
            TiLog.v(mLogTag.getLoggingTag(),
                    "Activity is finishing, destroying presenter " + mPresenter);
        }

        final TiConfiguration config = mPresenter.getConfig();
        if (!destroyPresenter &&
                !config.shouldRetainPresenter()) {
            // configuration says the presenter should not be retained, a new presenter instance
            // will be created and the current presenter should be destroyed
            destroyPresenter = true;
            TiLog.v(mLogTag.getLoggingTag(),
                    "presenter configured as not retaining, destroying " + mPresenter);
        }

        if (!destroyPresenter &&
                !config.useStaticSaviorToRetain() && mTiFragment.isDontKeepActivitiesEnabled()) {
            // configuration says the PresenterSavior should not be used. Retaining the presenter
            // relays on the Activity nonConfigurationInstance which is always null when
            // "don't keep activities" is enabled.
            // a new presenter instance will be created and the current presenter should be destroyed
            destroyPresenter = true;
            TiLog.v(mLogTag.getLoggingTag(),
                    "the PresenterSavior is disabled and \"don\'t keep activities\" is "
                            + "activated. The presenter can't be retained. Destroying "
                            + mPresenter);
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

        if (isUiPossible()) {
            mTiFragment.getUiThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    if (isUiPossible() && mActivityStarted) {
                        mViewBinder.bindView(mPresenter, mViewProvider);
                    }
                }
            });
        }
    }

    public void onStop_beforeSuper() {
        mActivityStarted = false;
        mPresenter.detachView();
    }

    @Override
    public void sendToPresenter(final PresenterAction<P> action) {
        final P presenter = getPresenter();
        if (presenter != null) {
            action.call(presenter);
        } else {
            mPostponedPresenterActions.add(action);
        }
    }

    @Override
    public String toString() {
        String presenter = getPresenter() == null ? "null" :
                getPresenter().getClass().getSimpleName()
                        + "@" + Integer.toHexString(getPresenter().hashCode());

        return getClass().getSimpleName()
                + ":" + TiFragmentDelegate.class.getSimpleName()
                + "@" + Integer.toHexString(hashCode())
                + "{presenter=" + presenter + "}";
    }

    private boolean isUiPossible() {
        return mTiFragment.isFragmentAdded() && !mTiFragment.isFragmentDetached();
    }

    private void logState() {
        if (ENABLE_DEBUG_LOGGING) {
            TiLog.v(mLogTag.getLoggingTag(), "isChangingConfigurations = "
                    + mTiFragment.isHostingActivityChangingConfigurations());
            TiLog.v(mLogTag.getLoggingTag(),
                    "isHostingActivityFinishing = " + mTiFragment.isHostingActivityFinishing());
            TiLog.v(mLogTag.getLoggingTag(),
                    "isAdded = " + mTiFragment.isFragmentAdded());
            TiLog.v(mLogTag.getLoggingTag(),
                    "isDetached = " + mTiFragment.isFragmentDetached());
            TiLog.v(mLogTag.getLoggingTag(),
                    "isDontKeepActivitiesEnabled = " + mTiFragment.isDontKeepActivitiesEnabled());

            final TiConfiguration config = mPresenter.getConfig();
            TiLog.v(mLogTag.getLoggingTag(),
                    "shouldRetain = " + config.shouldRetainPresenter());
            TiLog.v(mLogTag.getLoggingTag(),
                    "useStaticSavior = " + config.useStaticSaviorToRetain());
        }
    }

    /**
     * Executes all postponed presenter actions
     *
     * @param presenter where the actions will be sent to
     */
    private void sendPostponedActionsToPresenter(final P presenter) {
        while (!mPostponedPresenterActions.isEmpty()) {
            mPostponedPresenterActions.poll().call(presenter);
        }
    }
}