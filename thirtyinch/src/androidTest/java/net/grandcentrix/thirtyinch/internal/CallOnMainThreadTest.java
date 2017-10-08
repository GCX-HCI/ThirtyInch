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

package net.grandcentrix.thirtyinch.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import net.grandcentrix.thirtyinch.TiView;
import net.grandcentrix.thirtyinch.callonmainthread.CallOnMainThread;
import net.grandcentrix.thirtyinch.callonmainthread.CallOnMainThreadInterceptor;
import org.junit.*;

public class CallOnMainThreadTest {

    private interface TestView extends TiView {

        // at least one annotated method is required for the wrapping to work
        @CallOnMainThread
        void annotatedMethod();

        void throwingMethod();
    }

    @Test
    public void testForwardException() throws Exception {

        final CallOnMainThreadInterceptor interceptor = new CallOnMainThreadInterceptor();

        final TestView testView = new TestView() {
            @Override
            public void annotatedMethod() {

            }

            @Override
            public void throwingMethod() {
                throw new IllegalStateException("myException");
            }
        };

        try {
            testView.throwingMethod();
            fail("not thrown");
        } catch (IllegalStateException e) {
            assertThat(e).hasMessage("myException");
        }

        final TestView wrappedView = interceptor.intercept(testView);

        try {
            wrappedView.throwingMethod();
            fail("not thrown");
        } catch (IllegalStateException e) {
            e.printStackTrace();
            assertThat(e).hasMessage("myException");
        }

    }
}
