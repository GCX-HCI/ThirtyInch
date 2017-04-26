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

import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.assertj.core.api.Java6Assertions.assertThat;

public class EqualsComparatorTest {

    @Test
    public void different() throws Exception {
        final EqualsComparator comparator = new EqualsComparator();
        assertFalse(comparator.compareWith(new Object[]{"arg1"}));
        assertFalse(comparator.compareWith(new Object[]{"arg2"}));
    }

    @Test
    public void initialize() throws Exception {

        final EqualsComparator comparator = new EqualsComparator();
        assertFalse(comparator.compareWith(new Object[]{"arg1"}));
    }

    @Test
    public void same() throws Exception {
        final EqualsComparator comparator = new EqualsComparator();

        assertFalse(comparator.compareWith(new Object[]{"arg1"}));
        assertTrue(comparator.compareWith(new Object[]{"arg1"}));
        assertTrue(comparator.compareWith(new Object[]{"arg1"}));

    }

    @Test
    public void sameEquals_differentHashcode() throws Exception {
        final EqualsComparator comparator = new EqualsComparator();
        final Object arg1 = new Object() {
            @Override
            public boolean equals(final Object obj) {
                return true;
            }
        };
        final Object arg2 = new Object() {
            @Override
            public boolean equals(final Object obj) {
                return true;
            }
        };
        // equal but not same hash code
        assertThat(arg1).isEqualTo(arg2);
        assertThat(arg1.hashCode()).isNotEqualTo(arg2.hashCode());

        assertThat(comparator.compareWith(new Object[]{arg1})).isFalse();
        assertThat(comparator.compareWith(new Object[]{arg2})).isTrue();
        assertThat(comparator.compareWith(new Object[]{arg1})).isTrue();
        assertThat(comparator.compareWith(new Object[]{arg2})).isTrue();
    }

    @Test
    public void sameObject_equalsFalse_sameHashcode() throws Exception {

        final EqualsComparator comparator = new EqualsComparator();
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
        // equal but not same hash code
        assertThat(arg1).isNotEqualTo(arg1);
        assertThat(arg1).isSameAs(arg1);

        assertThat(comparator.compareWith(new Object[]{arg1})).isFalse();
        assertThat(comparator.compareWith(new Object[]{arg1})).isFalse();
        assertThat(comparator.compareWith(new Object[]{arg1})).isFalse();

    }
}