package net.grandcentrix.thirtyinch.kotlin.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import net.grandcentrix.thirtyinch.TiPresenter
import net.grandcentrix.thirtyinch.TiPresenter.State.DESTROYED
import net.grandcentrix.thirtyinch.TiPresenter.State.VIEW_ATTACHED
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
        private val context: CoroutineContext
) : CoroutineScope {

    /**
     * Parent [Job] for all coroutines that are started in the given presenter instance. When this `job` gets
     * cancelled all of its children jobs will get cancelled too.
     */
    private val job = Job()
    override val coroutineContext: CoroutineContext = context + job
    /**
     * Parent [Job] for all coroutines that are started while a `view` attached. When this `job` gets
     * cancelled (when the view detaches) all of its children jobs will get cancelled too.
     */
    private var onViewDetachJob: Job? = null
    /**
     * A [CoroutineContext] that will be used when starting any coroutines via [launchUntilViewDetaches].
     */
    private var onViewDetachCoroutineContext: CoroutineContext? = null
    /**
     * The current [TiPresenter.State] of [presenter]. Needs to be kept as [VIEW_DETACHED] is observed first
     * and would instantly cancel all coroutines of this scope.
     */
    private var presenterState: TiPresenter.State? = null

    init {
        presenter.addLifecycleObserver { state, hasLifecycleMethodBeenCalled ->
            if (!hasLifecycleMethodBeenCalled) return@addLifecycleObserver

            when {
                state == DESTROYED -> job.cancel()
                state == VIEW_DETACHED && presenterState != null -> {
                    onViewDetachCoroutineContext?.cancel()
                    onViewDetachCoroutineContext = null
                    onViewDetachJob = null
                }
                state == VIEW_ATTACHED -> {
                    onViewDetachJob = Job(job)
                    onViewDetachCoroutineContext = context + onViewDetachJob!!
                }
            }
            presenterState = state
        }
    }

    /**
     * Same as [launch], but the so started [Job] will be cancelled once the `view` detaches.
     *
     * Calling this method while no `view` is attached will throw a `IllegalStateException`, because no
     * [CoroutineContext] is available at this time.
     */
    fun launchUntilViewDetaches(
            block: suspend CoroutineScope.() -> Unit
    ): Job {
        val context = onViewDetachCoroutineContext ?: throw IllegalStateException(
                "launchUntilViewDetaches can only be called when there is a view attached")
        return launch(context = context, block = block)
    }
}