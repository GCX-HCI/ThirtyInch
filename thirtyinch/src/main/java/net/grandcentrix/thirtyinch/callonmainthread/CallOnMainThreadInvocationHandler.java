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

package net.grandcentrix.thirtyinch.callonmainthread;

import android.os.Handler;
import android.os.Looper;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import net.grandcentrix.thirtyinch.TiView;
import net.grandcentrix.thirtyinch.util.AbstractInvocationHandler;

final class CallOnMainThreadInvocationHandler<V> extends AbstractInvocationHandler {

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final V mView;

    public CallOnMainThreadInvocationHandler(V view) {
        mView = view;
    }

    @Override
    public String toString() {
        return "MainThreadProxy@" + Integer.toHexString(this.hashCode()) + "-" + mView.toString();
    }

    @Override
    protected Object handleInvocation(final Object proxy, final Method method, final Object[] args)
            throws Throwable {

        try {
            // If the method is a method from Object then defer to normal invocation.
            final Class<?> declaringClass = method.getDeclaringClass();
            if (declaringClass == Object.class) {
                return method.invoke(this, args);
            }

            // simply call the method when already on the main thread
            if (Looper.getMainLooper() == Looper.myLooper()) {
                return method.invoke(mView, args);
            }

            // only void methods are supported. Otherwise
            if (!method.getReturnType().equals(Void.TYPE)) {
                return method.invoke(mView, args);
            }

            // only methods of the View interface are supported
            if (!TiView.class.isAssignableFrom(declaringClass)) {
                return method.invoke(mView, args);
            }

            final CallOnMainThread comtAnnotation =
                    method.getAnnotation(CallOnMainThread.class);
            // check if method is correct annotated
            if (comtAnnotation == null) {
                return method.invoke(mView, args);
            }

            // send calls on the Ui Thread
            handler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        method.invoke(mView, args);
                    } catch (InvocationTargetException e) {
                        // To be consistent, the exception will be thrown, not caught and swallowed.
                        // Sadly, this exception cannot be caught by wrapping the invoked method with try catch.
                        e.printStackTrace();
                        throw new RuntimeException(e.getCause());
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            return null;

        } catch (InvocationTargetException e) {
            e.printStackTrace();
            throw e.getCause();
        } catch (IllegalAccessException e) {
            throw e;
        }
    }
}
