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
import java.util.List;
import net.grandcentrix.thirtyinch.BindViewInterceptor;
import net.grandcentrix.thirtyinch.Removable;
import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.TiView;

public interface InterceptableViewBinder<V extends TiView> {

    /**
     * Like Predicate added in API 24 with Java 8
     * <p>
     * A Predicate can determine a true or false value for any input of its
     * parametrized type. For example, a {@code RegexPredicate} might implement
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
    Removable addBindViewInterceptor(@NonNull final BindViewInterceptor interceptor);

    /**
     * @param interceptor the interceptor which returned the {@link TiView}
     * @return the cached result of {@link BindViewInterceptor#intercept(TiView)}
     */
    @Nullable
    V getInterceptedViewOf(@NonNull final BindViewInterceptor interceptor);

    /**
     * @param predicate filter the results
     * @return all interceptors matching the filter
     */
    @NonNull
    List<BindViewInterceptor> getInterceptors(@NonNull final Filter<BindViewInterceptor> predicate);

    /**
     * Invalidates the cache of the latest bound view. Forces the next binding of the view to run
     * through all the interceptors (again).
     */
    void invalidateView();
}
