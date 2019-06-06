package net.grandcentrix.thirtyinch.kotlin.coroutines

import kotlinx.coroutines.Job
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineContext
import net.grandcentrix.thirtyinch.TiPresenter
import net.grandcentrix.thirtyinch.TiView
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.*
import org.junit.runners.*

@RunWith(JUnit4::class)
@ObsoleteCoroutinesApi
class TiCoroutineScopeTest {

    private val presenter = Presenter()
    private val testPresenter = presenter.test()
    private val coroutineContext = TestCoroutineContext()
    private val view = object : TiView {}
    private val scope = TiCoroutineScope(presenter, coroutineContext)

    @Test
    fun `cancels all jobs when presenter is destroyed`() {
        testPresenter.attachView(view)

        // starting a job while a view is attached
        val newJob = scope.launch { delay(10000) }

        // detaching the view doesn't cancel it
        testPresenter.detachView()
        assertFalse(newJob.isCancelled)

        // but destroying the presenter does
        testPresenter.destroy()
        assertTrue(newJob.isCancelled)
    }

    @Test
    fun `cancels all jobs when view is detached`() {
        testPresenter.attachView(view)

        // starting a job while a view is attached
        val newJob = scope.launchUntilViewDetaches { delay(10000) }

        // detaching the view cancels the job
        testPresenter.detachView()
        assertTrue(newJob.isCancelled)
    }

    @Test
    fun `cancelling a job when view detaches does not cancel a job until presenter is destroyed`() {
        testPresenter.attachView(view)

        // starting two jobs, one until presenter is destroyed, one until view detaches
        val onDestroyJob = scope.launch { delay(10000) }
        val onViewDetachJob = scope.launchUntilViewDetaches { delay(10000) }

        // and then view detaches, cancels onViewDetachJob but not onDestroyJob
        testPresenter.detachView()

        assertTrue(onViewDetachJob.isCancelled)
        assertFalse(onDestroyJob.isCancelled)

        // destroying presenter then cancels the job
        testPresenter.destroy()
        assertTrue(onDestroyJob.isCancelled)
    }

    @Test
    fun `throw when launchUntilViewDetaches got called before view got attached`() {
        // don't attach a view
        try {
            scope.launchUntilViewDetaches { delay(10000) }
        } catch (exe: IllegalStateException) {
            assertTrue(exe.message == "launchUntilViewDetaches can only be called when there is a view attached")
        }
    }

    @Test
    fun `launching a job in onAttachView doesn't throw`() {
        lateinit var job: Job
        val presenter = Presenter2().apply {
            val scope = TiCoroutineScope(this, coroutineContext)
            jobLauncher = {
                job = scope.launchUntilViewDetaches { }
            }
        }

        presenter.test().attachView(view)

        assertTrue(job.isActive)
    }
}

private class Presenter : TiPresenter<TiView>()

private class Presenter2 : TiPresenter<TiView>() {

    lateinit var jobLauncher: (() -> Unit)

    override fun onAttachView(view: TiView) {
        super.onAttachView(view)
        jobLauncher()
    }
}