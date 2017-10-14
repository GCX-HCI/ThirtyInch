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

package net.grandcentrix.thirtyinch.plugin;


import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.BackstackReader;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.pascalwelsch.compositeandroid.fragment.FragmentPlugin;
import java.util.List;
import java.util.concurrent.Executor;
import net.grandcentrix.thirtyinch.BindViewInterceptor;
import net.grandcentrix.thirtyinch.Removable;
import net.grandcentrix.thirtyinch.TiFragment;
import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.TiView;
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

/**
 * Adds a {@link TiPresenter} to a Fragment. Can be used for both, {@link Fragment} and
 * {@link android.support.v4.app.DialogFragment}
 *
 * @param <P> {@link TiPresenter} with will be attached
 * @param <V> View, expected by the {@link TiPresenter}
 */
public class TiFragmentPlugin<P extends TiPresenter<V>, V extends TiView> extends FragmentPlugin
        implements TiViewProvider<V>, DelegatedTiFragment, TiLoggingTagProvider,
        InterceptableViewBinder<V>, PresenterAccessor<P, V> {

    private String TAG = this.getClass().getSimpleName()
            + "@" + Integer.toHexString(this.hashCode());

    private TiFragmentDelegate<P, V> mDelegate;

    private final UiThreadExecutor mUiThreadExecutor = new UiThreadExecutor();

    /**
     * Binds a {@link TiPresenter} returned by the {@link TiPresenterProvider} to the {@link
     * Fragment} and all future {@link Fragment} instances created due to configuration changes.
     * The provider will be only called once during {@link TiFragmentPlugin#onCreate(Bundle)}. This
     * lets you inject objects which require a {@link android.content.Context} and can't be
     * instantiated earlier. Using the interface also prevents instantiating the (possibly) heavy
     * {@link TiPresenter} which will never be used when a presenter is already created for this
     * {@link Fragment}.
     *
     * @param presenterProvider callback returning the presenter.
     */
    public TiFragmentPlugin(@NonNull final TiPresenterProvider<P> presenterProvider) {
        mDelegate = new TiFragmentDelegate<>(this, this, presenterProvider, this,
                PresenterSavior.getInstance());
    }

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
        mDelegate.onCreateView_beforeSuper(inflater, container, savedInstanceState);
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
        return getFragment().getHost();
    }

    /**
     * @return the cached result of {@link BindViewInterceptor#intercept(TiView)}
     */
    @Nullable
    @Override
    public final V getInterceptedViewOf(@NonNull final BindViewInterceptor interceptor) {
        return mDelegate.getInterceptedViewOf(interceptor);
    }

    /**
     * @param predicate filter the results
     * @return all interceptors matching the filter
     */
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
        return mUiThreadExecutor;
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
        return getFragment().isAdded();
    }

    @Override
    public final boolean isFragmentDetached() {
        return getFragment().isDetached();
    }

    @Override
    public boolean isFragmentInBackstack() {
        return BackstackReader.isInBackStack(getFragment());
    }

    @Override
    public boolean isFragmentRemoving() {
        return getFragment().isRemoving();
    }

    /**
     * the default implementation assumes that the fragment is the view and implements the {@link
     * TiView} interface. Override this method for a different behaviour.
     *
     * @return the object implementing the TiView interface
     */
    @NonNull
    public V provideView() {

        final Class<?> foundViewInterface = AnnotationUtil
                .getInterfaceOfClassExtendingGivenInterface(getFragment().getClass(), TiView.class);

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
                //noinspection unchecked
                return (V) getFragment();
            }
        }
    }

    @Override
    public String toString() {
        String presenter = mDelegate.getPresenter() == null ? "null" :
                mDelegate.getPresenter().getClass().getSimpleName()
                        + "@" + Integer.toHexString(mDelegate.getPresenter().hashCode());

        return getClass().getSimpleName()
                + ":" + TiFragment.class.getSimpleName()
                + "@" + Integer.toHexString(hashCode())
                + "{presenter=" + presenter + "}";
    }

    @Override
    protected void onAddedToDelegate() {
        super.onAddedToDelegate();
        TAG = getClass().getSimpleName()
                + ":" + TiFragment.class.getSimpleName()
                + "@" + Integer.toHexString(this.hashCode())
                + ":" + getOriginal().getClass().getSimpleName()
                + "@" + Integer.toHexString(getOriginal().hashCode());
    }

    @Override
    protected void onRemovedFromDelegated() {
        super.onRemovedFromDelegated();
        TAG = getClass().getSimpleName()
                + ":" + TiFragment.class.getSimpleName()
                + "@" + Integer.toHexString(this.hashCode());
    }
}
