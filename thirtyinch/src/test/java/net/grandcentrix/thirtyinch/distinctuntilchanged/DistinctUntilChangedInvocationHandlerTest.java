package net.grandcentrix.thirtyinch.distinctuntilchanged;

import net.grandcentrix.thirtyinch.TiView;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;

public class DistinctUntilChangedInvocationHandlerTest {

    private static class TestView implements TiView {
        int callCount;
        @DistinctUntilChanged
        public void ducMethod(String param) {
            callCount++;
        }
    }

    TestView ducView;
    DistinctUntilChangedInvocationHandler<TestView> handler;

    @Before
    public void setUp() {
        ducView = new TestView();
        handler = new DistinctUntilChangedInvocationHandler<>(ducView);
    }


    @Test
    public void shouldCallMethodTwice() throws Throwable {
        //given
        Method method = ducView.getClass().getMethod("ducMethod", String.class);

        //when
        handler.handleInvocation(null, method, new Object[]{"test string 1"});
        handler.handleInvocation(null, method, new Object[]{"test string 2"});
        handler.handleInvocation(null, method, new Object[]{"test string 2"});

        //then
        assertEquals(2, ducView.callCount);
    }


    @Test
    public void shouldCallMethodOnce() throws Throwable {
        //given
        Method method = ducView.getClass().getMethod("ducMethod", String.class);

        //when
        handler.handleInvocation(null, method, new Object[]{"test string 1"});
        handler.handleInvocation(null, method, new Object[]{"test string 1"});

        //then
        assertEquals(1, ducView.callCount);
    }

}