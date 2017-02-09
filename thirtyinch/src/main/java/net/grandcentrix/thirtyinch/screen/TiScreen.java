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

package net.grandcentrix.thirtyinch.screen;


import net.grandcentrix.thirtyinch.BindViewInterceptor;
import net.grandcentrix.thirtyinch.Removable;
import net.grandcentrix.thirtyinch.TiActivity;
import net.grandcentrix.thirtyinch.TiConfiguration;
import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.TiView;
import net.grandcentrix.thirtyinch.callonmainthread.CallOnMainThreadInterceptor;
import net.grandcentrix.thirtyinch.distinctuntilchanged.DistinctUntilChangedInterceptor;
import net.grandcentrix.thirtyinch.internal.InterceptableViewBinder;
import net.grandcentrix.thirtyinch.internal.PresenterViewBinder;
import net.grandcentrix.thirtyinch.internal.TiLoggingTagProvider;
import net.grandcentrix.thirtyinch.internal.TiPresenterProvider;
import net.grandcentrix.thirtyinch.internal.TiViewProvider;
import net.grandcentrix.thirtyinch.internal.UiThreadExecutorAutoBinder;
import net.grandcentrix.thirtyinch.util.AnnotationUtil;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public abstract class TiScreen<P extends TiPresenter<V>, V extends TiView>
        implements TiPresenterProvider<P>, TiViewProvider<V>, InterceptableViewBinder<V> {

    private static final String TAG = TiScreen.class.getSimpleName();

    private P mPresenter;

    private boolean mStarted = false;

    private final TiActivity mTiActivity;

    private Removable mUiThreadBinderRemovable;

    private final PresenterViewBinder<V> mViewBinder;

    public TiScreen(final TiActivity tiActivity) {
        mViewBinder = new PresenterViewBinder<>(new TiLoggingTagProvider() {
            @Override
            public String getLoggingTag() {
                return TAG;
            }
        });
        mTiActivity = tiActivity;
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

    public synchronized P getPresenter() {
        return mPresenter;
    }

    public TiActivity getTiActivity() {
        return mTiActivity;
    }

    @Override
    public void invalidateView() {
        mViewBinder.invalidateView();
    }

    public void onCreate() {
        mPresenter = providePresenter();
        mPresenter.create();

        final TiConfiguration config = mPresenter.getConfig();
        if (config.isCallOnMainThreadInterceptorEnabled()) {
            addBindViewInterceptor(new CallOnMainThreadInterceptor());
        }

        if (config.isDistinctUntilChangedInterceptorEnabled()) {
            addBindViewInterceptor(new DistinctUntilChangedInterceptor());
        }

        //noinspection unchecked
        final UiThreadExecutorAutoBinder uiThreadAutoBinder =
                new UiThreadExecutorAutoBinder(mPresenter, getTiActivity().getUiThreadExecutor());

        // bind ui thread to presenter when view is attached
        mUiThreadBinderRemovable = mPresenter.addLifecycleObserver(uiThreadAutoBinder);
    }

    @Nullable
    public abstract View onCreateView(@NonNull final Context context,
            @NonNull final ViewGroup container);

    public void onDestroy() {
        if (mUiThreadBinderRemovable != null) {
            mUiThreadBinderRemovable.remove();
            mUiThreadBinderRemovable = null;
        }
    }

    public void onStart() {
        mStarted = true;
        mTiActivity.getUiThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                if (mStarted) {
                    mViewBinder.bindView(mPresenter, TiScreen.this);
                }
            }
        });
    }

    public void onStop() {
        mStarted = false;
        mPresenter.detachView();
    }

    @NonNull
    @Override
    public V provideView() {
        final Class<?> foundViewInterface = AnnotationUtil
                .getInterfaceOfClassExtendingGivenInterface(this.getClass(), TiView.class);

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
                return (V) this;
            }
        }
    }
}
