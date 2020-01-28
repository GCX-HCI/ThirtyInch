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
import net.grandcentrix.thirtyinch.util.BackstackReader;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
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

/**
 * An Fragment which has a {@link TiPresenter} to build the Model View Presenter architecture on
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
 * <p>
 * The associated {@link TiPresenter} only lives when the {@link TiFragment} is added to the
 * {@link FragmentManager}. When {@link FragmentTransaction#remove(Fragment)} or {@link
 * FragmentTransaction#replace(int, Fragment)} results in removing this {@link TiFragment} from the
 * {@link FragmentManager} the {@link TiPresenter} gets destroyed. When the same {@link TiFragment}
 * instance will be added again a new {@link TiPresenter} will be created by calling {@link
 * #providePresenter()}.
 * </p>
 * <p>
 * The {@link TiPresenter} even survives when the {@link TiFragment} is in the
 * {@link FragmentManager} backstack. When the hosting Activity gets finished the
 * {@link TiPresenter} will be destroyed accordingly.
 * </p>
 * <p>
 * Using {@code setRetainInstance(true)} is not allowed as it causes many troubles. You should favor
 * the dumb view pattern and move all your state into the {@link TiPresenter}.
 * </p>
 *
 * <p>
 * Example:
 * <code>
 * <pre>
 * public class MyFragment extends TiFragment&lt;MyPresenter, MyView&gt; implements MyView {
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
public abstract class TiFragment<P extends TiPresenter<V>, V extends TiView> extends Fragment
        implements DelegatedTiFragment, TiPresenterProvider<P>, TiLoggingTagProvider,
        TiViewProvider<V>, InterceptableViewBinder<V>, PresenterAccessor<P, V> {

    private final String TAG = this.getClass().getSimpleName()
            + ":" + TiFragment.class.getSimpleName()
            + "@" + Integer.toHexString(this.hashCode());

    private final TiFragmentDelegate<P, V> mDelegate =
            new TiFragmentDelegate<>(this, this, this, this, PresenterSavior.getInstance());

    private final UiThreadExecutor mUiThreadExecutor = new UiThreadExecutor();

    @CallSuper
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDelegate.onCreate_afterSuper(savedInstanceState);
    }

    @CallSuper
    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
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
    public void onSaveInstanceState(@NonNull final Bundle outState) {
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
    public final boolean isFragmentAdded() {
        return isAdded();
    }

    @Override
    public final boolean isFragmentDetached() {
        return isDetached();
    }

    @Override
    public boolean isFragmentInBackstack() {
        return BackstackReader.isInBackStack(this);
    }

    @Override
    public boolean isFragmentRemoving() {
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

    /**
     * Don't use <code>setRetainInstance(true)</code>, it's designed for headless Fragments only.
     */
    @Override
    public void setRetainInstance(final boolean retain) {
        if (retain) {
            throw new IllegalStateException("Retaining TiFragment is not allowed. "
                    + "setRetainInstance(true) should only be used for headless Fragments. "
                    + "Move your state into the TiPresenter which survives recreation of TiFragment");
        }
        super.setRetainInstance(retain);
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