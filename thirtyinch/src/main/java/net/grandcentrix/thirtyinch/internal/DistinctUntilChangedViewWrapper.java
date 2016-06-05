package net.grandcentrix.thirtyinch.internal;

import net.grandcentrix.thirtyinch.*;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Proxy;

import static net.grandcentrix.thirtyinch.util.AnnotationUtil.getInterfaceOfClassExtendingGivenInterface;
import static net.grandcentrix.thirtyinch.util.AnnotationUtil.hasObjectMethodWithAnnotation;

public class DistinctUntilChangedViewWrapper {

    @NotNull
    public static <V extends TiView> V wrap(@NotNull final V view) {

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
