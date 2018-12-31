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

package net.grandcentrix.thirtyinch.logginginterceptor

import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import net.grandcentrix.thirtyinch.TiLog
import net.grandcentrix.thirtyinch.TiView
import org.assertj.core.api.Assertions.*
import org.junit.*
import org.junit.runner.*
import org.junit.runners.*
import java.util.regex.Pattern

@RunWith(JUnit4::class)
class LoggingInterceptorTest {

    private open class BaseActivity : MyView

    private class MyActivity : BaseActivity()

    private class TestViewImpl : TestView {

        override fun doSomething() {
            // stub
        }

        override fun singleArg(arg: Any?) {
            // stub
        }

        override fun throwUnexpected() {
            throw RuntimeException("Unexpected")
        }

        override fun twoArgs(arg1: Any, arg2: Any) {
            // stub
        }

        override fun varargs(vararg args: Any?) {
            // stub
        }
    }

    private interface MyView : TiView

    private interface TestView : TiView {

        fun doSomething()

        fun singleArg(arg: Any?)

        fun throwUnexpected()

        fun twoArgs(arg1: Any, arg2: Any)

        fun varargs(vararg args: Any?)
    }

    @Test
    fun `test crop long params`() {

        val logger = mockk<TiLog.Logger>(relaxUnitFun = true)
        val loggingInterceptor = LoggingInterceptor(logger)
        val view: TestView = loggingInterceptor.intercept(TestViewImpl())

        val msgSlot = slot<String>()

        val maxArg = "0123456789".repeat(24)

        view.twoArgs(maxArg + "too long", "B")
        verify { logger.log(any(), any(), capture(msgSlot)) }

        assertThat(msgSlot.captured)
                .doesNotContain("too long")
                .isEqualTo("twoArgs($maxArg…, B)")
    }

    @Test
    fun `test do not log object invocations`() {

        val logger = mockk<TiLog.Logger>()
        val loggingInterceptor = LoggingInterceptor(logger)
        val view: TestView = loggingInterceptor.intercept(TestViewImpl())

        view.hashCode()
        view.toString()
        view.javaClass

        verify(exactly = 0) { logger.log(any(), any(), any()) }
    }

    @Test
    fun `test find TiView interface in complex structure`() {

        val interceptor = LoggingInterceptor()

        val interceptView: TiView = interceptor.intercept(MyActivity())

        assertThat(interceptView)
                .isInstanceOf(TiView::class.java)
                .isInstanceOf(MyView::class.java)
                .isNotInstanceOf(MyActivity::class.java)
                .isNotInstanceOf(BaseActivity::class.java)
    }

    @Test
    fun `test log array`() {

        val logger = mockk<TiLog.Logger>(relaxUnitFun = true)
        val loggingInterceptor = LoggingInterceptor(logger)
        val view: TestView = loggingInterceptor.intercept(TestViewImpl())

        val msgSlot = slot<String>()

        val array = arrayOf("Buenos Aires", "Córdoba", "La Plata")
        view.twoArgs(array, "B")
        verify { logger.log(any(), any(), capture(msgSlot)) }

        assertThat(msgSlot.captured).matches(
                Pattern.compile("""twoArgs\(\{String\[]\[3]@[\da-f]{1,8}} \[Buenos Aires, Córdoba, La Plata], B\)""")
        )
    }

    @Test
    fun `test log array containing null`() {

        val logger = mockk<TiLog.Logger>(relaxUnitFun = true)
        val loggingInterceptor = LoggingInterceptor(logger)
        val view: TestView = loggingInterceptor.intercept(TestViewImpl())

        val msgSlot = slot<String>()

        val array: Array<Any?> = arrayOf(null)
        view.twoArgs(array, "B")
        verify { logger.log(any(), any(), capture(msgSlot)) }

        assertThat(msgSlot.captured).matches(
                Pattern.compile("""twoArgs\(\{Object\[]\[1]@[\da-f]{1,8}} \[null], B\)""")
        )
    }

    @Test
    fun `test log empty list`() {

        val logger = mockk<TiLog.Logger>(relaxUnitFun = true)
        val loggingInterceptor = LoggingInterceptor(logger)
        val view: TestView = loggingInterceptor.intercept(TestViewImpl())

        val msgSlot = slot<String>()

        view.twoArgs(ArrayList<Any>(), "B")
        verify { logger.log(any(), any(), capture(msgSlot)) }

        assertThat(msgSlot.captured)
                .matches(Pattern.compile("""twoArgs\(\{ArrayList\[0]@[\da-f]{1,8}} \[], B\)"""))
    }

    @Test
    fun `test log lists`() {

        val logger = mockk<TiLog.Logger>(relaxUnitFun = true)
        val loggingInterceptor = LoggingInterceptor(logger)
        val view: TestView = loggingInterceptor.intercept(TestViewImpl())

        val msgSlot = slot<String>()

        val list = listOf("Buenos Aires", "Córdoba", "La Plata")
        view.twoArgs(list, "B")
        verify { logger.log(any(), any(), capture(msgSlot)) }

        assertThat(msgSlot.captured).matches(
                Pattern.compile("""twoArgs\(\{ArrayList\[3]@[\da-f]{1,8}} \[Buenos Aires, Córdoba, La Plata], B\)""")
        )
    }

