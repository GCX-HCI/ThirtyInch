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

/**
 * Compares the arguments of a method annotated with {@link DistinctUntilChanged}
 */
public interface DistinctComparator {

    /**
     * Checks if the arguments of a method call have changed compare to the last call of the
     * method. This method returns {@code false} when calling it for the first time. It's the
     * initialization step allowing comparisons with the next arguments.
     *
     * @param newParameters arguments of the current method call. Compare them to the last call
     *                      of {@link #compareWith(Object[])}
     * @return {@code false} if the arguments have changed compared to the last call of this
     * method, {@code true} when they are the same
     */
    boolean compareWith(final Object[] newParameters);
}
