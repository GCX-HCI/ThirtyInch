package net.grandcentrix.thirtyinch.rx2

import net.grandcentrix.thirtyinch.TiPresenter
import net.grandcentrix.thirtyinch.TiView

/**
 * Convenience base [TiPresenter] class implementing [DisposableHandler] to be extended by your presenters.
 *
 * Makes possible to use [Disposable][io.reactivex.disposables.Disposable] handling extension functions without the
 * need to implement the interface yourself.
 */
open class DisposableHandlingPresenter<V : TiView> : TiPresenter<V>(), DisposableHandler {

    @Suppress("LeakingThis")
    override val disposableHandler = RxTiPresenterDisposableHandler(this)
}