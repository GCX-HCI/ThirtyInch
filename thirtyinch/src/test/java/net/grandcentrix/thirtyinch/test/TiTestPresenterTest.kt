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

package net.grandcentrix.thirtyinch.test

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.verify
import junit.framework.Assert.fail
import net.grandcentrix.thirtyinch.TiPresenter
import net.grandcentrix.thirtyinch.TiPresenter.State
import net.grandcentrix.thirtyinch.TiView
import org.assertj.core.api.Assertions.*
import org.junit.*

class TiTestPresenterTest {

    interface MockTiView : TiView {

        fun helloWorld()
    }

    @RelaxedMockK
    lateinit var mockPresenter: TiPresenter<TiView>

    private lateinit var mockTiPresenter: TiPresenter<MockTiView>

    @MockK
    lateinit var mockView: TiView

    @RelaxedMockK
    lateinit var mockTiView: MockTiView

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockTiPresenter = object : TiPresenter<MockTiView>() {

            override fun onAttachView(view: MockTiView) {
                super.onAttachView(view)
                sendToView { it.helloWorld() }
            }
        }.apply { create() }
    }

    @Test
    fun `test attachView should replace UiThreadExecutor`() {

        // Given the presenter is currently in the state VIEW_DETACHED.
        every { mockPresenter.state } returns State.VIEW_DETACHED

        val tiTestPresenter = TiTestPresenter(mockPresenter)

        // When a new View is attached to the TiTestPresenter.
        tiTestPresenter.attachView(mockView)

        // Then the TiTestPresenter should set any ui thread executor on the Presenter.
        verify { mockPresenter.setUiThreadExecutor(any()) }

        // And then the TiTestPresenter should attach the new View to the Presenter.
        verify { mockPresenter.attachView(mockView) }
    }

    @Test
    fun `test attachView with attached view should detach previous view`() {

        // Given the presenter is currently in the state VIEW_ATTACHED.
        every { mockPresenter.state } returns State.VIEW_ATTACHED

        val tiTestPresenter = TiTestPresenter(mockPresenter)

        // When a new View is attached to the TiTestPresenter.
        tiTestPresenter.attachView(mockTiView)

        // Then the TiTestPresenter should call detachView() on the Presenter.
        verify { mockPresenter.detachView() }
    }

    @Test
    fun `test sendToView in unit test with TiTestPresenter should not throw`() {
        val testPresenter = TiTestPresenter(mockTiPresenter)
        testPresenter.attachView(mockTiView)

        verify { mockTiView.helloWorld() }
    }

    @Test
    fun `test sendToView in unit test should throw`() {
        try {
            mockTiPresenter.attachView(mockTiView)
            fail("No exception")
        } catch (e: IllegalStateException) {
            assertThat(e.message).contains("no ui thread executor available")
        }
    }

    @Test
    fun `test simple view invocation with test presenter`() {
        val presenter = object : TiPresenter<MockTiView>() {
            override fun onAttachView(view: MockTiView) {
                super.onAttachView(view)
                sendToView { it.helloWorld() }
            }
        }
        val testPresenter = presenter.test()

        val view = mockk<MockTiView>(relaxUnitFun = true)
        testPresenter.attachView(view)

        verify { view.helloWorld() }
    }
}