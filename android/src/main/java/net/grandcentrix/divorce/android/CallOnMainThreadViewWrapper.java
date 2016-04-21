package net.grandcentrix.divorce.android;

import net.grandcentrix.divorce.View;

import java.lang.reflect.Proxy;

import static net.grandcentrix.divorce.util.AnnotationUtil.getInterfaceOfClassExtendingGivenInterface;
import static net.grandcentrix.divorce.util.AnnotationUtil.hasObjectMethodWithAnnotation;

public class CallOnMainThreadViewWrapper {

    public static <V extends View> V wrap(final V view) {

        Class<?> foundInterfaceClass = getInterfaceOfClassExtendingGivenInterface(
                view.getClass(), View.class);
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
