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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.grandcentrix.thirtyinch.BindViewInterceptor;
import net.grandcentrix.thirtyinch.Removable;
import net.grandcentrix.thirtyinch.TiLog;
import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.TiView;

/**
 * Binds a {@link TiView} to a {@link TiPresenter} and allows {@link BindViewInterceptor}s to
 * pivot the view before attaching
 *
 * @param <V> the {@link TiView}
 */
public class PresenterViewBinder<V extends TiView> implements InterceptableViewBinder<V> {

    private List<BindViewInterceptor> mBindViewInterceptors = new ArrayList<>();

    private HashMap<BindViewInterceptor, V> mInterceptorViewOutput = new HashMap<>();

    /**
     * the cached version of the view send to the presenter after it passed the interceptors
     */
    private V mLastView;

    private final TiLoggingTagProvider mLogTag;

    public PresenterViewBinder(final TiLoggingTagProvider loggingTagProvider) {
        mLogTag = loggingTagProvider;
    }

    @NonNull
    @Override
    public Removable addBindViewInterceptor(@NonNull final BindViewInterceptor interceptor) {
        mBindViewInterceptors.add(interceptor);
        invalidateView();

        return new OneTimeRemovable() {
            @Override
            public void onRemove() {
                mBindViewInterceptors.remove(interceptor);
                invalidateView();
            }
        };
    }

    /**
     * binds the view (this Activity) to the {@code presenter}. Allows interceptors to change,
     * delegate or wrap the view before it gets attached to the presenter.
     */
    public void bindView(final TiPresenter<V> presenter, final TiViewProvider<V> viewProvider) {
        if (mLastView == null) {
            invalidateView();
            V interceptedView = viewProvider.provideView();
            for (final BindViewInterceptor interceptor : mBindViewInterceptors) {
                interceptedView = interceptor.intercept(interceptedView);
                mInterceptorViewOutput.put(interceptor, interceptedView);
            }
            mLastView = interceptedView;
            TiLog.v(mLogTag.getLoggingTag(), "binding NEW view to Presenter " + mLastView);
            presenter.attachView(mLastView);
        } else {
            TiLog.v(mLogTag.getLoggingTag(), "binding the cached view to Presenter " + mLastView);
            presenter.attachView(mLastView);
        }
    }

    @Nullable
    @Override
    public V getInterceptedViewOf(@NonNull final BindViewInterceptor interceptor) {
        return mInterceptorViewOutput.get(interceptor);
    }

    @NonNull
    @Override
    public List<BindViewInterceptor> getInterceptors(
            @NonNull final Filter<BindViewInterceptor> predicate) {
        final ArrayList<BindViewInterceptor> result = new ArrayList<>();
        for (int i = 0; i < mBindViewInterceptors.size(); i++) {
            final BindViewInterceptor interceptor = mBindViewInterceptors.get(i);
            if (predicate.apply(interceptor)) {
                result.add(interceptor);
            }
        }
        return result;
    }

    @Override
    public void invalidateView() {
        mLastView = null;
        mInterceptorViewOutput.clear();
    }
}
