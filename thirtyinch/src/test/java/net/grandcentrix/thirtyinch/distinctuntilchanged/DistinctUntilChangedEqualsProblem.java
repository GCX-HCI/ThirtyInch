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

import java.util.Arrays;
import java.util.List;
import net.grandcentrix.thirtyinch.TiView;
import org.junit.*;

public class DistinctUntilChangedEqualsProblem {

    /**
     * Mutable pojo
     */
    private static class Tab {

        private String name;

        public Tab(final String name) {
            this.name = name;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Tab)) {
                return false;
            }

            final Tab tab = (Tab) o;

            return name != null ? name.equals(tab.name) : tab.name == null;

        }

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        @Override
        public int hashCode() {
            return name != null ? name.hashCode() : 0;
        }
    }

    // Impl because Mockito mocks cannot be wrapped in a proxy
    private static class TestViewImpl implements TestView {

        int equalsCalled = 0;

        int hashCalled = 0;

        @Override
        public void showTabsEquals(final List<Tab> tabs) {
            equalsCalled++;
        }

        @Override
        public void showTabsHash(final List<Tab> tabs) {
            hashCalled++;
        }
    }

    private interface TestView extends TiView {

        @DistinctUntilChanged(comparator = EqualsComparator.class)
        void showTabsEquals(final List<Tab> tabs);

        @DistinctUntilChanged(comparator = HashComparator.class)
        void showTabsHash(final List<Tab> tabs);
    }

    private Tab tab1 = new Tab("A");

    private Tab tab2 = new Tab("B");

    private Tab tab3 = new Tab("C");

    private TestViewImpl view;

    private TestView wrappedView;

    @Before
    public void setUp() {
        view = new TestViewImpl();
        final DistinctUntilChangedInterceptor interceptor = new DistinctUntilChangedInterceptor();
        wrappedView = interceptor.intercept(view);
        // check wrapping worked
        assertThat(view).isNotEqualTo(wrappedView);
    }

    @Test
    public void testMutatedObjectIsDetectedByHashCodeComparator() throws Exception {

        // call view for the first time
        final List<Tab> list1 = Arrays.asList(tab1, tab2, tab3);
        wrappedView.showTabsHash(list1);
        assertThat(view.hashCalled).isEqualTo(1);

        // mutate one tab
        tab1.setName("X");

        // create list with same tab objects
        final List<Tab> list2 = Arrays.asList(tab1, tab2, tab3);
        wrappedView.showTabsHash(list2);

        // view gets called on change
        assertThat(view.hashCalled).isEqualTo(2);
    }

    @Test
    public void testMutatedObjectIsNotDetectedByEqualsComparator() throws Exception {

        // call view for the first time
        final List<Tab> list1 = Arrays.asList(tab1, tab2, tab3);
        wrappedView.showTabsEquals(list1);
        assertThat(view.equalsCalled).isEqualTo(1);

        // mutate one tab
        tab1.setName("X");

        // create list with same tab objects
        final List<Tab> list2 = Arrays.asList(tab1, tab2, tab3);
        wrappedView.showTabsEquals(list2);

        // view doesn't get called because the objects are still the same (by reference)
        // this is expected but it's very confusing because the content has changed since the
        // last call. This happens when mutable objects are used as parameters
        assertThat(view.equalsCalled).isEqualTo(1);
    }
}
