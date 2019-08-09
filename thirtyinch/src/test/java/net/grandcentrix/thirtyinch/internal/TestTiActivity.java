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


import static org.mockito.Mockito.*;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.NonNull;
import java.util.concurrent.Executor;
import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.TiView;

/**
 * mock implementation of a {@link Activity} with all relevant lifecycle methods instructing the
 * {@link TiActivityDelegate} for testing
 */
public class TestTiActivity
        implements DelegatedTiActivity, TiViewProvider<TiView>,
        PresenterAccessor<TiPresenter<TiView>, TiView> {

    public static final class Builder {

        private TiPresenter<TiView> mPresenter;

        private TiPresenterProvider<TiPresenter<TiView>> mPresenterProvider;

        private TiPresenterSavior mSavior = new PresenterSavior();

        public Builder() {
        }

        public TestTiActivity build() {
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
            return new TestTiActivity(presenterProvider, mSavior);
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

    private final TiActivityDelegate<TiPresenter<TiView>, TiView> mDelegate;

    private final HostingActivity mHostingActivity = new HostingActivity();


    private TestTiActivity(final TiPresenterProvider<TiPresenter<TiView>> presenterProvider,
            final TiPresenterSavior savior) {
        mDelegate = new TiActivityDelegate<>(this, this, presenterProvider,
                new TiLoggingTagProvider() {
                    @Override
                    public String getLoggingTag() {
                        return "Test";
                    }
                }, savior);
    }

    @Override
    public Object getHostingContainer() {
        return mHostingActivity.getMockActivityInstance();
    }

    public Activity getMockActivityInstance() {
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
    public boolean isActivityFinishing() {
        return mHostingActivity.isFinishing();
    }

    public void onCreate(final Bundle saveInstanceState) {
        mDelegate.onCreate_afterSuper(saveInstanceState);
    }

    public void onDestroy() {
        mDelegate.onDestroy_afterSuper();
    }

    public void onSaveInstanceState(final Bundle outState) {
        mDelegate.onSaveInstanceState_afterSuper(outState);
    }

    public void onStart() {
        mDelegate.onStart_afterSuper();
    }

    public void onStop() {
        mDelegate.onStop_beforeSuper();
        mDelegate.onStop_afterSuper();
    }

    @NonNull
    @Override
    public TiView provideView() {
        return mock(TiView.class);
    }

    public void setFinishing(final boolean finishing) {
        mHostingActivity.setFinishing(finishing);
    }

}
