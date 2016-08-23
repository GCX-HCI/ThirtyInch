package net.grandcentrix.thirtyinch.internal;

import net.grandcentrix.thirtyinch.Removable;
import net.grandcentrix.thirtyinch.BindViewInterceptor;
import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.TiView;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

public interface InterceptableViewBinder<V extends TiView> {

    /**
     * Like Predicate added in API 24 with Java 8
     * <p>
     * A Predicate can determine a true or false value for any input of its
     * parameterized type. For example, a {@code RegexPredicate} might implement
     * {@code Predicate<String>}, and return true for any String that matches its
     * given regular expression.
     * <p/>
     * <p/>
     * Implementors of Predicate which may cause side effects upon evaluation are
     * strongly encouraged to state this fact clearly in their API documentation.
     */
    interface Filter<T> {

        boolean apply(T it);
    }

    /**
     * Adds an interceptor allowing to intercept the view which will be bound to the {@link
     * TiPresenter}
     */
    @NonNull
    Removable addBindViewInterceptor(final BindViewInterceptor interceptor);

    /**
     * @return the cached result of {@link BindViewInterceptor#intercept(TiView)}
     * @param interceptor the interceptor which returned the {@link TiView}
     */
    @Nullable
    V getInterceptedViewOf(final BindViewInterceptor interceptor);

    /**
     * @param predicate filter the results
     * @return all interceptors matching the filter
     */
    @NonNull
    List<BindViewInterceptor> getInterceptors(final Filter<BindViewInterceptor> predicate);

    /**
     * Invalidates the cache of the latest bound view. Forces the next binding of the view to run
     * through all the interceptors (again).
     */
    void invalidateView();
}
