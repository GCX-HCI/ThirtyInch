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

package net.grandcentrix.thirtyinch.logginginterceptor;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import net.grandcentrix.thirtyinch.TiLog;
import net.grandcentrix.thirtyinch.TiView;
import org.junit.*;
import org.junit.runner.*;
import org.junit.runners.*;
import org.mockito.*;

/**
 * This test class is left intentionally because of differences how Kotlin and Java treat varargs.
 * Because of that we have to test the behavior separately for both platforms.
 */
@RunWith(JUnit4.class)
public class LoggingInterceptorTestJava {

    private class TestViewImpl implements TestView {

        @Override
        public void varargs(final Object... args) {
            // stub
        }
    }

    private interface TestView extends TiView {

        void varargs(Object... args);

    }

    /**
     * In Java it is possible to pass a null array reference as a single vararg parameter.
     * In Kotlin a single value (null as well) passed as a vararg parameter will always be wrapped in an array.
     */
    @Test
    public void testLogNullVarargs() {

        final TiLog.Logger logger = mock(TiLog.Logger.class);
        final LoggingInterceptor loggingInterceptor = new LoggingInterceptor(logger);
        final TestView view = loggingInterceptor.intercept(new TestViewImpl());

        final ArgumentCaptor<String> msgCaptor = ArgumentCaptor.forClass(String.class);

        view.varargs((Object[]) null);
        verify(logger).log(anyInt(), anyString(), msgCaptor.capture());

        assertThat(msgCaptor.getValue()).isEqualTo("varargs(null)");
    }
}