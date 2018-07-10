package net.grandcentrix.thirtyinch.kotlin

import com.nhaarman.mockito_kotlin.*
import net.grandcentrix.thirtyinch.TiPresenter
import net.grandcentrix.thirtyinch.TiView
import org.junit.*
import org.junit.runner.*
import org.junit.runners.*

@RunWith(JUnit4::class)
class TiPresenterTest {

    interface View : TiView {
        fun aViewMethod()
    }

    class TestPresenter : TiPresenter<View>()

    private val mockView = mock<View>()

    @Test
    fun `test deliverToView should view as this and call it`() = with(TestPresenter()) {
        val tiTestPresenter = test()
        tiTestPresenter.attachView(mockView)

        deliverToView { aViewMethod() }

        verify(mockView).aViewMethod()
    }

    @Test
    fun `test deliverToView without attached view`() = with(TestPresenter()) {
        val tiTestPresenter = test()
        deliverToView { aViewMethod() }
        verify(mockView, never()).aViewMethod()

        tiTestPresenter.attachView(mockView)
        verify(mockView).aViewMethod()
    }
}