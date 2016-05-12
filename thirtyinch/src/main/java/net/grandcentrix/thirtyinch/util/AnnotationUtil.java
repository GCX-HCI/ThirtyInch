package net.grandcentrix.thirtyinch.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class AnnotationUtil {

    public static Class<?> getInterfaceOfClassExtendingGivenInterface(
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

    public static <T extends Annotation> boolean hasObjectMethodWithAnnotation(final Object o,
            final Class<T> annotation) {
        final Class<?>[] interfaces = o.getClass().getInterfaces();
        for (final Class<?> anInterface : interfaces) {
            for (final Method method : anInterface.getMethods()) {
                if (method.getAnnotation(annotation) != null) {
                    return true;
                }
            }

        }
        return false;
    }
}
