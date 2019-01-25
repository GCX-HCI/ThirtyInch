package net.grandcentrix.thirtyinch.kotlin.coroutines

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

    @Test
    fun `cancels all jobs when presenter is destroyed`() {
        val scope = TiCoroutineScope(presenter, coroutineContext)
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
        val scope = TiCoroutineScope(presenter, coroutineContext, true)
        testPresenter.attachView(view)

        // starting a job while a view is attached
        val newJob = scope.launch { delay(10000) }

        // detaching the view cancels the job
        testPresenter.detachView()
        assertTrue(newJob.isCancelled)
    }
}

class Presenter : TiPresenter<TiView>()