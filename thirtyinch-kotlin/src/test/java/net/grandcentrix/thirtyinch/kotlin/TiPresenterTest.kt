package net.grandcentrix.thirtyinch.kotlin

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import net.grandcentrix.thirtyinch.TiPresenter
import net.grandcentrix.thirtyinch.TiView
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class TiPresenterTest {

    interface View : TiView {

        fun aViewMethod()

    }

    class TestPresenter : TiPresenter<View>()

    private val mockView = mock<View>()

    @Test
    fun `test sendToViewKotlin should view as this and call it`() = with(TestPresenter()) {
        val tiTestPresenter = test()
        tiTestPresenter.create()
        tiTestPresenter.attachView(mockView)

        deliverToView { aViewMethod() }

        verify(mockView).aViewMethod()
    }
}