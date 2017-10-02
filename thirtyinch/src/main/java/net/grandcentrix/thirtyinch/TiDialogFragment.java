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

package net.grandcentrix.thirtyinch;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.BackstackReader;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.List;
import java.util.concurrent.Executor;
import net.grandcentrix.thirtyinch.internal.DelegatedTiFragment;
import net.grandcentrix.thirtyinch.internal.InterceptableViewBinder;
import net.grandcentrix.thirtyinch.internal.PresenterAccessor;
import net.grandcentrix.thirtyinch.internal.PresenterSavior;
import net.grandcentrix.thirtyinch.internal.TiFragmentDelegate;
import net.grandcentrix.thirtyinch.internal.TiLoggingTagProvider;
import net.grandcentrix.thirtyinch.internal.TiPresenterProvider;
import net.grandcentrix.thirtyinch.internal.TiViewProvider;
import net.grandcentrix.thirtyinch.internal.UiThreadExecutor;
import net.grandcentrix.thirtyinch.util.AnnotationUtil;

public abstract class TiDialogFragment<P extends TiPresenter<V>, V extends TiView>
        extends AppCompatDialogFragment
        implements DelegatedTiFragment, TiPresenterProvider<P>, TiLoggingTagProvider,
        TiViewProvider<V>, InterceptableViewBinder<V>, PresenterAccessor<P, V> {

    private final String TAG = this.getClass().getSimpleName()
            + ":" + TiDialogFragment.class.getSimpleName()
            + "@" + Integer.toHexString(this.hashCode());

    private final TiFragmentDelegate<P, V> mDelegate =
            new TiFragmentDelegate<>(this, this, this, this, PresenterSavior.getInstance());

    @CallSuper
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDelegate.onCreate_afterSuper(savedInstanceState);
    }

    @CallSuper
    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState) {
        mDelegate.invalidateView();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @CallSuper
    @Override
    public void onStart() {
        super.onStart();
        mDelegate.onStart_afterSuper();
    }

    @CallSuper
    @Override
    public void onStop() {
        mDelegate.onStop_beforeSuper();
        super.onStop();
    }

    @CallSuper
    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        mDelegate.onSaveInstanceState_afterSuper(outState);
    }

    @CallSuper
    @Override
    public void onDestroyView() {
        mDelegate.onDestroyView_beforeSuper();
        super.onDestroyView();
    }

    @CallSuper
    @Override
    public void onDestroy() {
        super.onDestroy();
        mDelegate.onDestroy_afterSuper();
    }

    @NonNull
    @Override
    public final Removable addBindViewInterceptor(@NonNull final BindViewInterceptor interceptor) {
        return mDelegate.addBindViewInterceptor(interceptor);
    }

    @Override
    public final Object getHostingContainer() {
        return getHost();
    }

    @Nullable
    @Override
    public final V getInterceptedViewOf(@NonNull final BindViewInterceptor interceptor) {
        return mDelegate.getInterceptedViewOf(interceptor);
    }

    @NonNull
    @Override
    public final List<BindViewInterceptor> getInterceptors(
            @NonNull final Filter<BindViewInterceptor> predicate) {
        return mDelegate.getInterceptors(predicate);
    }

    @Override
    public String getLoggingTag() {
        return TAG;
    }

    @Override
    public final P getPresenter() {
        return mDelegate.getPresenter();
    }

    @Override
    public final Executor getUiThreadExecutor() {
        return new UiThreadExecutor();
    }

    /**
     * Invalidates the cache of the latest bound view. Forces the next binding of the view to run
     * through all the interceptors (again).
     */
    @Override
    public final void invalidateView() {
        mDelegate.invalidateView();
    }

    @Override
    public final boolean isFragmentAdded() {
        return isAdded();
    }

    @Override
    public final boolean isFragmentDetached() {
        return isDetached();
    }

    @Override
    public final boolean isFragmentInBackstack() {
        return BackstackReader.isInBackStack(this);
    }

    @Override
    public final boolean isFragmentRemoving() {
        return isRemoving();
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
                    "This DialogFragment doesn't implement a TiView interface. "
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
    public String toString() {
        String presenter = getPresenter() == null ? "null" :
                getPresenter().getClass().getSimpleName()
                        + "@" + Integer.toHexString(getPresenter().hashCode());

        return getClass().getSimpleName()
                + "@" + Integer.toHexString(hashCode())
                + "{presenter=" + presenter + "}";
    }
}
