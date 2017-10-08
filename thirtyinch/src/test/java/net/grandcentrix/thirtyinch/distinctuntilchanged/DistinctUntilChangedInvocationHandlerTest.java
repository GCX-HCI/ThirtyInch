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

import java.lang.reflect.Method;
import net.grandcentrix.thirtyinch.TiView;
import org.junit.*;

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

    @Before
    public void setUp() {
        ducView = new TestView();
        handler = new DistinctUntilChangedInvocationHandler<>(ducView);
    }

    @Test
    public void testCallNonTiViewMethods() throws Throwable {

        assertThat(ducView.something).isEqualTo(0);
        final Method method = ducView.getClass().getMethod("doSomething", String.class);
        handler.handleInvocation(null, method, new Object[]{"test"});
        assertThat(ducView.something).isEqualTo(1);
        handler.handleInvocation(null, method, new Object[]{"test"});
        assertThat(ducView.something).isEqualTo(2);
    }

    @Test
    public void testDirectlyCallNonVoid() throws Throwable {
        final Method method = ducView.getClass().getMethod("returnValueOneArg", String.class);
        assertThat(handler.handleInvocation(null, method, new Object[]{"test"}))
                .isEqualTo("success1");
        assertThat(handler.handleInvocation(null, method, new Object[]{"test"}))
                .isEqualTo("success2");
    }

    @Test
    public void testDirectlyCallNotAnnotatedMethods() throws Throwable {
        assertThat(ducView.notAnnotated).isEqualTo(0);
        final Method method = ducView.getClass().getMethod("notAnnotated", String.class);
        handler.handleInvocation(null, method, new Object[]{"hi"});
        assertThat(ducView.notAnnotated).isEqualTo(1);
        handler.handleInvocation(null, method, new Object[]{"hi"});
        assertThat(ducView.notAnnotated).isEqualTo(2);
    }

    @Test
    public void testDirectlyCallObjectMethods() throws Throwable {
        final Method method = ducView.getClass().getMethod("toString");
        assertThat((String) handler.handleInvocation(null, method, new Object[]{}))
                .contains(TestView.class.getSimpleName());
    }

    @Test
    public void testDirectlyForwardCallsWithZeroArguments() throws Throwable {
        assertThat(ducView.zeroArgsCount).isEqualTo(0);
        final Method method = ducView.getClass().getMethod("zeroArgs");
        handler.handleInvocation(null, method, new Object[]{});
        assertThat(ducView.zeroArgsCount).isEqualTo(1);
        handler.handleInvocation(null, method, new Object[]{});
        assertThat(ducView.zeroArgsCount).isEqualTo(2);
    }

    @Test
    public void testMethodsForwardExceptions() throws Throwable {
        final Method method = ducView.getClass().getMethod("throwing", String.class);

        try {
            handler.handleInvocation(null, method, new Object[]{"Blubb123"});
            fail("did not throw");
        } catch (IllegalStateException e) {
            assertThat(e).hasMessage("Blubb123");
        }
    }

    @Test
    public void testShouldCallMethodAfterClearCache() throws Throwable {
        //given
        final Method method = ducView.getClass().getMethod("ducMethod", String.class);
        Object[] args = new Object[]{"p1"};

        //when
        handler.handleInvocation(null, method, args);
        handler.handleInvocation(null, method, args);
        assertThat(ducView.callCount).isEqualTo(1);

        //simulate reference dropped
        handler.clearCache();

        // should be called again
        handler.handleInvocation(null, method, new Object[]{"new param"});

        //then
        assertThat(ducView.callCount).isEqualTo(2);
    }

    @Test
    public void testShouldCallMethodOnce() throws Throwable {
        //given
        final Method method = ducView.getClass().getMethod("ducMethod", String.class);

        //when
        handler.handleInvocation(null, method, new Object[]{"test string 1"});
        handler.handleInvocation(null, method, new Object[]{"test string 1"});

        //then
        assertThat(ducView.callCount).isEqualTo(1);
    }

    @Test
    public void testShouldCallMethodTwice() throws Throwable {
        //given
        final Method method = ducView.getClass().getMethod("ducMethod", String.class);

        //when
        handler.handleInvocation(null, method, new Object[]{"test string 1"});
        handler.handleInvocation(null, method, new Object[]{"test string 2"});
        handler.handleInvocation(null, method, new Object[]{"test string 2"});

        //then
        assertThat(ducView.callCount).isEqualTo(2);
    }

    @Test
    public void testSwallowCall() throws Throwable {
        final Method method = ducView.getClass().getMethod("logDropped", String.class);

        assertThat(ducView.callCount).isEqualTo(0);
        handler.handleInvocation(null, method, new Object[]{"test string"});
        assertThat(ducView.callCount).isEqualTo(1);

        handler.handleInvocation(null, method, new Object[]{"test string"});
        assertThat(ducView.callCount).isEqualTo(1);
    }

    @Test
    public void testThrowWhenInitializingABadComparator() throws Throwable {
        final Method method = ducView.getClass().getMethod("badComparator", String.class);

        try {
            handler.handleInvocation(null, method, new Object[]{"test"});
            fail("did not throw");
        } catch (IllegalStateException e) {
            assertThat(e)
                    .hasMessageContaining("true")
                    .hasMessageContaining("initialization");
        }
    }
}