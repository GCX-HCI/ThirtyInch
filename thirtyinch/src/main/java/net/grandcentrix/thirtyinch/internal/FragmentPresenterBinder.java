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


import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.TiView;
import net.grandcentrix.thirtyinch.util.AnnotationUtil;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.BackstackReader;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;

import java.util.concurrent.Executor;

public class FragmentPresenterBinder<P extends TiPresenter<V>, V extends TiView>
        extends FragmentManager.FragmentLifecycleCallbacks
        implements DelegatedTiFragment, TiViewProvider<V>, TiLoggingTagProvider,
        PresenterAccessor<P, V> {

    private final String TAG;

    private final FragmentActivity mActivity;

    private final TiFragmentDelegate<P, V> mDelegate;

    private final Fragment mFragment;

    private final UiThreadExecutor mUiThreadExecutor = new UiThreadExecutor();

    // TODO create override with TiViewProvider
    public FragmentPresenterBinder(final Fragment fragment,
            final Bundle savedInstanceState,
            final TiPresenterProvider<P> provider) {
        mFragment = fragment;
        mActivity = fragment.getActivity();

        TAG = mActivity.getClass().getSimpleName()
                + "@" + Integer.toHexString(mActivity.hashCode());

        mDelegate = new TiFragmentDelegate<>(this, this, provider, this,
                PresenterSavior.getInstance());

        // sadly there is no FragmentLifecycleCallback which executes before Fragment#onCreate
        // containing the saved instance state. So this FragmentPresenterBinder must be initialized
        // in Fragment#onCreate(Bundle)
        mDelegate.onCreate_afterSuper(savedInstanceState);
    }

    @Override
    public Activity getHostingActivity() {
        return mActivity;
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
        mDelegate.onDestroy_afterSuper();
    }

    @Override
    public void onFragmentSaveInstanceState(final FragmentManager fm, final Fragment f,
            final Bundle outState) {
        super.onFragmentSaveInstanceState(fm, f, outState);
        mDelegate.onSaveInstanceState_afterSuper(outState);
    }

    @Override
    public void onFragmentStarted(final FragmentManager fm, final Fragment f) {
        super.onFragmentStarted(fm, f);
        mDelegate.onStart_afterSuper();
    }

    @Override
    public void onFragmentStopped(final FragmentManager fm, final Fragment f) {
        mDelegate.onStop_beforeSuper();
        super.onFragmentStopped(fm, f);
    }

    @Override
    public void onFragmentViewCreated(final FragmentManager fm, final Fragment f, final View v,
            final Bundle savedInstanceState) {
        mDelegate.onCreateView_beforeSuper(null, null, savedInstanceState);
        super.onFragmentViewCreated(fm, f, v, savedInstanceState);
    }

    @Override
    public void onFragmentViewDestroyed(final FragmentManager fm, final Fragment f) {
        mDelegate.onDestroyView_beforeSuper();
        super.onFragmentViewDestroyed(fm, f);
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
