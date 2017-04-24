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
import net.grandcentrix.thirtyinch.Removable;
import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.TiPresenterBinder;
import net.grandcentrix.thirtyinch.TiView;
import net.grandcentrix.thirtyinch.util.AnnotationUtil;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;
import java.util.concurrent.Executor;

public class ActivityPresenterBinder<P extends TiPresenter<V>, V extends TiView>
        implements DelegatedTiActivity<P>, TiViewProvider<V>, TiLoggingTagProvider,
        Application.ActivityLifecycleCallbacks, TiPresenterBinder<P, V> {

    private final String TAG;

    private final Activity mActivity;

    private final TiActivityDelegate<P, V> mDelegate;

    private final UiThreadExecutor mUiThreadExecutor = new UiThreadExecutor();

    public ActivityPresenterBinder(@NonNull final Activity activity,
            final Bundle savedInstanceState,
            final TiPresenterProvider<P> presenterProvider,
            final TiViewProvider<V> viewProvider) {
        mActivity = activity;

        TAG = mActivity.getClass().getSimpleName()
                + "@" + Integer.toHexString(mActivity.hashCode());

        mDelegate = new TiActivityDelegate<>(this, viewProvider != null ? viewProvider : this,
                presenterProvider, this, PresenterSavior.getInstance());

        // There is no onPreActivityCreate(Bundle) callback to execute code as code in
        // super.onCreate(Bundle) would be executed. Therefore this class must be initialized in
        // Activity#onCreate(Bundle) where this method will be called directly.
        // The presenter is available immediately with #getPresenter()
        mDelegate.onCreate_afterSuper(savedInstanceState);
    }

    @NonNull
    @Override
    public Removable addBindViewInterceptor(@NonNull final BindViewInterceptor interceptor) {
        return mDelegate.addBindViewInterceptor(interceptor);
    }

    @Override
    public Activity getHostingActivity() {
        return mActivity;
    }

    @Nullable
    @Override
    public V getInterceptedViewOf(@NonNull final BindViewInterceptor interceptor) {
        return mDelegate.getInterceptedViewOf(interceptor);
    }

    @NonNull
    @Override
    public List<BindViewInterceptor> getInterceptors(
            @NonNull final Filter<BindViewInterceptor> predicate) {
        return mDelegate.getInterceptors(predicate);
    }

    @Override
    public String getLoggingTag() {
        return TAG;
    }

    @Override
    public P getPresenter() {
        return mDelegate.getPresenter();
    }

    @Override
    public Executor getUiThreadExecutor() {
        return mUiThreadExecutor;
    }

    @Override
    public void invalidateView() {
        mDelegate.invalidateView();
    }

    @Override
    public boolean isActivityChangingConfigurations() {
        return mActivity.isChangingConfigurations();
    }

    @Override
    public boolean isActivityFinishing() {
        return mActivity.isFinishing();
    }

    @Override
    public void onActivityCreated(final Activity activity,
            final Bundle savedInstanceState) {
        // already called in constructor
    }

    @Override
    public void onActivityDestroyed(final Activity activity) {
        if (activity == mActivity) {
            mDelegate.onDestroy_afterSuper();

            // always expect call on attachPresenter in Activity constructor
            mActivity.getApplication().unregisterActivityLifecycleCallbacks(this);
        }
    }

    @Override
    public void onActivityPaused(final Activity activity) {
        // noop
    }

    @Override
    public void onActivityResumed(final Activity activity) {
        // noop
    }

    @Override
    public void onActivitySaveInstanceState(final Activity activity,
            final Bundle outState) {
        if (activity == mActivity) {
            mDelegate.onSaveInstanceState_afterSuper(outState);
        }
    }

    @Override
    public void onActivityStarted(final Activity activity) {
        if (activity == mActivity) {
            mDelegate.onStart_afterSuper();
        }
    }

    @Override
    public void onActivityStopped(final Activity activity) {
        if (activity == mActivity) {
            mDelegate.onStop_beforeSuper();
            mDelegate.onStop_afterSuper();
        }
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public V provideView() {
        final Class<?> foundViewInterface = AnnotationUtil
                .getInterfaceOfClassExtendingGivenInterface(mActivity.getClass(), TiView.class);

        if (foundViewInterface == null) {
            throw new IllegalArgumentException(
                    "This Activity doesn't implement a TiView interface. "
                            + "This is the default behaviour. Override provideView() to explicitly change this.");
        } else {
            if (foundViewInterface.getSimpleName().equals("TiView")) {
                throw new IllegalArgumentException(
                        "extending TiView doesn't make sense, it's an empty interface."
                                + " This is the default behaviour. Override provideView() to explicitly change this.");
            } else {
                // assume that the activity itself is the view and implements the TiView interface
                return (V) mActivity;
            }
        }
    }
}
