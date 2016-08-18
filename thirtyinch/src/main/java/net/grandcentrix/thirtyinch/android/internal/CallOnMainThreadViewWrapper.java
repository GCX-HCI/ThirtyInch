package net.grandcentrix.thirtyinch.android.internal;

import net.grandcentrix.thirtyinch.BindViewInterceptor;
import net.grandcentrix.thirtyinch.TiView;
import net.grandcentrix.thirtyinch.android.CallOnMainThread;

import android.util.Log;

import java.lang.reflect.Proxy;

import static net.grandcentrix.thirtyinch.util.AnnotationUtil.getInterfaceOfClassExtendingGivenInterface;
import static net.grandcentrix.thirtyinch.util.AnnotationUtil.hasObjectMethodWithAnnotation;

public class CallOnMainThreadViewWrapper implements BindViewInterceptor {

    private static final String TAG = CallOnMainThreadViewWrapper.class.getSimpleName();

    @Override
    public <V extends TiView> V intercept(final V view) {
        final V wrapped = wrap(view);
        Log.d(TAG, "wrapping View " + view + " in " + wrapped);
        return wrapped;
    }

    private <V extends TiView> V wrap(final V view) {

        Class<?> foundInterfaceClass = getInterfaceOfClassExtendingGivenInterface(
                view.getClass(), TiView.class);
        if (foundInterfaceClass == null) {
            throw new IllegalStateException("the interface extending View could not be found");
        }

        if (!hasObjectMethodWithAnnotation(view, CallOnMainThread.class)) {
            // not method has the annotation, returning original view
            // not creating a proxy
            return view;
        }

        //noinspection unchecked,UnnecessaryLocalVariable
        final V wrappedView = (V) Proxy.newProxyInstance(
                foundInterfaceClass.getClassLoader(), new Class<?>[]{foundInterfaceClass},
                new CallOnMainThreadInvocationHandler<>(view));

        return wrappedView;
    }
}