    @Test
    fun `test log list containing null`() {

        val logger = mockk<TiLog.Logger>(relaxUnitFun = true)
        val loggingInterceptor = LoggingInterceptor(logger)
        val view: TestView = loggingInterceptor.intercept(TestViewImpl())

        val msgSlot = slot<String>()

        // listOf() would create SingletonList
        val list: List<Any?> = arrayListOf(null)
        view.twoArgs(list, "B")
        verify { logger.log(any(), any(), capture(msgSlot)) }

        assertThat(msgSlot.captured).matches(
                Pattern.compile("""twoArgs\(\{ArrayList\[1]@[\da-f]{1,8}} \[null], B\)""")
        )
    }

    @Test
    fun `test log multiple arguments`() {

        val logger = mockk<TiLog.Logger>(relaxUnitFun = true)
        val loggingInterceptor = LoggingInterceptor(logger)
        val view: TestView = loggingInterceptor.intercept(TestViewImpl())

        val msgSlot = slot<String>()

        view.twoArgs("A", "B")
        verify { logger.log(any(), any(), capture(msgSlot)) }

        assertThat(msgSlot.captured).isEqualTo("twoArgs(A, B)")
    }

    @Test
    fun `test log null`() {

        val logger = mockk<TiLog.Logger>(relaxUnitFun = true)
        val loggingInterceptor = LoggingInterceptor(logger)
        val view: TestView = loggingInterceptor.intercept(TestViewImpl())

        val msgSlot = slot<String>()

        view.singleArg(null)
        verify { logger.log(any(), any(), capture(msgSlot)) }

        assertThat(msgSlot.captured).isEqualTo("singleArg(null)")
    }

    @Test
    fun `test log null varargs`() {

        val logger = mockk<TiLog.Logger>(relaxUnitFun = true)
        val loggingInterceptor = LoggingInterceptor(logger)
        val view: TestView = loggingInterceptor.intercept(TestViewImpl())

        val msgSlot = slot<String>()

        view.varargs(null)
        verify { logger.log(any(), any(), capture(msgSlot)) }

        assertThat(msgSlot.captured).matches(
                Pattern.compile("""varargs\(\{Object\[]\[1]@[\da-f]{1,8}} \[null]\)"""))
    }

    @Test
    fun `test log varargs`() {

        val logger = mockk<TiLog.Logger>(relaxUnitFun = true)
        val loggingInterceptor = LoggingInterceptor(logger)
        val view: TestView = loggingInterceptor.intercept(TestViewImpl())

        val msgSlot = slot<String>()

        view.varargs("Buenos Aires", "Córdoba", "La Plata")
        verify { logger.log(any(), any(), capture(msgSlot)) }

        assertThat(msgSlot.captured).matches(
                Pattern.compile("""varargs\(\{Object\[]\[3]@[\da-f]{1,8}} \[Buenos Aires, Córdoba, La Plata]\)""")
        )
    }

    @Test
    fun `test log void methods`() {

        val logger = mockk<TiLog.Logger>(relaxUnitFun = true)
        val loggingInterceptor = LoggingInterceptor(logger)
        val view: TestView = loggingInterceptor.intercept(TestViewImpl())

        val msgSlot = slot<String>()

        view.doSomething()
        verify { logger.log(any(), any(), capture(msgSlot)) }

        assertThat(msgSlot.captured).isEqualTo("doSomething()")
    }

    @Test
    fun `test logger noop do not wrap`() {
        val interceptor = LoggingInterceptor(TiLog.NOOP)
        val view = mockk<TiView>()
        val interceptView = interceptor.intercept(view)
        assertThat(interceptView).isEqualTo(view)
    }

    @Test
    fun `test logger null do not wrap`() {
        val interceptor = LoggingInterceptor(null)
        val view = mockk<TiView>()
        val interceptView = interceptor.intercept(view)
        assertThat(interceptView).isEqualTo(view)
    }

    @Test
    fun `test logging disabled wrap`() {
        val interceptor = LoggingInterceptor()
        val view = mockk<TiView>()
        val interceptView = interceptor.intercept(view)
        assertThat(interceptView).isNotEqualTo(view).isNotSameAs(view)
    }

    @Test
    fun `test report errors correctly`() {

        val logger = mockk<TiLog.Logger>(relaxUnitFun = true)
        val loggingInterceptor = LoggingInterceptor(logger)
        val view: TestView = loggingInterceptor.intercept(TestViewImpl())

        val msgSlot = slot<String>()

        try {
            view.throwUnexpected()
            fail("did not throw")
        } catch (e: RuntimeException) {
            assertThat(e).hasMessage("Unexpected")
        }

        verify { logger.log(any(), any(), capture(msgSlot)) }

        // make sure logging happened before the method was called
        assertThat(msgSlot.captured).isEqualTo("throwUnexpected()")
    }
}