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

import net.grandcentrix.thirtyinch.util.AbstractInvocationHandler;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static android.content.ContentValues.TAG;
import static net.grandcentrix.thirtyinch.util.AnnotationUtil.getInterfaceOfClassExtendingGivenInterface;

/**
 * Logs all methods calls and parameters to the bound view interface.
 */
public class LoggingInterceptor implements BindViewInterceptor {

    private final static class MethodLoggingInvocationHandler<V> extends AbstractInvocationHandler {

        private final V mView;

        private MethodLoggingInvocationHandler(V view) {
            mView = view;
        }

        @Override
        public String toString() {
            return "MethodLoggingProxy@" + Integer.toHexString(this.hashCode()) + "-" + mView
                    .toString();
        }

        @Override
        protected Object handleInvocation(final Object proxy, final Method method,
                final Object[] args)
                throws Throwable {

            try {
                // If the method is a method from Object then defer to normal invocation.
                final Class<?> declaringClass = method.getDeclaringClass();
                if (declaringClass == Object.class) {
                    return method.invoke(this, args);
                }

                TiLog.v(TAG, toString(method, args));
                return method.invoke(mView, args);

            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }

        private static String toString(@NonNull final Method method,
                @Nullable final Object[] args) {
            if (args == null || args.length == 0) {
                return "";
            }
            final String paramsString = parseParams(args, 240);
            //noinspection StringBufferReplaceableByString
            final StringBuilder sb = new StringBuilder(method.getName());
            sb.append("(");
            sb.append(paramsString);
            sb.append(")");
            return sb.toString();
        }
    }

    private final boolean mEnableLogging;

    public LoggingInterceptor(final boolean enableLogging) {
        mEnableLogging = enableLogging;
    }

    @Override
    public <V extends TiView> V intercept(final V view) {
        if (mEnableLogging) {
            final V wrapped = wrap(view);
            TiLog.v(TAG, "wrapping View " + view + " in " + wrapped);
            return wrapped;
        }
        return view;
    }

    @SuppressWarnings("unchecked")
    private <V extends TiView> V wrap(final V view) {

        Class<?> foundInterfaceClass = getInterfaceOfClassExtendingGivenInterface(
                view.getClass(), TiView.class);
        if (foundInterfaceClass == null) {
            throw new IllegalStateException("the interface extending View could not be found");
        }

        final V wrappedView = (V) Proxy.newProxyInstance(
                foundInterfaceClass.getClassLoader(), new Class<?>[]{foundInterfaceClass},
                new MethodLoggingInvocationHandler<>(view));

        return wrappedView;
    }

    private static String parseParams(Object[] a, int maxLenOfParam) {
        if (a == null) {
            return "null";
        }

        int iMax = a.length - 1;
        if (iMax == -1) {
            return "[]";
        }

        StringBuilder b = new StringBuilder();
        for (int i = 0; ; i++) {
            final String param = String.valueOf(a[i]);
            if (param.length() <= maxLenOfParam) {
                b.append(param);
            } else {
                final String shortParam =
                        param.substring(0, Math.min(param.length(), maxLenOfParam));
                b.append(shortParam);
                b.append("...");
            }
            if (i == iMax) {
                return b.toString();
            }
            b.append(", ");
        }
    }
}