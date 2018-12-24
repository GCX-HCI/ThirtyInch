package net.grandcentrix.thirtyinch.rx2

import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import io.reactivex.Observable
import org.junit.*

class DisposableHandlerTest {

    @Before
    fun setUp() {
        mockkStatic("net.grandcentrix.thirtyinch.rx2.DisposableHandler")
    }

    @Test
    fun `disposeWhenDestroyed calls through to correct method`() {
        val handler = object : DisposableHandler {
            override val disposableHandler: RxTiPresenterDisposableHandler = mockk(relaxed = true)

            val disposable = Observable.never<Any>().subscribe()

            fun disposeDestroyed() {
                disposable.disposeWhenDestroyed()
            }
        }

        handler.disposeDestroyed()

        verify { handler.disposableHandler.manageDisposable(handler.disposable) }
    }

    @Test
    fun `disposeWhenViewDetached calls through to correct method`() {
        val handler = object : DisposableHandler {
            override val disposableHandler: RxTiPresenterDisposableHandler = mockk(relaxed = true)

            val disposable = Observable.never<Any>().subscribe()

            fun disposeDetached() {
                disposable.disposeWhenViewDetached()
            }
        }

        handler.disposeDetached()

        verify { handler.disposableHandler.manageViewDisposable(handler.disposable) }
    }
}