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

package net.grandcentrix.thirtyinch;

import net.grandcentrix.thirtyinch.internal.DelegatedTiFragment;
import net.grandcentrix.thirtyinch.internal.InterceptableViewBinder;
import net.grandcentrix.thirtyinch.internal.TiFragmentDelegate;
import net.grandcentrix.thirtyinch.internal.TiLoggingTagProvider;
import net.grandcentrix.thirtyinch.internal.TiPresenterProvider;
import net.grandcentrix.thirtyinch.internal.TiViewProvider;
import net.grandcentrix.thirtyinch.util.AndroidDeveloperOptions;
import net.grandcentrix.thirtyinch.util.AnnotationUtil;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public abstract class TiFragment<P extends TiPresenter<V>, V extends TiView> extends Fragment
        implements DelegatedTiFragment, TiPresenterProvider<P>, TiLoggingTagProvider,
        TiViewProvider<V>, InterceptableViewBinder<V> {

    private final String TAG = this.getClass().getSimpleName()
            + ":" + TiFragment.class.getSimpleName()
            + "@" + Integer.toHexString(this.hashCode());

    private final TiFragmentDelegate<P, V> mDelegate =
            new TiFragmentDelegate<>(this, this, this, this);

    @NonNull
    @Override
    public Removable addBindViewInterceptor(@NonNull final BindViewInterceptor interceptor) {
        return mDelegate.addBindViewInterceptor(interceptor);
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

    public P getPresenter() {
        return mDelegate.getPresenter();
    }

    /**
     * Invalidates the cache of the latest bound view. Forces the next binding of the view to run
     * through all the interceptors (again).
     */
    @Override
    public void invalidateView() {
        mDelegate.invalidateView();
    }

    @Override
    public boolean isDontKeepActivitiesEnabled() {
        return AndroidDeveloperOptions.isDontKeepActivitiesEnabled(getActivity());
    }

    @Override
    public boolean isFragmentAdded() {
        return isAdded();
    }

    @Override
    public boolean isFragmentDetached() {
        return isDetached();
    }

    @Override
    public boolean isHostingActivityChangingConfigurations() {
        return getActivity().isChangingConfigurations();
    }

    @Override
    public boolean isHostingActivityFinishing() {
        return getActivity().isFinishing();
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDelegate.onCreate_afterSuper(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState) {
        mDelegate.onCreateView_beforeSuper(inflater, container, savedInstanceState);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDelegate.onDestroy_afterSuper();
    }

    @Override
    public void onDestroyView() {
        mDelegate.onDestroyView_beforeSuper();
        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        mDelegate.onSaveInstanceState_afterSuper(outState);
    }

    @Override
    public void onStart() {
        super.onStart();
        mDelegate.onStart_afterSuper();
    }

    @Override
    public void onStop() {
        mDelegate.onStop_beforeSuper();
        super.onStop();
    }

    @Override
    public boolean postToMessageQueue(final Runnable runnable) {
        return getActivity().getWindow().getDecorView().post(runnable);
    }

    /**
     * the default implementation assumes that the fragment is the view and implements the {@link
     * TiView} interface. Override this method for a different behaviour.
     *
     * @return the object implementing the TiView interface
     */
    @SuppressWarnings("unchecked")
    @NonNull
    public V provideView() {

        final Class<?> foundViewInterface = AnnotationUtil
                .getInterfaceOfClassExtendingGivenInterface(getClass(), TiView.class);

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
                // assume that the fragment itself is the view and implements the TiView interface
                return (V) this;
            }
        }
    }

    @Override
    public void setFragmentRetainInstance(final boolean retain) {
        setRetainInstance(retain);
    }

    @Override
    public String toString() {
        String presenter = getPresenter() == null ? "null" :
                getPresenter().getClass().getSimpleName()
                        + "@" + Integer.toHexString(getPresenter().hashCode());

        return getClass().getSimpleName()
                + "@" + Integer.toHexString(hashCode())
                + "{presenter=" + presenter + "}";
    }
}