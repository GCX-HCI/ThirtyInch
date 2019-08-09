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

import androidx.annotation.VisibleForTesting;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import net.grandcentrix.thirtyinch.TiLog;
import net.grandcentrix.thirtyinch.TiView;
import net.grandcentrix.thirtyinch.util.AbstractInvocationHandler;

final class DistinctUntilChangedInvocationHandler<V> extends AbstractInvocationHandler {

    private static final String TAG = DistinctUntilChangedInvocationHandler.class.getSimpleName();

    @VisibleForTesting
    HashMap<String, DistinctComparator> mLatestMethodCalls = new HashMap<>();

    private final V mView;

    public DistinctUntilChangedInvocationHandler(V view) {
        mView = view;
    }

    public void clearCache() {
        mLatestMethodCalls.clear();
    }

    @Override
    public String toString() {
        return "DistinctUntilChangedProxy@"
                + Integer.toHexString(this.hashCode()) + "-" + mView.toString();
    }

    @Override
    protected Object handleInvocation(final Object proxy, final Method method, final Object[] args)
            throws Throwable {

        //noinspection TryWithIdenticalCatches
        try {
            // If the method is a method from Object then defer to normal invocation.
            final Class<?> declaringClass = method.getDeclaringClass();
            if (declaringClass == Object.class) {
                return method.invoke(this, args);
            }

            // always call methods with zero arguments
            if (args == null || args.length == 0) {
                return method.invoke(mView, args);
            }

            // only void methods support distinctUntilChanged
            if (!method.getReturnType().equals(Void.TYPE)) {
                return method.invoke(mView, args);
            }

            // @DistinctUntilChanged is only valid on methods of the view interface extending View
            if (!TiView.class.isAssignableFrom(declaringClass)) {
                return method.invoke(mView, args);
            }

            final DistinctUntilChanged ducAnnotation =
                    method.getAnnotation(DistinctUntilChanged.class);

            // check if method is correct annotated
            if (ducAnnotation == null) {
                return method.invoke(mView, args);
            }

            final String methodName = method.toGenericString();

            final DistinctComparator comparator = mLatestMethodCalls.get(methodName);
            if (comparator == null) {
                // detected first call to method

                // initialize a new comparator defined by the annotation
                DistinctComparator newComparator = ducAnnotation.comparator().newInstance();

                // initialize the comparator with the already called parameters
                // the comparator is now able to compare this call with the next one
                if (newComparator.compareWith(args)) {
                    // when initializing the comparator with the first call it cannot return true
                    // which would mean the first call is the same as the previous call which
                    // never happened
                    throw new IllegalStateException("comparator returns 'true' at initialization.");
                }
                // save for later usage
                mLatestMethodCalls.put(methodName, newComparator);

                // it's the first call to this method, call it
                return method.invoke(mView, args);
            }

            // compare with last called arguments
            if (!comparator.compareWith(args)) {
                // arguments changed, call the method
                return method.invoke(mView, args);
            } else {
                // don't call the method, the data was already sent to the view
                if (ducAnnotation.logDropped()) {
                    TiLog.d(TAG, "not calling " + method
                            + " with args " + Arrays.toString(args) + "."
                            + " Was already called with the same parameters before.");
                }
                return null;
            }

        } catch (InvocationTargetException e) {
            e.printStackTrace();
            throw e.getCause();
        } catch (IllegalAccessException e) {
            throw e;
        }
    }
}
