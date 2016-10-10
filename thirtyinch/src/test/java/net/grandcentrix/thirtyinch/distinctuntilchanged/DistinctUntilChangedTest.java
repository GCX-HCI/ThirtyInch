package net.grandcentrix.thirtyinch.distinctuntilchanged;

import net.grandcentrix.thirtyinch.TiView;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

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

        @Override
        public int hashCode() {
            return mTest != null ? mTest.hashCode() : 0;
        }
    }

    private interface TestViewHash extends TiView {

        @DistinctUntilChanged(comparator = HashComparator.class)
        void annotatedMethod(String str);
    }

    private interface TestViewEquals extends TiView {

        @DistinctUntilChanged(comparator = EqualsComparator.class)
        void annotatedMethod(String str);
    }

    private interface TestViewBadHash extends TiView {

        @DistinctUntilChanged(comparator = EqualsComparator.class)
        void annotatedMethod(BadHashObject bho);
    }

    private interface TestViewBadEquals extends TiView {

        @DistinctUntilChanged(comparator = HashComparator.class)
        void annotatedMethod(BadEqualsObject bho);
    }

    /**
     * Using {@link HashComparator} when the objects have a bad equals method works.
     */
    @Test
    public void testDistincUntilChangedBadEquals() throws Exception {
        DistinctUntilChangedInterceptor interceptor = new DistinctUntilChangedInterceptor();
        final CountWrapper counter = new CountWrapper();
        final TestViewBadEquals testView = new TestViewBadEquals() {

            @Override
            public void annotatedMethod(final BadEqualsObject beo) {
                counter.call();
            }
        };
        final TestViewBadEquals testViewWrapped = interceptor.intercept(testView);

        testViewWrapped.annotatedMethod(new BadEqualsObject("test"));
        testViewWrapped.annotatedMethod(new BadEqualsObject("test"));
        testViewWrapped.annotatedMethod(new BadEqualsObject("test2"));

        assertThat(counter.getCalled(), is(equalTo(2)));
    }

    /**
     * Using {@link EqualsComparator} when the objects have a bad hashcode method works.
     */
    @Test
    public void testDistincUntilChangedBadHash() throws Exception {
        DistinctUntilChangedInterceptor interceptor = new DistinctUntilChangedInterceptor();
        final CountWrapper counter = new CountWrapper();
        final TestViewBadHash testView = new TestViewBadHash() {

            @Override
            public void annotatedMethod(final BadHashObject bho) {
                counter.call();
            }
        };
        final TestViewBadHash testViewWrapped = interceptor.intercept(testView);

        testViewWrapped.annotatedMethod(new BadHashObject("test"));
        testViewWrapped.annotatedMethod(new BadHashObject("test"));
        testViewWrapped.annotatedMethod(new BadHashObject("test2"));

        assertThat(counter.getCalled(), is(equalTo(2)));
    }

    /**
     * Using {@link EqualsComparator} should work on well defined objects.
     */
    @Test
    public void testDistincUntilChangedEquals() throws Exception {
        DistinctUntilChangedInterceptor interceptor = new DistinctUntilChangedInterceptor();
        final CountWrapper counter = new CountWrapper();
        final TestViewEquals testView = new TestViewEquals() {

            @Override
            public void annotatedMethod(final String str) {
                counter.call();
            }
        };
        final TestViewEquals testViewWrapped = interceptor.intercept(testView);

        testViewWrapped.annotatedMethod("test");
        testViewWrapped.annotatedMethod("test");
        testViewWrapped.annotatedMethod("test2");

        assertThat(counter.getCalled(), is(equalTo(2)));
    }

    /**
     * Using {@link HashComparator} should work on well defined objects.
     */
    @Test
    public void testDistincUntilChangedHash() throws Exception {
        DistinctUntilChangedInterceptor interceptor = new DistinctUntilChangedInterceptor();
        final CountWrapper counter = new CountWrapper();
        final TestViewHash testView = new TestViewHash() {

            @Override
            public void annotatedMethod(final String str) {
                counter.call();
            }
        };
        final TestViewHash testViewWrapped = interceptor.intercept(testView);

        testViewWrapped.annotatedMethod("test");
        testViewWrapped.annotatedMethod("test");
        testViewWrapped.annotatedMethod("test2");

        assertThat(counter.getCalled(), is(equalTo(2)));
    }
}
