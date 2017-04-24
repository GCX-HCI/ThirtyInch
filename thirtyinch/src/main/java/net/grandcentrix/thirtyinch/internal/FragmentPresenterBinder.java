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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.BackstackReader;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;

import java.util.List;
import java.util.concurrent.Executor;

public class FragmentPresenterBinder<P extends TiPresenter<V>, V extends TiView>
        extends FragmentManager.FragmentLifecycleCallbacks
        implements DelegatedTiFragment, TiViewProvider<V>, TiLoggingTagProvider,
        TiPresenterBinder<P, V> {

    private final String TAG;

    private final FragmentActivity mActivity;

    private final TiFragmentDelegate<P, V> mDelegate;

    private final Fragment mFragment;

    private final UiThreadExecutor mUiThreadExecutor = new UiThreadExecutor();

    public FragmentPresenterBinder(final Fragment fragment,
            final Bundle savedInstanceState,
            final TiPresenterProvider<P> presenterProvider,
            final TiViewProvider<V> viewProvider) {
        mFragment = fragment;
        mActivity = fragment.getActivity();

        TAG = mActivity.getClass().getSimpleName()
                + "@" + Integer.toHexString(mActivity.hashCode());

        mDelegate = new TiFragmentDelegate<>(this, viewProvider != null ? viewProvider : this,
                presenterProvider, this, PresenterSavior.getInstance());

        // There is no onFragmentPreCreated(Bundle) callback to execute code as code in
        // super.onCreate(Bundle) would be executed. Therefore this class must be initialized in
        // Fragment#onCreate(Bundle) where this method will be called directly.
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
    public boolean isFragmentAdded() {
        return mFragment.isAdded();
    }

    @Override
    public boolean isFragmentDetached() {
        return mFragment.isDetached();
    }

    @Override
    public boolean isFragmentInBackstack() {
        return BackstackReader.isInBackStack(mFragment);
    }

    @Override
    public boolean isFragmentRemoving() {
        return mFragment.isRemoving();
    }

    @Override
    public boolean isHostingActivityChangingConfigurations() {
        return mActivity.isChangingConfigurations();
    }

    @Override
    public boolean isHostingActivityFinishing() {
        return mActivity.isFinishing();
    }

    @Override
    public void onFragmentDestroyed(final FragmentManager fm, final Fragment f) {
        super.onFragmentDestroyed(fm, f);
        if (f == mFragment) {
            mDelegate.onDestroy_afterSuper();
        }
    }

    @Override
    public void onFragmentSaveInstanceState(final FragmentManager fm, final Fragment f,
            final Bundle outState) {
        super.onFragmentSaveInstanceState(fm, f, outState);
        if (f == mFragment) {
            mDelegate.onSaveInstanceState_afterSuper(outState);
        }
    }

    @Override
    public void onFragmentStarted(final FragmentManager fm, final Fragment f) {
        super.onFragmentStarted(fm, f);
        if (f == mFragment) {
            mDelegate.onStart_afterSuper();
        }
    }

    @Override
    public void onFragmentStopped(final FragmentManager fm, final Fragment f) {
        super.onFragmentStopped(fm, f);
        if (f == mFragment) {
            mDelegate.onStop_beforeSuper();
        }
    }

    @Override
    public void onFragmentViewCreated(final FragmentManager fm, final Fragment f, final View v,
            final Bundle savedInstanceState) {
        super.onFragmentViewCreated(fm, f, v, savedInstanceState);
        if (f == mFragment) {
            mDelegate.onCreateView_beforeSuper(null, null, savedInstanceState);
        }
    }

    @Override
    public void onFragmentViewDestroyed(final FragmentManager fm, final Fragment f) {
        super.onFragmentViewDestroyed(fm, f);
        if (f == mFragment) {
            mDelegate.onDestroyView_beforeSuper();
        }
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public V provideView() {
        final Class<?> foundViewInterface = AnnotationUtil
                .getInterfaceOfClassExtendingGivenInterface(mFragment.getClass(), TiView.class);

        if (foundViewInterface == null) {
            throw new IllegalArgumentException(
                    "This Fragment doesn't implement a TiView interface. "
                            + "This is the default behaviour. Override provideView() to explicitly change this.");
        } else {
            if (foundViewInterface.getSimpleName().equals("TiView")) {
                throw new IllegalArgumentException(
                        "extending TiView doesn't make sense, it's an empty interface."
                                + " This is the default behaviour. Override provideView() to explicitly change this.");
            } else {
                // assume that the activity itself is the view and implements the TiView interface
                return (V) mFragment;
            }
        }
    }
}
