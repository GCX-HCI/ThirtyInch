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
import io.mockk.verifySequence
import org.assertj.core.api.Assertions.*
import org.junit.*
import org.mockito.*
import org.mockito.Mockito.*

class TiLifecycleObserverTest {

    private lateinit var presenter: TiMockPresenter

    @MockK
    lateinit var view: TiView

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        TiLog.setLogger { _, _, _ ->
            // prevent RuntimeException: android.util.Log not mocked
        }
        presenter = TiMockPresenter()
    }

    @Test
    fun `test called attached in correct order`() {
        presenter.create()

        // Given 2 observers
        val observer1 = mockk<TiLifecycleObserver>(relaxUnitFun = true)
        presenter.addLifecycleObserver(observer1)
        val observer2 = mockk<TiLifecycleObserver>(relaxUnitFun = true)
        presenter.addLifecycleObserver(observer2)

        // When a view attaches
        presenter.attachView(mockk())

        // Then the last added observer gets called last
        verifySequence {
            observer1.onChange(TiPresenter.State.VIEW_ATTACHED, false)
            observer2.onChange(TiPresenter.State.VIEW_ATTACHED, false)

            observer1.onChange(TiPresenter.State.VIEW_ATTACHED, true)
            observer2.onChange(TiPresenter.State.VIEW_ATTACHED, true)
        }
    }

    @Test
    fun `test called create in correct order`() {
        // Given 2 observers
        val observer1 = mockk<TiLifecycleObserver>(relaxUnitFun = true)
        presenter.addLifecycleObserver(observer1)
        val observer2 = mockk<TiLifecycleObserver>(relaxUnitFun = true)
        presenter.addLifecycleObserver(observer2)

        // When the presenter gets created and reached view detached state
        presenter.create()

        // Then the last added observer gets called first because it's a destructive event
        verifySequence {
            observer2.onChange(TiPresenter.State.VIEW_DETACHED, false)
            observer1.onChange(TiPresenter.State.VIEW_DETACHED, false)

            observer2.onChange(TiPresenter.State.VIEW_DETACHED, true)
            observer1.onChange(TiPresenter.State.VIEW_DETACHED, true)
        }
    }

    @Test
    fun `test called destroy in correct order`() {
        // Given a presenter with 2 added observers
        presenter.create()
        val observer1 = mockk<TiLifecycleObserver>(relaxUnitFun = true)
        presenter.addLifecycleObserver(observer1)
        val observer2 = mockk<TiLifecycleObserver>(relaxUnitFun = true)
        presenter.addLifecycleObserver(observer2)

        // When the presenter gets destroyed
        presenter.destroy()

        // Then the last added observer gets called first
        verifySequence {
            observer2.onChange(TiPresenter.State.DESTROYED, false)
            observer1.onChange(TiPresenter.State.DESTROYED, false)

            observer2.onChange(TiPresenter.State.DESTROYED, true)
            observer1.onChange(TiPresenter.State.DESTROYED, true)
        }
    }

    @Test
    fun `test called detached in correct order`() {
        presenter.create()
        presenter.attachView(mockk())

        // Given 2 observers
        val observer1 = mockk<TiLifecycleObserver>(relaxUnitFun = true)
        presenter.addLifecycleObserver(observer1)
        val observer2 = mockk<TiLifecycleObserver>(relaxUnitFun = true)
        presenter.addLifecycleObserver(observer2)

        // When the view detached
        presenter.detachView()

        // Then the last added observer gets called first
        verifySequence {
            observer2.onChange(TiPresenter.State.VIEW_DETACHED, false)
            observer1.onChange(TiPresenter.State.VIEW_DETACHED, false)

            observer2.onChange(TiPresenter.State.VIEW_DETACHED, true)
            observer1.onChange(TiPresenter.State.VIEW_DETACHED, true)
        }
    }

    @Test
    fun `test create`() {
        val states = mutableListOf<Pair<TiPresenter.State, Boolean>>()
        presenter.addLifecycleObserver { state, hasLifecycleMethodBeenCalled ->
            states.add(Pair(state, hasLifecycleMethodBeenCalled))
        }

        presenter.create()

        val beforeLast = states[states.size - 2]
        assertThat(beforeLast.first).isEqualTo(TiPresenter.State.VIEW_DETACHED)
        assertThat(beforeLast.second).isEqualTo(false)

        val last = states.last()
        assertThat(last.first).isEqualTo(TiPresenter.State.VIEW_DETACHED)
        assertThat(last.second).isEqualTo(true)
    }

    @Test
    fun `test destroy`() {
        val states = mutableListOf<Pair<TiPresenter.State, Boolean>>()
        presenter.addLifecycleObserver { state, hasLifecycleMethodBeenCalled ->
            states.add(Pair(state, hasLifecycleMethodBeenCalled))
        }

        assertThat(presenter.mLifecycleObservers).hasSize(1)

        presenter.create()
        presenter.attachView(view)
        presenter.detachView()
        presenter.destroy()

        val beforeLast = states[states.size - 2]
        assertThat(beforeLast.first).isEqualTo(TiPresenter.State.DESTROYED)
        assertThat(beforeLast.second).isEqualTo(false)

        val last = states.last()
        assertThat(last.first).isEqualTo(TiPresenter.State.DESTROYED)
        assertThat(last.second).isEqualTo(true)

        assertThat(presenter.mLifecycleObservers).isEmpty()
    }

    @Test
    fun `test remove observer`() {
        val states = mutableListOf<Pair<TiPresenter.State, Boolean>>()
        val removable = presenter.addLifecycleObserver { state, hasLifecycleMethodBeenCalled ->
            states.add(Pair(state, hasLifecycleMethodBeenCalled))
        }

        presenter.create()

        val beforeLast = states[states.size - 2]
        assertThat(beforeLast.first).isEqualTo(TiPresenter.State.VIEW_DETACHED)
        assertThat(beforeLast.second).isEqualTo(false)

        val last = states.last()
        assertThat(last.first).isEqualTo(TiPresenter.State.VIEW_DETACHED)
        assertThat(last.second).isEqualTo(true)

        removable.remove()

        presenter.attachView(view)

        val beforeLast2 = states[states.size - 2]
        assertThat(beforeLast2.first).isNotEqualTo(TiPresenter.State.VIEW_ATTACHED)

        val last2 = states.last()
        assertThat(last2.first).isNotEqualTo(TiPresenter.State.VIEW_ATTACHED)
    }

    @Test
    fun `test remove observer twice`() {
        val states = mutableListOf<Pair<TiPresenter.State, Boolean>>()
        val observer = TiLifecycleObserver { state, hasLifecycleMethodBeenCalled ->
            states.add(Pair(state, hasLifecycleMethodBeenCalled))
        }

        val removable = presenter.addLifecycleObserver(observer)
        assertThat(presenter.mLifecycleObservers).hasSize(1)
        removable.remove()
        assertThat(presenter.mLifecycleObservers).isEmpty()

        val removable2 = presenter.addLifecycleObserver(observer)
        // remove should only remove once
        removable.remove()
        assertThat(presenter.mLifecycleObservers).hasSize(1)

        removable2.remove()
        assertThat(presenter.mLifecycleObservers).isEmpty()
    }

    @Test
    fun `test remove other observer`() {
        presenter.create()

        // add observers only for attach event
        val observer1 = mock(TiLifecycleObserver::class.java)
        presenter.addLifecycleObserver(observer1)
        val observer2 = mock(TiLifecycleObserver::class.java)
        val removable = presenter.addLifecycleObserver(observer2)

        // when observer1 receives the first event it unregisters observer2
        doAnswer {
            removable.remove()
            null
        }.`when`(observer1).onChange(
                ArgumentMatchers.any(TiPresenter.State::class.java),
                ArgumentMatchers.anyBoolean()
        )

        presenter.attachView(mock(TiView::class.java))

        val inOrder = inOrder(observer1, observer2)

        //observer 1 receives pre onAttachView event
        inOrder.verify(observer1).onChange(TiPresenter.State.VIEW_ATTACHED, false)

        // observer2 receives the pre event even when observer1 removed observer2 before observer2 received the pre event
        inOrder.verify(observer2).onChange(TiPresenter.State.VIEW_ATTACHED, false)

        // observer 1 receives post onAttachView event
        inOrder.verify(observer1).onChange(TiPresenter.State.VIEW_ATTACHED, true)

        // observer2 never receives the post event, is unregistered at that time
        verifyNoMoreInteractions(observer2)
    }

    @Test
    fun `test sleep`() {
        val states = mutableListOf<Triple<TiPresenter.State, Boolean, TiView?>>()
        presenter.addLifecycleObserver { state, hasLifecycleMethodBeenCalled ->
            states.add(Triple(state, hasLifecycleMethodBeenCalled, presenter.view))
        }

        presenter.create()
        presenter.attachView(view)
        presenter.detachView()

        val beforeLast = states[states.size - 2]
        assertThat(beforeLast.first).isEqualTo(TiPresenter.State.VIEW_DETACHED)
        assertThat(beforeLast.second).isEqualTo(false)
        assertThat(beforeLast.third).isNotNull()

        val last = states.last()
        assertThat(last.first).isEqualTo(TiPresenter.State.VIEW_DETACHED)
        assertThat(last.second).isEqualTo(true)
        assertThat(last.third).isNotNull()
    }

    @Test
    fun `test wake up`() {
        val states = mutableListOf<Triple<TiPresenter.State, Boolean, TiView?>>()
        presenter.addLifecycleObserver { state, hasLifecycleMethodBeenCalled ->
            states.add(Triple(state, hasLifecycleMethodBeenCalled, presenter.view))
        }

        presenter.create()
        presenter.attachView(view)

        val beforeLast = states[states.size - 2]
        assertThat(beforeLast.first).isEqualTo(TiPresenter.State.VIEW_ATTACHED)
        assertThat(beforeLast.second).isEqualTo(false)
        assertThat(beforeLast.third).isNotNull()

        val last = states.last()
        assertThat(last.first).isEqualTo(TiPresenter.State.VIEW_ATTACHED)
        assertThat(last.second).isEqualTo(true)
        assertThat(last.third).isNotNull()
    }
}
