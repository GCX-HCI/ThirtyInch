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

import net.grandcentrix.thirtyinch.internal.DelegatedTiActivity;
import net.grandcentrix.thirtyinch.internal.InterceptableViewBinder;
import net.grandcentrix.thirtyinch.internal.PresenterAccessor;
import net.grandcentrix.thirtyinch.internal.PresenterNonConfigurationInstance;
import net.grandcentrix.thirtyinch.internal.PresenterSavior;
import net.grandcentrix.thirtyinch.internal.TiActivityDelegate;
import net.grandcentrix.thirtyinch.internal.TiLoggingTagProvider;
import net.grandcentrix.thirtyinch.internal.TiPresenterProvider;
import net.grandcentrix.thirtyinch.internal.TiViewProvider;
import net.grandcentrix.thirtyinch.internal.UiThreadExecutor;
import net.grandcentrix.thirtyinch.util.AndroidDeveloperOptions;
import net.grandcentrix.thirtyinch.util.AnnotationUtil;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * Created by pascalwelsch on 9/8/15.
 */
public abstract class TiActivity<P extends TiPresenter<V>, V extends TiView>
        extends AppCompatActivity
        implements TiPresenterProvider<P>, TiViewProvider<V>, DelegatedTiActivity<P>,
        TiLoggingTagProvider, InterceptableViewBinder<V>, PresenterAccessor<P, V> {

    private final String TAG = this.getClass().getSimpleName()
            + ":" + TiActivity.class.getSimpleName()
            + "@" + Integer.toHexString(this.hashCode());

    private final TiActivityDelegate<P, V> mDelegate
            = new TiActivityDelegate<>(this, this, this, this, PresenterSavior.getInstance());

    private final UiThreadExecutor mUiThreadExecutor = new UiThreadExecutor();

    @NonNull
    @Override
    public final Removable addBindViewInterceptor(@NonNull final BindViewInterceptor interceptor) {
        return mDelegate.addBindViewInterceptor(interceptor);
    }

    @Override
    public Activity getHostingActivity() {
        return this;
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

    /**
     * is {@code null} before {@link #onCreate(Bundle)}
     */
    @Override
    public final P getPresenter() {
        return mDelegate.getPresenter();
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public final P getRetainedPresenter() {
        // try recover presenter via lastNonConfigurationInstance
        // this works most of the time
        final Object nci = getLastCustomNonConfigurationInstance();
        if (nci instanceof PresenterNonConfigurationInstance) {
            final PresenterNonConfigurationInstance pnci = (PresenterNonConfigurationInstance) nci;
            return (P) pnci.getPresenter();
        }
        return null;
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
    public final boolean isActivityChangingConfigurations() {
        return isChangingConfigurations();
    }

    @Override
    public final boolean isActivityFinishing() {
        return isFinishing();
    }

    @Override
    public final boolean isDontKeepActivitiesEnabled() {
        return AndroidDeveloperOptions.isDontKeepActivitiesEnabled(this);
    }

    @CallSuper
    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDelegate.onConfigurationChanged_afterSuper(newConfig);
    }

    @Nullable
    @CallSuper
    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        final P presenter = mDelegate.getPresenter();
        if (presenter == null) {
            return null;
        }

        if (presenter.getConfig().shouldRetainPresenter()) {
            return new PresenterNonConfigurationInstance<>(presenter,
                    super.onRetainCustomNonConfigurationInstance());
        }

        return null;
    }

    @SuppressWarnings("unchecked")
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

    @Override
    public String toString() {
        String presenter = mDelegate.getPresenter() == null ? "null" :
                mDelegate.getPresenter().getClass().getSimpleName()
                        + "@" + Integer.toHexString(mDelegate.getPresenter().hashCode());

        return getClass().getSimpleName()
                + ":" + TiActivity.class.getSimpleName()
                + "@" + Integer.toHexString(hashCode())
                + "{presenter = " + presenter + "}";
    }

    @CallSuper
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDelegate.onCreate_afterSuper(savedInstanceState);
    }

    @CallSuper
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDelegate.onDestroy_afterSuper();
    }

    @CallSuper
    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        mDelegate.onSaveInstanceState_afterSuper(outState);
    }

    @CallSuper
    @Override
    protected void onStart() {
        super.onStart();
        mDelegate.onStart_afterSuper();
    }

    @CallSuper
    @Override
    protected void onStop() {
        mDelegate.onStop_beforeSuper();
        super.onStop();
        mDelegate.onStop_afterSuper();
    }


}
