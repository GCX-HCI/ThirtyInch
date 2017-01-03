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

import com.pascalwelsch.compositeandroid.activity.ActivityPlugin;
import com.pascalwelsch.compositeandroid.activity.CompositeNonConfigurationInstance;

import net.grandcentrix.thirtyinch.BindViewInterceptor;
import net.grandcentrix.thirtyinch.Removable;
import net.grandcentrix.thirtyinch.TiActivity;
import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.TiView;
import net.grandcentrix.thirtyinch.internal.DelegatedTiActivity;
import net.grandcentrix.thirtyinch.internal.InterceptableViewBinder;
import net.grandcentrix.thirtyinch.internal.TiActivityDelegate;
import net.grandcentrix.thirtyinch.internal.TiLoggingTagProvider;
import net.grandcentrix.thirtyinch.internal.TiPresenterProvider;
import net.grandcentrix.thirtyinch.internal.TiViewProvider;
import net.grandcentrix.thirtyinch.util.AndroidDeveloperOptions;
import net.grandcentrix.thirtyinch.util.AnnotationUtil;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

public class TiActivityPlugin<P extends TiPresenter<V>, V extends TiView> extends ActivityPlugin
        implements TiViewProvider<V>, DelegatedTiActivity<P>, TiLoggingTagProvider,
        InterceptableViewBinder<V> {

    public static final String NCI_KEY_PRESENTER = "presenter";

    private String TAG = this.getClass().getSimpleName()
            + "@" + Integer.toHexString(this.hashCode());

    private TiActivityDelegate<P, V> mDelegate;

    /**
     * Binds a {@link TiPresenter} returned by the {@link TiPresenterProvider} to the {@link
     * Activity} and all future {@link Activity} instances created due to configuration changes.
     * The provider will be only called once during {@link TiActivityPlugin#onCreate(Bundle)}. This
     * lets you inject objects which require a {@link android.content.Context} and can't be
     * instantiated in the constructor of the {@link Activity}. Using the interface also prevents
     * instantiating the (possibly) heavy {@link TiPresenter} which will never be used when a
     * presenter is already created for this {@link Activity}.
     *
     * @param presenterProvider callback returning the presenter.
     */
    public TiActivityPlugin(@NonNull final TiPresenterProvider<P> presenterProvider) {
        mDelegate = new TiActivityDelegate<>(this, this, presenterProvider, this);
    }

    @NonNull
    @Override
    public Removable addBindViewInterceptor(@NonNull final BindViewInterceptor interceptor) {
        return mDelegate.addBindViewInterceptor(interceptor);
    }

    /**
     * @return the cached result of {@link BindViewInterceptor#intercept(TiView)}
     */
    @Nullable
    @Override
    public V getInterceptedViewOf(@NonNull final BindViewInterceptor interceptor) {
        return mDelegate.getInterceptedViewOf(interceptor);
    }

    /**
     * @param predicate filter the results
     * @return all interceptors matching the filter
     */
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

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public P getRetainedPresenter() {
        final Object nci = getLastNonConfigurationInstance(NCI_KEY_PRESENTER);
        if (nci != null) {
            return (P) nci;
        }
        return null;
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
    public boolean isActivityChangingConfigurations() {
        return getActivity().isChangingConfigurations();
    }

    @Override
    public boolean isActivityFinishing() {
        return getActivity().isFinishing();
    }

    @Override
    public boolean isDontKeepActivitiesEnabled() {
        return AndroidDeveloperOptions.isDontKeepActivitiesEnabled(getActivity());
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDelegate.onConfigurationChanged_afterSuper(newConfig);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDelegate.onCreate_afterSuper(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDelegate.onDestroy_afterSuper();
    }

    @Override
    @Nullable
    public CompositeNonConfigurationInstance onRetainNonConfigurationInstance() {
        final P presenter = mDelegate.getPresenter();
        if (presenter == null) {
            return null;
        }

        if (presenter.getConfig().shouldRetainPresenter()) {
            return new CompositeNonConfigurationInstance(NCI_KEY_PRESENTER, presenter);
        }

        return null;
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
        mDelegate.onStop_afterSuper();
    }

    @Override
    public boolean postToMessageQueue(final Runnable runnable) {
        return getActivity().getWindow().getDecorView().post(runnable);
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public V provideView() {
        final Class<?> foundViewInterface = AnnotationUtil
                .getInterfaceOfClassExtendingGivenInterface(getActivity().getClass(), TiView.class);

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
                return (V) getActivity();
            }
        }
    }

    @Override
    public String toString() {
        String presenter = mDelegate.getPresenter() == null ? "null" :
                mDelegate.getPresenter().getClass().getSimpleName()
                        + "@" + Integer.toHexString(mDelegate.getPresenter().hashCode());

        return getClass().getSimpleName()
                + ":" + TiActivity.class.getSimpleName()
                + "@" + Integer.toHexString(hashCode())
                + "{presenter=" + presenter + "}";
    }

    @Override
    protected void onAddedToDelegate() {
        super.onAddedToDelegate();
        TAG = getClass().getSimpleName()
                + ":" + TiActivity.class.getSimpleName()
                + "@" + Integer.toHexString(this.hashCode())
                + ":" + getOriginal().getClass().getSimpleName()
                + "@" + Integer.toHexString(getOriginal().hashCode());
    }

    @Override
    protected void onRemovedFromDelegated() {
        super.onRemovedFromDelegated();
        TAG = getClass().getSimpleName()
                + ":" + TiActivity.class.getSimpleName()
                + "@" + Integer.toHexString(this.hashCode());
    }
}
