/*
 * Copyright (C) 2017 grandcentrix GmbH
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.grandcentrix.thirtyinch.test

import io.mockk.mockk
import io.mockk.verify
import net.grandcentrix.thirtyinch.TiPresenter
import net.grandcentrix.thirtyinch.TiView
import org.junit.*

class TestPresenterExampleTest {

    private class LoginPresenter : TiPresenter<LoginView>() {

        fun onSubmitClicked() {
            sendToView { it.showError("No username entered") }
        }
    }

    private interface LoginView : TiView {

        fun showError(msg: String)
    }

    @Test
    fun `test load data`() {
        val loginPresenter = LoginPresenter()
        val testPresenter = loginPresenter.test()
        val view = testPresenter.attachView(mockk(relaxUnitFun = true))

        loginPresenter.onSubmitClicked()
        verify { view.showError("No username entered") }
    }
}
