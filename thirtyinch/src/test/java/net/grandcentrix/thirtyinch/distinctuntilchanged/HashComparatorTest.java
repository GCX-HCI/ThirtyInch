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

import static org.assertj.core.api.Assertions.*;

import org.junit.*;

public class HashComparatorTest {

    @Test
    public void different() throws Exception {
        final HashComparator comparator = new HashComparator();
        assertThat(comparator.compareWith(new Object[]{"arg1"})).isFalse();
        assertThat(comparator.compareWith(new Object[]{"arg2"})).isFalse();
    }

    @Test
    public void initialize() throws Exception {

        final HashComparator comparator = new HashComparator();
        assertThat(comparator.compareWith(new Object[]{"arg1"})).isFalse();
    }

    @Test
    public void same() throws Exception {
        final HashComparator comparator = new HashComparator();

        assertThat(comparator.compareWith(new Object[]{"arg1"})).isFalse();
        assertThat(comparator.compareWith(new Object[]{"arg1"})).isTrue();
        assertThat(comparator.compareWith(new Object[]{"arg1"})).isTrue();
    }

    @Test
    public void sameHashcode_differentEquals() throws Exception {
        final HashComparator comparator = new HashComparator();
        final Object arg1 = new Object() {
            @Override
            public boolean equals(final Object obj) {
                return false;
            }

            @Override
            public int hashCode() {
                return 1;
            }
        };
        final Object arg2 = new Object() {
            @Override
            public boolean equals(final Object obj) {
                return false;
            }

            @Override
            public int hashCode() {
                return 1;
            }
        };
        // equal but not same hash code
        assertThat(arg1).isNotEqualTo(arg2);
        assertThat(arg1).isNotSameAs(arg2);
        assertThat(arg1.hashCode()).isEqualTo(arg2.hashCode());

        assertThat(comparator.compareWith(new Object[]{arg1})).isFalse();
        assertThat(comparator.compareWith(new Object[]{arg2})).isTrue();
        assertThat(comparator.compareWith(new Object[]{arg1})).isTrue();
        assertThat(comparator.compareWith(new Object[]{arg2})).isTrue();
    }
}