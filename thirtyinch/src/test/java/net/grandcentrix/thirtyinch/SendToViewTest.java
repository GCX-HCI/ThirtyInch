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


import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import android.support.annotation.NonNull;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.junit.*;
import org.mockito.*;

public class SendToViewTest {

    private class TestPresenter extends TiPresenter<TestView> {

    }

    private interface TestView extends TiView {

        void doSomething1();

        void doSomething2();

        void doSomething3();
    }

    private Executor mImmediatelySameThread = new Executor() {
        @Override
        public void execute(@NonNull final Runnable action) {
            action.run();
        }
    };

    @Test
    public void sendToViewInOrder() throws Exception {
        final TestPresenter presenter = new TestPresenter();
        presenter.create();
        presenter.setUiThreadExecutor(mImmediatelySameThread);
        assertThat(presenter.getQueuedViewActions()).isEmpty();

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

        assertThat(presenter.getQueuedViewActions()).isEmpty();

        final InOrder inOrder = inOrder(view);
        inOrder.verify(view).doSomething3();
        inOrder.verify(view).doSomething1();
        inOrder.verify(view).doSomething2();
    }

    @Test
    public void testSendToViewRunsOnTheMainThread() throws Exception {

        // Given a presenter with executor (single thread)
        final TiPresenter<TiView> presenter = new TiPresenter<TiView>() {
        };
        presenter.create();

        final ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(final Runnable r) {
                return new Thread(r, "test ui thread");
            }
        });
        presenter.setUiThreadExecutor(executor);
        presenter.attachView(mock(TiView.class));

        final Thread testThread = Thread.currentThread();

        // When send work to the view
        final CountDownLatch latch = new CountDownLatch(1);

        presenter.sendToView(new ViewAction<TiView>() {
            @Override
            public void call(final TiView tiView) {
                // Then the work gets executed on the ui thread
                final Thread currentThread = Thread.currentThread();
                assertThat(testThread).isNotSameAs(currentThread);
                assertThat("test ui thread")
                        .as("executed on wrong thread")
                        .isEqualTo(currentThread.getName());
                latch.countDown();
            }
        });

        // wait a reasonable amount of time for the thread to execute the work
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    public void viewAttached() throws Exception {
        final TestPresenter presenter = new TestPresenter();
        presenter.create();
        presenter.setUiThreadExecutor(mImmediatelySameThread);
        assertThat(presenter.getQueuedViewActions()).isEmpty();

        final TestView view = mock(TestView.class);
        presenter.attachView(view);

        presenter.sendToView(new ViewAction<TestView>() {
            @Override
            public void call(final TestView view) {
                view.doSomething1();
            }
        });
        assertThat(presenter.getQueuedViewActions()).isEmpty();
        verify(view).doSomething1();
    }

    @Test
    public void viewDetached() throws Exception {
        final TestPresenter presenter = new TestPresenter();
        presenter.create();
        presenter.setUiThreadExecutor(mImmediatelySameThread);
        assertThat(presenter.getQueuedViewActions()).isEmpty();

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

        assertThat(presenter.getQueuedViewActions()).isEmpty();
    }

    @Test
    public void viewReceivesNoInteractionsAfterDetaching() throws Exception {
        final TestPresenter presenter = new TestPresenter();
        presenter.create();
        presenter.setUiThreadExecutor(mImmediatelySameThread);
        assertThat(presenter.getQueuedViewActions()).isEmpty();

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
        assertThat(presenter.getQueuedViewActions()).isEmpty();

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
