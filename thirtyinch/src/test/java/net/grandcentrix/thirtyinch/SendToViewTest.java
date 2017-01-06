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

package net.grandcentrix.thirtyinch;


import org.junit.Test;
import org.mockito.InOrder;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

public class SendToViewTest {

    private class TestPresenter extends TiPresenter<TestView> {

    }

    private interface TestView extends TiView {

        void doSomething1();

        void doSomething2();

        void doSomething3();
    }

    @Test
    public void sendToViewInOrder() throws Exception {
        final TestPresenter presenter = new TestPresenter();
        presenter.create();
        assertThat(presenter.getQueuedViewActions()).hasSize(0);

        presenter.sendToView(new ViewAction<TestView>() {
            @Override
            public void call(final TestView view) {
                view.doSomething3();
            }
        });
        presenter.sendToView(new ViewAction<TestView>() {
            @Override
            public void call(final TestView view) {
                view.doSomething1();
            }
        });
        presenter.sendToView(new ViewAction<TestView>() {
            @Override
            public void call(final TestView view) {
                view.doSomething2();
            }
        });
        assertThat(presenter.getQueuedViewActions()).hasSize(3);

        final TestView view = mock(TestView.class);
        presenter.attachView(view);

        assertThat(presenter.getQueuedViewActions()).hasSize(0);

        final InOrder inOrder = inOrder(view);
        inOrder.verify(view).doSomething3();
        inOrder.verify(view).doSomething1();
        inOrder.verify(view).doSomething2();
    }

    @Test
    public void viewAttached() throws Exception {
        final TestPresenter presenter = new TestPresenter();
        presenter.create();
        assertThat(presenter.getQueuedViewActions()).hasSize(0);

        final TestView view = mock(TestView.class);
        presenter.attachView(view);

        presenter.sendToView(new ViewAction<TestView>() {
            @Override
            public void call(final TestView view) {
                view.doSomething1();
            }
        });
        assertThat(presenter.getQueuedViewActions()).hasSize(0);
        verify(view).doSomething1();
    }

    @Test
    public void viewDetached() throws Exception {
        final TestPresenter presenter = new TestPresenter();
        presenter.create();
        assertThat(presenter.getQueuedViewActions()).hasSize(0);

        presenter.sendToView(new ViewAction<TestView>() {
            @Override
            public void call(final TestView view) {
                view.doSomething1();
            }
        });
        assertThat(presenter.getQueuedViewActions()).hasSize(1);

        final TestView view = mock(TestView.class);
        presenter.attachView(view);
        verify(view).doSomething1();

        assertThat(presenter.getQueuedViewActions()).hasSize(0);
    }

    @Test
    public void viewReceivesNoInteractionsAfterDetaching() throws Exception {
        final TestPresenter presenter = new TestPresenter();
        presenter.create();
        assertThat(presenter.getQueuedViewActions()).hasSize(0);

        final TestView view = mock(TestView.class);
        presenter.attachView(view);
        presenter.detachView();

        presenter.sendToView(new ViewAction<TestView>() {
            @Override
            public void call(final TestView view) {
                view.doSomething1();
            }
        });
        assertThat(presenter.getQueuedViewActions()).hasSize(1);
        verifyZeroInteractions(view);

        presenter.attachView(view);

        verify(view).doSomething1();
        assertThat(presenter.getQueuedViewActions()).hasSize(0);


        presenter.detachView();

        presenter.sendToView(new ViewAction<TestView>() {
            @Override
            public void call(final TestView view) {
                view.doSomething1();
            }
        });
        assertThat(presenter.getQueuedViewActions()).hasSize(1);

        verifyNoMoreInteractions(view);
    }
}
