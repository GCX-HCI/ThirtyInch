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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.concurrent.Executor;

import static org.mockito.Mockito.mock;

public class TiActivityDelegateBuilder {

    private boolean mIsChangingConfigurations = false;

    private boolean mIsDontKeepActivitiesEnabled = false;

    private boolean mIsFinishing = false;

    private TiPresenter<TiView> mPresenter;

    private TiPresenterProvider<TiPresenter<TiView>> mPresenterProvider;

    private TiPresenterProvider<TiPresenter<TiView>> mRetainedPresenterProvider;

    public TiActivityDelegate<TiPresenter<TiView>, TiView> build() {
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

        return new TiActivityDelegate<>(new DelegatedTiActivity<TiPresenter<TiView>>() {
            @Nullable
            @Override
            public TiPresenter<TiView> getRetainedPresenter() {
                if (mRetainedPresenterProvider == null) {
                    return null;
                }
                return mRetainedPresenterProvider.providePresenter();
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
            public boolean isActivityChangingConfigurations() {
                return mIsChangingConfigurations;
            }

            @Override
            public boolean isActivityFinishing() {
                return mIsFinishing;
            }

            @Override
            public boolean isDontKeepActivitiesEnabled() {
                return mIsDontKeepActivitiesEnabled;
            }

        }, new TiViewProvider<TiView>() {
            @NonNull
            @Override
            public TiView provideView() {
                return mock(TiView.class);
            }
        }, presenterProvider, new TiLoggingTagProvider() {
            @Override
            public String getLoggingTag() {
                return "TestLogTag";
            }
        });
    }

    public TiActivityDelegateBuilder setDontKeepActivitiesEnabled(final boolean enabled) {
        mIsDontKeepActivitiesEnabled = enabled;
        return this;
    }

    public TiActivityDelegateBuilder setIsChangingConfigurations(final boolean changing) {
        mIsChangingConfigurations = changing;
        return this;
    }

    public TiActivityDelegateBuilder setIsFinishing(final boolean finishing) {
        mIsFinishing = finishing;
        return this;
    }

    public TiActivityDelegateBuilder setPresenter(TiPresenter<TiView> presenter) {
        mPresenter = presenter;
        return this;
    }

    public TiActivityDelegateBuilder setPresenterProvider(
            TiPresenterProvider<TiPresenter<TiView>> provider) {
        mPresenterProvider = provider;
        return this;
    }

    public TiActivityDelegateBuilder setRetainedPresenterProvider(
            TiPresenterProvider<TiPresenter<TiView>> retainedPresenterProvider) {
        mRetainedPresenterProvider = retainedPresenterProvider;
        return this;
    }
}
