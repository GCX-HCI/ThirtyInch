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

package net.grandcentrix.thirtyinch.util;

import android.support.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class AnnotationUtil {

    @Nullable
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
