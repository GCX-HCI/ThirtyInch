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

import android.support.annotation.VisibleForTesting;
import java.lang.ref.WeakReference;
import java.util.Arrays;

/**
 * A {@link DistinctComparator} implementation which uses the {@link Object#equals(Object)}
 * method of the parameters to detect changes. This comparator holds weak references to the
 * previous parameters compared to {@link EqualsComparator}
 */
public class WeakEqualsComparator implements DistinctComparator {

    @VisibleForTesting
    WeakReference<Object[]> mLastParameters;

    @Override
    public boolean compareWith(final Object[] newParameters) {
        if (mLastParameters == null || !Arrays.equals(newParameters, mLastParameters.get())) {
            mLastParameters = new WeakReference<>(newParameters);
            return false;
        }
        return true;
    }
}
