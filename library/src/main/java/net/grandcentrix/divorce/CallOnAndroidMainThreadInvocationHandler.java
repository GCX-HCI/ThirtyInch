package net.grandcentrix.divorce;

import android.os.Handler;
import android.os.Looper;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class CallOnAndroidMainThreadInvocationHandler<V> implements InvocationHandler {

    private final Handler handler = new Handler(Looper.getMainLooper());

    private V mView;

    public CallOnAndroidMainThreadInvocationHandler(V view) {
        mView = view;
    }

    @Override
    public synchronized Object invoke(final Object proxy, final Method method,
            final Object[] args) throws Throwable {

        // If the method is a method from Object then defer to normal invocation.
        final Class<?> declaringClass = method.getDeclaringClass();
        if (declaringClass == Object.class) {
            return method.invoke(this, args);
        }

        if (View.class.isAssignableFrom(declaringClass)
                && method.getReturnType().equals(Void.TYPE)) {
            // call view void methods on main thread
            handler.post(new Runnable() {
                @Override
                public void run() {
                    //noinspection TryWithIdenticalCatches
                    try {
                        Object result = method.invoke(mView, args);
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            });
            return null;
        } else {
            return method.invoke(mView, args);
        }
    }
}
