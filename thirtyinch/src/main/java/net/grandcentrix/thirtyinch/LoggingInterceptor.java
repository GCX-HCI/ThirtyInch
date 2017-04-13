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
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import static net.grandcentrix.thirtyinch.util.AnnotationUtil.getInterfaceOfClassExtendingGivenInterface;

/**
 * Logs all methods calls and parameters to the bound view interface.
 */
public class LoggingInterceptor implements BindViewInterceptor {

    private final static class MethodLoggingInvocationHandler<V> extends AbstractInvocationHandler {

        /**
         * limit each argument instead of the complete string. This should limit the overall
         * output to a reasonable length while showing all params
         */
        public static final int MAX_LEN_OF_PARAM = 240;

        private TiLog.Logger mLogger;

        private final V mView;

        private MethodLoggingInvocationHandler(V view, @NonNull TiLog.Logger logger) {
            mView = view;
            mLogger = logger;
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
                mLogger.log(Log.VERBOSE, TAG, toString(method, args));
                return method.invoke(mView, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }

        private static String toString(@NonNull final Method method,
                @Nullable final Object[] args) {
            final StringBuilder sb = new StringBuilder(method.getName());
            sb.append("(");
            if (args != null && args.length > 0) {
                final String paramsString = parseParams(args, MAX_LEN_OF_PARAM);
                sb.append(paramsString);
            }
            sb.append(")");
            return sb.toString();
        }
    }

    private static final String TAG = MethodLoggingInvocationHandler.class.getSimpleName();

    private final TiLog.Logger mLogger;

    /**
     * Logs all view interface method invocations to {@link TiLog}. You may have to enable
     * logging from {@link TiLog} or set your own logger with {@link LoggingInterceptor#LoggingInterceptor(TiLog.Logger)}
     */
    public LoggingInterceptor() {
        this(TiLog.TI_LOG);
    }

    /**
     * Logs all view interface method invocations to the provided {@link TiLog.Logger} interface
     *
     * @param logger custom logger, {@link TiLog#LOGCAT} or {@link TiLog#NOOP} to disable logging.
     */
    public LoggingInterceptor(@Nullable final TiLog.Logger logger) {
        if (logger == null) {
            mLogger = TiLog.NOOP;
        } else {
            mLogger = logger;
        }
    }

    @Override
    public <V extends TiView> V intercept(final V view) {
        if (mLogger != TiLog.NOOP) {
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
            throw new IllegalStateException("the interface extending TiView could not be found");
        }

        final V wrappedView = (V) Proxy.newProxyInstance(
                foundInterfaceClass.getClassLoader(), new Class<?>[]{foundInterfaceClass},
                new MethodLoggingInvocationHandler<>(view, mLogger));

        return wrappedView;
    }

    private static String parseParams(Object[] methodParams, int maxLenOfParam) {
        int iMax = methodParams.length - 1;

        StringBuilder b = new StringBuilder();
        for (int i = 0; ; i++) {
            final Object arg = methodParams[i];

            final String param;
            if (arg instanceof List) {
                final int size = ((List) arg).size();
                final String stringPresentation = String.valueOf(arg);
                param = "{" + arg.getClass().getSimpleName()
                        + "[" + size + "]"
                        + "@" + Integer.toHexString(arg.hashCode()) + "}"
                        + " " + stringPresentation;
            } else if (arg instanceof Object[]) {
                final Object[] args = ((Object[]) arg);
                final int size = args.length;

                final StringBuilder sb = new StringBuilder();
                sb.append("[");
                for (int j = 0; j < args.length; j++) {
                    sb.append(String.valueOf(args[j]));
                    if (j + 1 < args.length) {
                        sb.append(", ");
                    }
                }
                sb.append("]");

                param = "{" + arg.getClass().getSimpleName()
                        + "[" + size + "]"
                        + "@" + Integer.toHexString(arg.hashCode()) + "}"
                        + " " + sb;
            } else {
                param = String.valueOf(arg);
            }

            if (param.length() <= maxLenOfParam) {
                b.append(param);
            } else {
                final String shortParam =
                        param.substring(0, Math.min(param.length(), maxLenOfParam));
                b.append(shortParam);
                // trim remaining whitespace at the end before appending ellipsis
                b = new StringBuilder(b.toString().trim());
                b.append("…");
            }
            if (i == iMax) {
                return b.toString();
            }
            b.append(", ");
        }
    }
}