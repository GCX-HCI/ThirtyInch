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

import net.grandcentrix.thirtyinch.TiView;
import org.junit.*;

/**
 * Testing {@link DistinctUntilChanged} annotation via {@link DistinctUntilChangedInterceptor}
 * requires interfaces extending {@link TiView}.
 */
public class DistinctUntilChangedTest {

    private class CountWrapper {

        private int mCalled = 0;

        public void call() {
            mCalled++;
        }

        public int getCalled() {
            return mCalled;
        }
    }

    /**
     * This class doesn't have a good hashcode method
     */
    private class BadHashObject {

        private final String mTest;

        public BadHashObject(final String test) {
            mTest = test;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final BadHashObject that = (BadHashObject) o;

            return mTest != null ? mTest.equals(that.mTest) : that.mTest == null;

        }

        @Override
        public int hashCode() {
            return 42;
        }
    }

    /**
     * This class doesn't have a good equals method
     */
    private class BadEqualsObject {

        private final String mTest;

        public BadEqualsObject(final String test) {
            mTest = test;
        }

        @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
        @Override
        public boolean equals(final Object o) {
            return true;
        }

        @Override
        public int hashCode() {
            return mTest != null ? mTest.hashCode() : 0;
        }
    }

    private interface TestViewHash extends TiView {

        @DistinctUntilChanged(comparator = HashComparator.class)
        void annotatedMethod(Object o);
    }

    private interface TestViewEquals extends TiView {

        @DistinctUntilChanged(comparator = EqualsComparator.class)
        void annotatedMethod(Object o);
    }

    /**
     * Using {@link EqualsComparator} should work on well defined objects.
     */
    @Test
    public void testDistinctUntilChangedEquals() throws Exception {
        DistinctUntilChangedInterceptor interceptor = new DistinctUntilChangedInterceptor();
        final CountWrapper counter = new CountWrapper();
        final TestViewEquals testView = new TestViewEquals() {

            @Override
            public void annotatedMethod(final Object o) {
                counter.call();
            }
        };
        final TestViewEquals testViewWrapped = interceptor.intercept(testView);

        testViewWrapped.annotatedMethod("test");
        testViewWrapped.annotatedMethod("test");
        testViewWrapped.annotatedMethod("test2");

        assertThat(counter.getCalled()).isEqualTo(2);
    }

    /**
     * Using {@link HashComparator} should work on well defined objects.
     */
    @Test
    public void testDistinctUntilChangedHash() throws Exception {
        DistinctUntilChangedInterceptor interceptor = new DistinctUntilChangedInterceptor();
        final CountWrapper counter = new CountWrapper();
        final TestViewHash testView = new TestViewHash() {

            @Override
            public void annotatedMethod(final Object o) {
                counter.call();
            }
        };
        final TestViewHash testViewWrapped = interceptor.intercept(testView);

        testViewWrapped.annotatedMethod("test");
        testViewWrapped.annotatedMethod("test");
        testViewWrapped.annotatedMethod("test2");

        assertThat(counter.getCalled()).isEqualTo(2);
    }

    /**
     * test custom equals implementation (always equals)
     */
    @Test
    public void testEqualsComparison_badEquals() throws Exception {
        DistinctUntilChangedInterceptor interceptor = new DistinctUntilChangedInterceptor();
        final CountWrapper counter = new CountWrapper();
        final TestViewEquals testView = new TestViewEquals() {

            @Override
            public void annotatedMethod(final Object o) {
                counter.call();
            }
        };
        final TestViewEquals testViewWrapped = interceptor.intercept(testView);

        testViewWrapped.annotatedMethod(new BadEqualsObject("test"));
        testViewWrapped.annotatedMethod(new BadEqualsObject("test"));
        testViewWrapped.annotatedMethod(new BadEqualsObject("test2"));

        assertThat(counter.getCalled()).isEqualTo(1);
    }

    /**
     * make sure the equals comparison doesn't use the hash implementation
     */
    @Test
    public void testEqualsComparison_badHash_works() throws Exception {
        DistinctUntilChangedInterceptor interceptor = new DistinctUntilChangedInterceptor();
        final CountWrapper counter = new CountWrapper();
        final TestViewEquals testView = new TestViewEquals() {

            @Override
            public void annotatedMethod(final Object o) {
                counter.call();
            }
        };
        final TestViewEquals testViewWrapped = interceptor.intercept(testView);

        testViewWrapped.annotatedMethod(new BadHashObject("test"));
        testViewWrapped.annotatedMethod(new BadHashObject("test"));
        testViewWrapped.annotatedMethod(new BadHashObject("test2"));

        assertThat(counter.getCalled()).isEqualTo(2);
    }

    /**
     * make sure the hash comparison doesn't use the equals implementation
     */
    @Test
    public void testHashComparison_badEquals_works() throws Exception {
        DistinctUntilChangedInterceptor interceptor = new DistinctUntilChangedInterceptor();
        final CountWrapper counter = new CountWrapper();
        final TestViewHash testView = new TestViewHash() {

            @Override
            public void annotatedMethod(final Object o) {
                counter.call();
            }
        };
        final TestViewHash testViewWrapped = interceptor.intercept(testView);

        testViewWrapped.annotatedMethod(new BadEqualsObject("test"));
        testViewWrapped.annotatedMethod(new BadEqualsObject("test"));
        testViewWrapped.annotatedMethod(new BadEqualsObject("test2"));

        assertThat(counter.getCalled()).isEqualTo(2);
    }

    /**
     * test custom hash implementation (always the same)
     */
    @Test
    public void testHashComparison_badHash() throws Exception {
        DistinctUntilChangedInterceptor interceptor = new DistinctUntilChangedInterceptor();
        final CountWrapper counter = new CountWrapper();
        final TestViewHash testView = new TestViewHash() {

            @Override
            public void annotatedMethod(final Object o) {
                counter.call();
            }
        };
        final TestViewHash testViewWrapped = interceptor.intercept(testView);

        testViewWrapped.annotatedMethod(new BadHashObject("test"));
        testViewWrapped.annotatedMethod(new BadHashObject("test"));
        testViewWrapped.annotatedMethod(new BadHashObject("test2"));

        assertThat(counter.getCalled()).isEqualTo(1);
    }
}
