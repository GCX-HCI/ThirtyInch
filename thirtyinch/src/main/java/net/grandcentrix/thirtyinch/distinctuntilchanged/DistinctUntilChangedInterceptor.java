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

package net.grandcentrix.thirtyinch.distinctuntilchanged;

import static net.grandcentrix.thirtyinch.util.AnnotationUtil.getInterfaceOfClassExtendingGivenInterface;
import static net.grandcentrix.thirtyinch.util.AnnotationUtil.hasObjectMethodWithAnnotation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.lang.reflect.Proxy;
import net.grandcentrix.thirtyinch.BindViewInterceptor;
import net.grandcentrix.thirtyinch.TiLog;
import net.grandcentrix.thirtyinch.TiView;
import net.grandcentrix.thirtyinch.internal.InterceptableViewBinder;

public class DistinctUntilChangedInterceptor implements BindViewInterceptor {

    private static final String TAG = DistinctUntilChangedInterceptor.class.getSimpleName();

    @SuppressWarnings("unchecked")
    @Nullable
    public static DistinctUntilChangedInvocationHandler<TiView> unwrap(@NonNull final TiView view) {
        try {
            return (DistinctUntilChangedInvocationHandler) Proxy.getInvocationHandler(view);
        } catch (ClassCastException e) {
            return null;
        }
    }

    public <V extends TiView> void clearCache(final InterceptableViewBinder<V> interceptable) {
        final TiView wrappedView = interceptable.getInterceptedViewOf(this);
        if (wrappedView != null) {
            final DistinctUntilChangedInvocationHandler<TiView> view
                    = DistinctUntilChangedInterceptor.unwrap(wrappedView);
            if (view != null) {
                view.clearCache();
                TiLog.v(TAG, "cleared the distinctUntilChanged cache of " + view);
            }
        }
    }

    @Override
    public <V extends TiView> V intercept(final V view) {
        final V wrapped = wrap(view);
        TiLog.v(TAG, "wrapping View " + view + " in " + wrapped);
        return wrapped;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    public <V extends TiView> V wrap(@NonNull final V view) {

        Class<?> foundInterfaceClass =
                getInterfaceOfClassExtendingGivenInterface(view.getClass(), TiView.class);
        if (foundInterfaceClass == null) {
            throw new IllegalStateException("the interface extending View could not be found");
        }

        if (!hasObjectMethodWithAnnotation(view, DistinctUntilChanged.class)) {
            // not method has the annotation, returning original view
            // not creating a proxy
            return view;
        }

        return (V) Proxy.newProxyInstance(
                foundInterfaceClass.getClassLoader(), new Class<?>[]{foundInterfaceClass},
                new DistinctUntilChangedInvocationHandler<>(view));
    }
}
