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

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.concurrent.Executor;

import static org.mockito.Mockito.mock;


/**
 * mock implementation of a {@link android.support.v4.app.Fragment} with all relevant lifecycle
 * methods instructing the {@link TiFragmentDelegate} for testing
 */
public class TestTiFragment
        implements DelegatedTiFragment, TiViewProvider<TiView>,
        PresenterAccessor<TiPresenter<TiView>, TiView> {

    public static class Builder {

        private HostingActivity mHostingActivity = new HostingActivity();

        private boolean mIsDontKeepActivitiesEnabled = false;

        private TiPresenter<TiView> mPresenter;

        private TiPresenterProvider<TiPresenter<TiView>> mPresenterProvider;

        private TiPresenterSavior mSavior = new PresenterSavior();

        public TestTiFragment build() {
            TiPresenterProvider<TiPresenter<TiView>> presenterProvider = mPresenterProvider;
            if (presenterProvider == null) {
                presenterProvider = new TiPresenterProvider<TiPresenter<TiView>>() {
                    @NonNull
                    @Override
                    public TiPresenter<TiView> providePresenter() {
                        return mPresenter;
                    }
                };
            }

            return new TestTiFragment(presenterProvider, mIsDontKeepActivitiesEnabled, mSavior,
                    mHostingActivity);
        }

        public Builder setDontKeepActivitiesEnabled(final boolean enabled) {
            mIsDontKeepActivitiesEnabled = enabled;
            return this;
        }

        public Builder setHostingActivity(final HostingActivity hostingActivity) {
            mHostingActivity = hostingActivity;
            return this;
        }

        public Builder setPresenter(TiPresenter<TiView> presenter) {
            mPresenter = presenter;
            return this;
        }

        public Builder setPresenterProvider(
                TiPresenterProvider<TiPresenter<TiView>> provider) {
            mPresenterProvider = provider;
            return this;
        }

        public Builder setSavior(final TiPresenterSavior savior) {
            mSavior = savior;
            return this;
        }
    }

    private boolean mAdded = false;

    private final TiFragmentDelegate mDelegate;

    private boolean mDetached = false;

    private final HostingActivity mHostingActivity;

    private boolean mInBackstack;

    private boolean mIsDontKeepActivitiesEnabled;

    private boolean mRemoving;

    private TestTiFragment(final TiPresenterProvider<TiPresenter<TiView>> presenterProvider,
            final boolean isDontKeepActivitiesEnabled,
            final TiPresenterSavior savior,
            final HostingActivity hostingActivity) {

        mDelegate = new TiFragmentDelegate<>(this, this, presenterProvider,
                new TiLoggingTagProvider() {
                    @Override
                    public String getLoggingTag() {
                        return "Test";
                    }
                }, savior);

        mIsDontKeepActivitiesEnabled = isDontKeepActivitiesEnabled;
        mHostingActivity = hostingActivity;
    }

    @Override
    public Activity getHostingActivity() {
        return mHostingActivity.getMockActivityInstance();
    }

    @Override
    public TiPresenter<TiView> getPresenter() {
        return mDelegate.getPresenter();
    }

    @Override
    public Executor getUiThreadExecutor() {
        return new Executor() {
            @Override
            public void execute(@NonNull final Runnable action) {
                action.run();
            }
        };
    }

    @Override
    public boolean isFragmentAdded() {
        return mAdded;
    }

    @Override
    public boolean isFragmentDetached() {
        return mDetached;
    }

    @Override
    public boolean isFragmentRemoving() {
        return mRemoving;
    }

    @Override
    public boolean isHostingActivityChangingConfigurations() {
        return mHostingActivity.isChangingConfiguration();
    }

    @Override
    public boolean isHostingActivityFinishing() {
        return mHostingActivity.isFinishing();
    }

    @Override
    public boolean isInBackstack() {
        return mInBackstack;
    }

    public void onCreate(final Bundle saveInstanceState) {
        mDelegate.onCreate_afterSuper(saveInstanceState);
    }

    public void onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        mDelegate.onCreateView_beforeSuper(inflater, container, savedInstanceState);
    }

    public void onDestroy() {
        mDelegate.onDestroy_afterSuper();
    }

    public void onDestroyView() {
        mDelegate.onDestroyView_beforeSuper();
    }

    public void onSaveInstanceState(final Bundle outState) {
        mDelegate.onSaveInstanceState_afterSuper(outState);
    }

    public void onStart() {
        mDelegate.onStart_afterSuper();
    }

    public void onStop() {
        mDelegate.onStop_beforeSuper();
    }

    @NonNull
    @Override
    public TiView provideView() {
        return mock(TiView.class);
    }

    public void setAdded(final boolean added) {
        mAdded = added;
    }

    public void setDetached(final boolean detached) {
        mDetached = detached;
    }

    public void setInBackstack(final boolean inBackstack) {
        mInBackstack = inBackstack;
    }

    public void setRemoving(final boolean removing) {
        mRemoving = removing;
    }
}
