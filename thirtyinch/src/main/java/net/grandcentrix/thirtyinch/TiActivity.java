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

import android.content.res.Configuration;
import android.os.Bundle;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;
import java.util.concurrent.Executor;
import net.grandcentrix.thirtyinch.internal.DelegatedTiActivity;
import net.grandcentrix.thirtyinch.internal.InterceptableViewBinder;
import net.grandcentrix.thirtyinch.internal.PresenterAccessor;
import net.grandcentrix.thirtyinch.internal.PresenterSavior;
import net.grandcentrix.thirtyinch.internal.TiActivityDelegate;
import net.grandcentrix.thirtyinch.internal.TiLoggingTagProvider;
import net.grandcentrix.thirtyinch.internal.TiPresenterProvider;
import net.grandcentrix.thirtyinch.internal.TiViewProvider;
import net.grandcentrix.thirtyinch.internal.UiThreadExecutor;
import net.grandcentrix.thirtyinch.util.AnnotationUtil;

/**
 * An Activity which has a {@link TiPresenter} to build the Model View Presenter architecture on
 * Android.
 *
 * <p>
 * The {@link TiPresenter} will be created in {@link #providePresenter()} called in
 * {@link #onCreate(Bundle)}. Depending on the {@link TiConfiguration} passed into the
 * {@link TiPresenter#TiPresenter(TiConfiguration)} constructor the {@link TiPresenter} survives
 * orientation changes (default).
 * </p>
 * <p>
 * The {@link TiPresenter} requires a interface to communicate with the View. Normally the Activity
 * implements the View interface (which must extend {@link TiView}) and is returned by default
 * from {@link #provideView()}.
 * </p>
 *
 * <p>
 * Example:
 * <code>
 * <pre>
 * public class MyActivity extends TiActivity&lt;MyPresenter, MyView&gt; implements MyView {
 *
 *     &#064;Override
 *     public MyPresenter providePresenter() {
 *         return new MyPresenter();
 *     }
 * }
 *
 * public class MyPresenter extends TiPresenter&lt;MyView&gt; {
 *
 *     &#064;Override
 *     protected void onCreate() {
 *         super.onCreate();
 *     }
 * }
 *
 * public interface MyView extends TiView {
 *
 *     // void showItems(List&lt;Item&gt; items);
 *
 *     // Observable&lt;Item&gt; onItemClicked();
 * }
 * </pre>
 * </code>
 * </p>
 *
 * @param <V> the View type, must implement {@link TiView}
 * @param <P> the Presenter type, must extend {@link TiPresenter}
 */
public abstract class TiActivity<P extends TiPresenter<V>, V extends TiView>
        extends AppCompatActivity
        implements TiPresenterProvider<P>, TiViewProvider<V>, DelegatedTiActivity,
        TiLoggingTagProvider, InterceptableViewBinder<V>, PresenterAccessor<P, V> {

    private final String TAG = this.getClass().getSimpleName()
            + ":" + TiActivity.class.getSimpleName()
            + "@" + Integer.toHexString(this.hashCode());

    private final TiActivityDelegate<P, V> mDelegate
            = new TiActivityDelegate<>(this, this, this, this, PresenterSavior.getInstance());

    private final UiThreadExecutor mUiThreadExecutor = new UiThreadExecutor();

    @CallSuper
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDelegate.onCreate_afterSuper(savedInstanceState);
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

    @CallSuper
    @Override
    protected void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        mDelegate.onSaveInstanceState_afterSuper(outState);
    }

    @CallSuper
    @Override
    protected void onDestroy() {
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
    public final boolean isActivityFinishing() {
        return isFinishing();
    }

    @CallSuper
    @Override
    public void onConfigurationChanged(@NonNull final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDelegate.onConfigurationChanged_afterSuper(newConfig);
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


}
