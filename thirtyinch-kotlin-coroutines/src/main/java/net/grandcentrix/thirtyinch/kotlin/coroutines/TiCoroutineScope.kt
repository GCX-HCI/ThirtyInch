package net.grandcentrix.thirtyinch.kotlin.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import net.grandcentrix.thirtyinch.TiPresenter
import net.grandcentrix.thirtyinch.TiPresenter.State.DESTROYED
import net.grandcentrix.thirtyinch.TiPresenter.State.VIEW_DETACHED
import kotlin.coroutines.CoroutineContext

/**
 * A [CoroutineScope] that is bound to the lifecycle of the given [TiPresenter].
 *
 * @param cancelWhenViewDetaches If all started coroutines in this scope should be cancelled when the view detaches.
 * By default they're cancelled when `presenter` is destroyed. You should recreate this object when the view attaches,
 * if you set this to `true`.
 */
class TiCoroutineScope(
        private val presenter: TiPresenter<*>,
        context: CoroutineContext,
        cancelWhenViewDetaches: Boolean = false
) : CoroutineScope {

    /**
     * Parent [Job] for all coroutines that are started in the given presenter instance. When this `job` gets
     * cancelled all of its children jobs will get cancelled too.
     */
    private val job = Job()
    override val coroutineContext: CoroutineContext = context + job
    /**
     * The current [TiPresenter.State] of [presenter]. Needs to be kept as [VIEW_DETACHED] is observed first
     * and would instantly cancel all coroutines of this scope.
     */
    private var presenterState: TiPresenter.State? = null

    init {
        presenter.addLifecycleObserver { state, hasLifecycleMethodBeenCalled ->
            if (!hasLifecycleMethodBeenCalled) return@addLifecycleObserver

            when {
                state == DESTROYED && !cancelWhenViewDetaches -> job.cancel()
                state == VIEW_DETACHED && cancelWhenViewDetaches && presenterState != null -> job.cancel()
            }
            presenterState = state
        }
    }
}