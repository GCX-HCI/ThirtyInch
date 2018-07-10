package net.grandcentrix.thirtyinch.kotlin

import android.annotation.SuppressLint
import net.grandcentrix.thirtyinch.TiPresenter
import net.grandcentrix.thirtyinch.TiView

/**
 * Will call the given [block] in [TiPresenter.sendToView].
 *
 * This have the benefit that we can omit the `it` inside the `sendToView { }` call.
 *
 * Example:
 * ```
 * // Before
 * presenter.sendToView { it.aViewMethod() }
 * // After
 * presenter.deliverToView { aViewMethod() }
 * ```
 */
@SuppressLint("RestrictedApi")
fun <V : TiView> TiPresenter<V>.deliverToView(block: V.() -> Unit) = sendToView { block(it) }