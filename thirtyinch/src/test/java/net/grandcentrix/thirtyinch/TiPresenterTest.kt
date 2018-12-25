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

import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import net.grandcentrix.thirtyinch.test.TiTestPresenter
import org.assertj.core.api.Assertions.*
import org.junit.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * @author jannisveerkamp
 * @since 11.07.16.
 */
class TiPresenterTest {

    private lateinit var presenter: TiMockPresenter

    @MockK
    lateinit var view: TiView

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        presenter = TiMockPresenter()
    }

    @Test
    fun `attach different view`() {
        val viewOverride = mockk<TiView>()
        presenter.create()
        presenter.attachView(view)
        assertThat(presenter.view).isEqualTo(view)

        try {
            presenter.attachView(viewOverride)
            fail("no exception thrown")
        } catch (e: IllegalStateException) {
            assertThat(e).hasMessageContaining("detachView")
        }
    }

    @Test
    fun `attach same view twice`() {
        presenter.create()

        presenter.attachView(view)
        assertThat(presenter.view).isEqualTo(view)

        presenter.attachView(view)
        assertThat(presenter.view).isEqualTo(view)
    }

    @Test
    fun `attach view to destroyed presenter`() {
        presenter.create()
        presenter.destroy()

        try {
            presenter.attachView(view)
            fail("no exception thrown")
        } catch (e: IllegalStateException) {
            assertThat(e).hasMessageContaining("terminal state")
        }
    }

    @Test
    fun `attach without initialize`() {
        try {
            presenter.attachView(view)
            fail("no exception thrown")
        } catch (e: IllegalStateException) {
            assertThat(e).hasMessageContaining("create()")
        }
    }

    @Test
    fun `destroy presenter with attached view`() {
        presenter.create()
        presenter.attachView(view)

        try {
            presenter.destroy()
            fail("error expected")
        } catch (e: IllegalStateException) {
            assertThat(e)
                    .hasMessageContaining("attached")
                    .hasMessageContaining("detachView()")
        }
    }

    @Test
    fun `destroy without attaching view`() {
        presenter.create()
        presenter.destroy()
    }

    @Test
    fun `detach view`() {
        presenter.create()
        assertThat(presenter.view).isNull()

        val view = mockk<TiView>()
        presenter.attachView(view)
        assertThat(presenter.view).isEqualTo(view)

        presenter.detachView()
        assertThat(presenter.view).isNull()
    }

    @Test
    fun `detach view not attached`() {
        presenter.create()

        // no exception, just ignoring
        presenter.detachView()

        presenter.attachView(mockk())
        // no exception, just ignoring
        presenter.detachView()
        presenter.detachView()
    }

    @Test
    fun `test calling onAttachView directly`() {
        try {
            presenter.onAttachView(mockk())
            fail("no exception thrown")
        } catch (e: IllegalAccessError) {
            assertThat(e)
                    .hasMessageContaining("attachView(TiView)")
                    .hasMessageContaining("#onAttachView(TiView)")
        }
    }

    @Test
    fun `test calling onCreate directly`() {
        try {
            presenter.onCreate()
            fail("no exception thrown")
        } catch (e: IllegalAccessError) {
            assertThat(e)
                    .hasMessageContaining("create()")
                    .hasMessageContaining("#onCreate()")
        }
    }

    @Test
    fun `test calling onDestroy directly`() {
        try {
            presenter.onDestroy()
            fail("no exception thrown")
        } catch (e: IllegalAccessError) {
            assertThat(e)
                    .hasMessageContaining("destroy()")
                    .hasMessageContaining("#onDestroy()")
        }
    }

    @Test
    fun `test calling onDetachView directly`() {
        try {
            presenter.onDetachView()
            fail("no exception thrown")
        } catch (e: IllegalAccessError) {
            assertThat(e)
                    .hasMessageContaining("detachView()")
                    .hasMessageContaining("#onDetachView()")
        }
    }

    @Test
    fun `test calling onSleep directly`() {
        try {
            presenter.onSleep()
            fail("no exception thrown")
        } catch (e: IllegalAccessError) {
            assertThat(e)
                    .hasMessageContaining("detachView()")
                    .hasMessageContaining("#onSleep()")
        }
    }

    @Test
    fun `test calling onWakeUp directly`() {
        try {
            presenter.onWakeUp()
            fail("no exception thrown")
        } catch (e: IllegalAccessError) {
            assertThat(e)
                    .hasMessageContaining("attachView(TiView)")
                    .hasMessageContaining("#onWakeUp()")
        }
    }

    @Test
    fun `test create`() {
        assertThat(presenter.onCreateCalled).isEqualTo(0)
        presenter.create()
        assertThat(presenter.onCreateCalled).isEqualTo(1)

        // onCreate can only be called once
        presenter.create()
        assertThat(presenter.onCreateCalled).isEqualTo(1)
    }

    @Test(expected = SuperNotCalledException::class)
    fun `test create super not called`() {
        val presenter = object : TiPresenter<TiView>() {
            override fun onCreate() {
                // Intentionally not calling super.onCreate()
            }
        }
        presenter.create()
    }

    @Test
    fun `test destroy`() {
        presenter.create()

        assertThat(presenter.onDestroyCalled).isEqualTo(0)
        presenter.destroy()
        assertThat(presenter.onDestroyCalled).isEqualTo(1)

        presenter.destroy()
        assertThat(presenter.onDestroyCalled).isEqualTo(1)
    }

    @Test
    fun `test destroy create not called`() {
        assertThat(presenter.onDestroyCalled).isEqualTo(0)
        presenter.destroy()
        assertThat(presenter.onDestroyCalled).isEqualTo(0)
    }

    @Test(expected = SuperNotCalledException::class)
    fun `test destroy super not called`() {
        val presenter = object : TiPresenter<TiView>() {
            override fun onDestroy() {
                // Intentionally not calling super.onDestroy()
            }
        }
        presenter.create()
        presenter.destroy()
    }

    @Test
    fun `test getViewOrThrow`() {
        presenter.create()
        presenter.attachView(view)
        presenter.detachView()

        try {
            presenter.viewOrThrow
            failBecauseExceptionWasNotThrown(IllegalStateException::class.java)
        } catch (e: IllegalStateException) {
            assertThat(e).hasMessage("The view is currently not attached. Use 'sendToView(ViewAction)' instead.")
        }
    }

    @Test
    fun `test getViewOrThrow returns view`() {
        presenter.create()
        presenter.attachView(view)
        assertThat(presenter.viewOrThrow).isEqualTo(view)
    }

    @Test
    fun `test getView returns null`() {
        presenter.create()
        presenter.attachView(view)
        presenter.detachView()
        assertThat(presenter.view).isNull()
    }

    @Test
    fun `test getView returns view`() {
        presenter.create()
        presenter.attachView(view)
        assertThat(presenter.view).isEqualTo(view)
    }

    @Test
    fun `test missing ui executor and detached view`() {
        val presenter = object : TiPresenter<TiView>() {}

        try {
            presenter.runOnUiThread(mockk())
        } catch (e: IllegalStateException) {
            assertThat(e)
                    .hasMessageContaining("view")
                    .hasMessageContaining("no executor")
        }
    }

    @Test
    fun `test missing ui executor attached view`() {
        val presenter = object : TiPresenter<TiView>() {}
        presenter.create()
        presenter.attachView(mockk())

        try {
            presenter.runOnUiThread(mockk())
        } catch (e: IllegalStateException) {
            assertThat(e.message).doesNotContain("view")
            assertThat(e).hasMessageContaining("no ui thread executor")
        }
    }

    @Test
    fun `test onAttachView super not called`() {
        val presenter = object : TiPresenter<TiView>() {
            override fun onAttachView(view: TiView) {
                // Intentionally not calling super.onSleep()
            }
        }
        presenter.create()

        try {
            presenter.attachView(mockk())
            fail("no exception thrown")
        } catch (e: SuperNotCalledException) {
            assertThat(e).hasMessageContaining("super.onAttachView(TiView)")
        }
    }

    @Test
    fun `test onDetachView super not called`() {
        val presenter = object : TiPresenter<TiView>() {
            override fun onDetachView() {
                // Intentionally not calling super.onSleep()
            }
        }
        presenter.create()
        presenter.attachView(mockk())

        try {
            presenter.detachView()
            fail("no exception thrown")
        } catch (e: SuperNotCalledException) {
            assertThat(e).hasMessageContaining("super.onDetachView()")
        }
    }

    @Test
    fun `test run on ui executor`() {
        // Given a presenter with executor (single thread)
        val presenter = object : TiPresenter<TiView>() {}
        presenter.create()

        val executor = Executors.newSingleThreadExecutor { Thread(it, "test ui thread") }
        presenter.setUiThreadExecutor(executor)
        presenter.attachView(mockk())

        val testThread = Thread.currentThread()

        // When scheduling work to the UI thread
        val latch = CountDownLatch(1)

        presenter.runOnUiThread {
            // Then the work gets executed on the correct thread
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
    fun `test sleep super not called`() {
        val presenter = object : TiPresenter<TiView>() {
            override fun onSleep() {
                // Intentionally not calling super.onSleep()
            }
        }
        presenter.create()
        presenter.attachView(mockk())

        try {
            presenter.detachView()
            fail("no exception thrown")
        } catch (e: SuperNotCalledException) {
            assertThat(e).hasMessageContaining("super.onSleep()")
        }
    }

    @Test
    fun `test test should return TiTestPresenter`() {
        val test = presenter.test()

        assertThat(test).isInstanceOf(TiTestPresenter::class.java)
    }

    @Test
    fun `test toString`() {
        presenter.create()
        assertThat(presenter.toString())
                .contains("TiMockPresenter")
                .contains("{view = null}")
        presenter.attachView(view)
        assertThat(presenter.toString())
                .contains("TiMockPresenter")
                .contains("{view = TiView")
    }

    @Test
    fun `test wakeUp super not called`() {
        val presenter = object : TiPresenter<TiView>() {
            override fun onWakeUp() {
                // Intentionally not calling super.onWakeup()
            }
        }
        presenter.create()

        try {
            presenter.attachView(mockk())
            fail("no exception thrown")
        } catch (e: SuperNotCalledException) {
            assertThat(e).hasMessageContaining("super.onWakeUp()")
        }
    }
}