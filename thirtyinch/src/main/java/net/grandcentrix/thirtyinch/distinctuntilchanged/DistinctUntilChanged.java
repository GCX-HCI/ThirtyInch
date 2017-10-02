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

package net.grandcentrix.thirtyinch.distinctuntilchanged;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.grandcentrix.thirtyinch.TiView;

/**
 * When added to a {@code void} method with at least one parameter inside a {@link TiView}, the
 * method implementation will only be called when the parameters change. A
 * {@link DistinctComparator} class is used to detect changes. By default it uses
 * {@link HashComparator}.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistinctUntilChanged {

    Class<? extends DistinctComparator> comparator() default HashComparator.class;

    boolean logDropped() default false;

}
