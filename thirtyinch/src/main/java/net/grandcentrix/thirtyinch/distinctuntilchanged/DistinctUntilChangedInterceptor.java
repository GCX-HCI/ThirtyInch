package net.grandcentrix.thirtyinch.distinctuntilchanged;

import net.grandcentrix.thirtyinch.BindViewInterceptor;
import net.grandcentrix.thirtyinch.TiView;
import net.grandcentrix.thirtyinch.internal.InterceptableViewBinder;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.lang.reflect.Proxy;

import static net.grandcentrix.thirtyinch.util.AnnotationUtil.getInterfaceOfClassExtendingGivenInterface;
import static net.grandcentrix.thirtyinch.util.AnnotationUtil.hasObjectMethodWithAnnotation;

public class DistinctUntilChangedInterceptor implements BindViewInterceptor {

    private static final String TAG = DistinctUntilChangedInterceptor.class.getSimpleName();

    @Nullable
    public static DistinctUntilChangedInvocationHandler<TiView> unwrap(@NonNull final TiView view) {
        try {
            //noinspection unchecked
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
                Log.v(TAG, "cleared the distinctUntilChanged cache of " + view);
            }
        }
    }

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
