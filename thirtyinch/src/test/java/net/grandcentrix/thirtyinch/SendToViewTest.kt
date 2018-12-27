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

package net.grandcentrix.thirtyinch

import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifySequence
import org.assertj.core.api.Assertions.*
import org.junit.*
import org.mockito.Mockito.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class SendToViewTest {

    private lateinit var immediatelySameThread: Executor

    private class TestPresenter : TiPresenter<TestView>()

    private interface TestView : TiView {

        fun doSomething1()

        fun doSomething2()

        fun doSomething3()
    }

    @Before
    fun setUp() {
        immediatelySameThread = Executor { it.run() }
    }

    @Test
    fun `sendToView in order`() {
        val presenter = TestPresenter()
        presenter.create()
        presenter.setUiThreadExecutor(immediatelySameThread)
        assertThat(presenter.queuedViewActions).isEmpty()

        presenter.sendToView { it.doSomething3() }
        presenter.sendToView { it.doSomething1() }
        presenter.sendToView { it.doSomething2() }
        assertThat(presenter.queuedViewActions).hasSize(3)

        val view = mockk<TestView>(relaxUnitFun = true)
        presenter.attachView(view)

        assertThat(presenter.queuedViewActions).isEmpty()

        verifySequence {
            view.doSomething3()
            view.doSomething1()
            view.doSomething2()
        }
    }

    @Test
    fun `test sendToView runs on the main thread`() {
        // Given a presenter with executor (single thread)
        val presenter = object : TiPresenter<TiView>() {}
        presenter.create()

        val executor = Executors.newSingleThreadExecutor { Thread(it, "test ui thread") }
        presenter.setUiThreadExecutor(executor)
        presenter.attachView(mockk())

        val testThread = Thread.currentThread()

        // When send work to the view
        val latch = CountDownLatch(1)

        presenter.sendToView {
            // Then the work gets executed on the ui thread
            val currentThread = Thread.currentThread()
            assertThat(testThread).isNotSameAs(currentThread)
            assertThat("test ui thread")
                    .`as`("executed on wrong thread")
                    .isEqualTo(currentThread.name)
            latch.countDown()
        }

        // wait a reasonable amount of time for the thread to execute the work
        latch.await(5, TimeUnit.SECONDS)
    }

    @Test
    fun `view attached`() {
        val presenter = TestPresenter()
        presenter.create()
        presenter.setUiThreadExecutor(immediatelySameThread)
        assertThat(presenter.queuedViewActions).isEmpty()

        val view = mockk<TestView>(relaxUnitFun = true)
        presenter.attachView(view)

        presenter.sendToView { it.doSomething1() }
        assertThat(presenter.queuedViewActions).isEmpty()
        verify { view.doSomething1() }
    }

    @Test
    fun `view detached`() {
        val presenter = TestPresenter()
        presenter.create()
        presenter.setUiThreadExecutor(immediatelySameThread)
        assertThat(presenter.queuedViewActions).isEmpty()

        presenter.sendToView { it.doSomething1() }
        assertThat(presenter.queuedViewActions).hasSize(1)

        val view = mockk<TestView>(relaxUnitFun = true)
        presenter.attachView(view)
        verify { view.doSomething1() }

        assertThat(presenter.queuedViewActions).isEmpty()
    }

    @Test
    fun `view receives no interactions after detaching`() {
        val presenter = TestPresenter()
        presenter.create()
        presenter.setUiThreadExecutor(immediatelySameThread)
        assertThat(presenter.queuedViewActions).isEmpty()

        val view = mock(TestView::class.java)
        presenter.attachView(view)
        presenter.detachView()

        presenter.sendToView { it.doSomething1() }
        assertThat(presenter.queuedViewActions).hasSize(1)
        verifyZeroInteractions(view)

        presenter.attachView(view)

        verify(view).doSomething1()
        assertThat(presenter.queuedViewActions).isEmpty()

        presenter.detachView()

        presenter.sendToView { it.doSomething1() }
        assertThat(presenter.queuedViewActions).hasSize(1)

        verifyNoMoreInteractions(view)
    }
}
