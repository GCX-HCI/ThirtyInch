package net.grandcentrix.thirtyinch.kotlin

import io.reactivex.disposables.Disposable
import net.grandcentrix.thirtyinch.rx2.RxTiPresenterDisposableHandler

/**
 * Interface to implement by your [TiPresenter][net.grandcentrix.thirtyinch.TiPresenter] to provide extension
 * functions for managing [Disposable]s.
 *
 * Initialize the [disposableHandler] with reference to your TiPresenter instance.
 *
 * Usage:
 * ```
 * class MyPresenter : TiPresenter<MyView>(), DisposableHandler {
 *
 *     override val disposableHandler = RxTiPresenterDisposableHandler(this)
 *
 *     override fun onCreate() {
 *         super.onCreate()
 *
 *         // Presenter lifecycle dependent Disposable
 *         myObservable
 *             .subscribe()
 *             .disposeWhenDestroyed()
 *     }
 *
 *     override fun onAttachView(view: MyView) {
 *         super.onAttachView(view)
 *
 *         // View attached/detached dependent Disposable
 *         myViewObservable
 *             .subscribe()
 *             .disposeWhenViewDetached()
 *     }
 * }
 * ```
 */
interface DisposableHandler {

    /**
     * Initialize with reference to your [TiPresenter][net.grandcentrix.thirtyinch.TiPresenter] instance
     */
    val disposableHandler: RxTiPresenterDisposableHandler

    /**
     * Dispose of [Disposable]s dependent on the [TiPresenter][net.grandcentrix.thirtyinch.TiPresenter] lifecycle
     */
    fun Disposable.disposeWhenDestroyed(): Disposable = disposableHandler.manageDisposable(this)

    /**
     * Dispose of [Disposable]s dependent on the [TiView][net.grandcentrix.thirtyinch.TiView] attached/detached state
     */
    fun Disposable.disposeWhenViewDetached(): Disposable = disposableHandler.manageViewDisposable(this)
}