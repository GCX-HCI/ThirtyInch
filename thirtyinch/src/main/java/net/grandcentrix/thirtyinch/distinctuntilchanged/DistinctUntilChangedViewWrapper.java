package net.grandcentrix.thirtyinch.distinctuntilchanged;

import net.grandcentrix.thirtyinch.TiBindViewInterceptor;
import net.grandcentrix.thirtyinch.TiView;

import android.support.annotation.NonNull;
import android.util.Log;

import java.lang.reflect.Proxy;

import static net.grandcentrix.thirtyinch.util.AnnotationUtil.getInterfaceOfClassExtendingGivenInterface;
import static net.grandcentrix.thirtyinch.util.AnnotationUtil.hasObjectMethodWithAnnotation;

public class DistinctUntilChangedViewWrapper implements TiBindViewInterceptor {

    private static final String TAG = DistinctUntilChangedViewWrapper.class.getSimpleName();

    @Override
    public <V extends TiView> V intercept(final V view) {
        final V wrapped = wrap(view);

        Log.d(TAG, "wrapping View " + view + " in " + wrapped);
        return wrapped;
    }

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

        //noinspection unchecked,UnnecessaryLocalVariable
        final V wrappedView = (V) Proxy.newProxyInstance(
                foundInterfaceClass.getClassLoader(), new Class<?>[]{foundInterfaceClass},
                new DistinctUntilChangedInvocationHandler<>(view));

        return wrappedView;
    }
}
