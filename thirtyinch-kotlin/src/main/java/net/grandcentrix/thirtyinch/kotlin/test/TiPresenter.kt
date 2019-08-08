package net.grandcentrix.thirtyinch.kotlin.test

import net.grandcentrix.thirtyinch.TiPresenter
import net.grandcentrix.thirtyinch.TiView

/**
 * Convenience function to use in tests.
 *
 * Returns this [TiPresenter] with attached [mockView].
 */
fun <V : TiView, P : TiPresenter<V>> P.testAttachView(mockView: V): P =
        apply { test().run { attachView(mockView) } }
