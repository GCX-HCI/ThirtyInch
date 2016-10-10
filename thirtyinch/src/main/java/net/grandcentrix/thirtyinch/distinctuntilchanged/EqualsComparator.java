/*
 * Copyright (C) 2016 grandcentrix GmbH
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

import java.util.Arrays;

/**
 * A {@link DistinctComparator} implementation which uses the {@link Object#equals(Object)}
 * method of the parameters to detect changes. It holds a strong reference to the previously use
 * parameters for a real {@link Object#equals(Object)} comparison.
 * <p>
 * The strong reference to the previous parameters could cause problems when dealing with big
 * data blobs such as bitmaps. Consider using a simpler comparator like {@link HashComparator} or
 * {@link WeakEqualsComparator}.
 */
public class EqualsComparator implements DistinctComparator {

    private Object[] mLastParameters;

    @Override
    public boolean compareWith(final Object[] newParameters) {
        if (!Arrays.equals(newParameters, mLastParameters)) {
            mLastParameters = newParameters;
            return false;
        }
        return true;
    }
}
