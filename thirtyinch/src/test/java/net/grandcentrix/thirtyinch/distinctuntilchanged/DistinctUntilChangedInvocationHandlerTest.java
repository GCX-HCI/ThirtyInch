package net.grandcentrix.thirtyinch.distinctuntilchanged;

import net.grandcentrix.thirtyinch.TiView;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class DistinctUntilChangedInvocationHandlerTest {

    public static class BadComparator implements DistinctComparator {

        @Override
        public boolean compareWith(final Object[] newParameters) {
            // returns true for initial state, should throw
            return true;
        }
    }

    private static class TestView extends NotTiView implements TiView {

        int callCount;

        int zeroArgsCount;

        private int notAnnotated;

        private int success;

        @DistinctUntilChanged(comparator = BadComparator.class)
        public void badComparator(String param) {

        }

        @DistinctUntilChanged
        public void ducMethod(String param) {
            callCount++;
        }

        @DistinctUntilChanged(logDropped = true)
        public void logDropped(String param) {
            callCount++;
        }

        public void notAnnotated(String param) {
            notAnnotated++;
        }

        public String returnValueOneArg(String param) {
            return "success" + (++success);
        }

        public void throwing(final String s) {
            throw new IllegalStateException(s);
        }

        public void zeroArgs() {
            zeroArgsCount++;
        }

        private void privateMethod() {

        }
    }

    private static class NotTiView {

        int something;

        // super implementation of TiView should be called directly
        public void doSomething(String param) {
            something++;
        }
    }

    private TestView ducView;

    private DistinctUntilChangedInvocationHandler<TestView> handler;

    @Test
    public void callNonTiViewMethods() throws Throwable {

        assertThat(ducView.something).isEqualTo(0);
        final Method method = ducView.getClass().getMethod("doSomething", String.class);
        handler.handleInvocation(null, method, new Object[]{"test"});
        assertThat(ducView.something).isEqualTo(1);
        handler.handleInvocation(null, method, new Object[]{"test"});
        assertThat(ducView.something).isEqualTo(2);
    }

    @Test
    public void directlyCallNonVoid() throws Throwable {
        final Method method = ducView.getClass().getMethod("returnValueOneArg", String.class);
        assertThat(handler.handleInvocation(null, method, new Object[]{"test"}))
                .isEqualTo("success1");
        assertThat(handler.handleInvocation(null, method, new Object[]{"test"}))
                .isEqualTo("success2");
    }

    @Test
    public void directlyCallNotAnnotatedMethods() throws Throwable {
        assertThat(ducView.notAnnotated).isEqualTo(0);
        final Method method = ducView.getClass().getMethod("notAnnotated", String.class);
        handler.handleInvocation(null, method, new Object[]{"hi"});
        assertThat(ducView.notAnnotated).isEqualTo(1);
        handler.handleInvocation(null, method, new Object[]{"hi"});
        assertThat(ducView.notAnnotated).isEqualTo(2);
    }

    @Test
    public void directlyCallObjectMethods() throws Throwable {
        final Method method = ducView.getClass().getMethod("toString");
        assertThat((String) handler.handleInvocation(null, method, new Object[]{}))
                .contains(TestView.class.getSimpleName());
    }

    @Test
    public void directlyForwardCallsWithZeroArguments() throws Throwable {
        assertThat(ducView.zeroArgsCount).isEqualTo(0);
        final Method method = ducView.getClass().getMethod("zeroArgs");
        handler.handleInvocation(null, method, new Object[]{});
        assertThat(ducView.zeroArgsCount).isEqualTo(1);
        handler.handleInvocation(null, method, new Object[]{});
        assertThat(ducView.zeroArgsCount).isEqualTo(2);
    }

    @Test
    public void methodsForwardExceptions() throws Throwable {
        final Method method = ducView.getClass().getMethod("throwing", String.class);

        try {
            handler.handleInvocation(null, method, new Object[]{"Blubb123"});
            fail("did not throw");
        } catch (IllegalStateException e) {
            assertThat(e).hasMessage("Blubb123");
        }
    }

    @Before
    public void setUp() {
        ducView = new TestView();
        handler = new DistinctUntilChangedInvocationHandler<>(ducView);
    }

    @Test
    public void shouldCallMethodAfterClearCache() throws Throwable {
        //given
        final Method method = ducView.getClass().getMethod("ducMethod", String.class);
        Object[] args = new Object[]{"p1"};

        //when
        handler.handleInvocation(null, method, args);
        handler.handleInvocation(null, method, args);
        assertEquals(1, ducView.callCount);

        //simulate reference dropped
        handler.clearCache();

        // should be called again
        handler.handleInvocation(null, method, new Object[]{"new param"});

        //then
        assertEquals(2, ducView.callCount);
    }

    @Test
    public void shouldCallMethodOnce() throws Throwable {
        //given
        final Method method = ducView.getClass().getMethod("ducMethod", String.class);

        //when
        handler.handleInvocation(null, method, new Object[]{"test string 1"});
        handler.handleInvocation(null, method, new Object[]{"test string 1"});

        //then
        assertEquals(1, ducView.callCount);
    }

    @Test
    public void shouldCallMethodTwice() throws Throwable {
        //given
        final Method method = ducView.getClass().getMethod("ducMethod", String.class);

        //when
        handler.handleInvocation(null, method, new Object[]{"test string 1"});
        handler.handleInvocation(null, method, new Object[]{"test string 2"});
        handler.handleInvocation(null, method, new Object[]{"test string 2"});

        //then
        assertEquals(2, ducView.callCount);
    }

    @Test
    public void swallowCall() throws Throwable {
        final Method method = ducView.getClass().getMethod("logDropped", String.class);

        assertEquals(0, ducView.callCount);
        handler.handleInvocation(null, method, new Object[]{"test string"});
        assertEquals(1, ducView.callCount);

        handler.handleInvocation(null, method, new Object[]{"test string"});
        assertEquals(1, ducView.callCount);
    }

    @Test
    public void throwWhenInitializingABadComparator() throws Throwable {
        final Method method = ducView.getClass().getMethod("badComparator", String.class);

        try {
            handler.handleInvocation(null, method, new Object[]{"test"});
            fail("did not throw");
        } catch (IllegalStateException e) {
            assertThat(e).hasMessageContaining("true")
                    .hasMessageContaining("initialization");
        }
    }
}