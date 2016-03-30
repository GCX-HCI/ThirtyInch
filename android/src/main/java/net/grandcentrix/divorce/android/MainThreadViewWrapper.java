package net.grandcentrix.divorce.android;

import net.grandcentrix.divorce.View;

import java.lang.reflect.Proxy;

public class MainThreadViewWrapper {

    public static <V extends View> V wrap(final V view) {

        Class<?> foundInterfaceClass = getInterfaceOfClassExtendingGivenInterface(
                view.getClass(), View.class);
        if (foundInterfaceClass == null) {
            throw new IllegalStateException("the interface extending View could not be found");
        }
        //noinspection unchecked,UnnecessaryLocalVariable
        final V wrappedView = (V) Proxy.newProxyInstance(
                foundInterfaceClass.getClassLoader(), new Class<?>[]{foundInterfaceClass},
                new CallOnAndroidMainThreadInvocationHandler<>(view));

        return wrappedView;
    }

    private static Class<?> getInterfaceOfClassExtendingGivenInterface(
            final Class<?> possibleExtendingClass,
            final Class<?> givenInterface) {
        if (!givenInterface.isAssignableFrom(possibleExtendingClass)) {
            // not possible
            return null;
        }

        // assignable, find the interface
        Class<?> viewClass = possibleExtendingClass;
        while (viewClass != null) {
            final Class<?>[] interfaces = viewClass.getInterfaces();
            for (final Class<?> clazz : interfaces) {
                if (givenInterface.isAssignableFrom(clazz)) {
                    return clazz;
                }
            }

            // check super
            viewClass = viewClass.getSuperclass();
        }

        // should never happen
        return null;
    }
}
