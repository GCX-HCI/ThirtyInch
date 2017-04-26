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

import java.util.Arrays;

/**
 * A {@link DistinctComparator} implementation which uses the {@link Object#hashCode()} of the
 * parameters to detect changes. It doesn't hold a references to the previously sent parameters.
 * In theory this comparison could miss changes compared to {@link EqualsComparator} when multiple
 * mutated object accidentally return the same hashcode.
 */
public class HashComparator implements DistinctComparator {

    private int mLastParametersHash = 0;

    @Override
    public boolean compareWith(final Object[] newParameters) {
        final int hash = Arrays.hashCode(newParameters);
        if (hash == mLastParametersHash) {
            return true;
        }
        mLastParametersHash = hash;
        return false;
    }
}
