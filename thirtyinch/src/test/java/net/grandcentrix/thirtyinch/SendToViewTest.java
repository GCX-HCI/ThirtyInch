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

import android.support.annotation.NonNull;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.assertTrue;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
    public void sendToView_viewDetached_setExecutor_executesActions() throws Exception {
        final TestPresenter presenter = new TestPresenter();
        presenter.create();

        final TestView view = mock(TestView.class);

        // will not be posted until the view and the executor will be attached
        presenter.sendToView(new ViewAction<TestView>() {
            @Override
            public void call(final TestView testView) {
                testView.doSomething1();
            }
        });
        verify(view, never()).doSomething1();

        // setting the executor doesn't run the actions when the view is detached
        presenter.setUiThreadExecutor(new Executor() {
            @Override
            public void execute(@NonNull final Runnable command) {
                command.run();
            }
        });
        verify(view, never()).doSomething1();

        // when both are attached, the actions will be executed
        presenter.attachView(view);
        verify(view).doSomething1();
    }

    @Test
    public void sendToView_withView_afterInRunningState_crashes() throws Exception {
        final TestPresenter presenter = new TestPresenter();
        presenter.create();

        final TestView view = mock(TestView.class);
        presenter.attachView(view);

        // call sendToView without executor crashes when the view is running
        try {
            presenter.sendToView(new ViewAction<TestView>() {
                @Override
                public void call(final TestView testView) {
                    testView.doSomething1();
                }
            });
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        } catch (IllegalStateException e) {
            assertThat(e).hasMessageContaining("no ui thread executor");
        }

        verify(view, never()).doSomething1();
    }

    @Test
    public void sendToView_withView_beforeInRunningState_executesAction_withExecutor()
            throws Exception {
        final TestPresenter presenter = new TestPresenter() {
            @Override
            protected void onAttachView(@NonNull final TestView view) {
                super.onAttachView(view);
                // call sendToView in attachView before executor is attached, will be postponed
                sendToView(new ViewAction<TestView>() {
                    @Override
                    public void call(final TestView testView) {
                        testView.doSomething1();
                    }
                });
            }
        };
        presenter.create();
        presenter.setUiThreadExecutor(new Executor() {
            @Override
            public void execute(@NonNull final Runnable command) {
                command.run();
            }
        });

        // postponed actions will be executed
        final TestView view = mock(TestView.class);
        presenter.attachView(view);

        verify(view).doSomething1();
    }

    @Test
    public void sendToView_withView_beforeInRunningState_executesAction_without_executor()
            throws Exception {
        final TestPresenter presenter = new TestPresenter() {
            @Override
            protected void onAttachView(@NonNull final TestView view) {
                super.onAttachView(view);
                // call sendToView in attachView before executor is attached, will be postponed
                sendToView(new ViewAction<TestView>() {
                    @Override
                    public void call(final TestView testView) {
                        testView.doSomething1();
                    }
                });
            }
        };
        presenter.create();

        // postponed actions will be executed even without executor
        final TestView view = mock(TestView.class);
        presenter.attachView(view);

        verify(view).doSomething1();
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
                assertNotSame(testThread, currentThread);
                assertTrue("executed on wrong thread",
                        "test ui thread".equals(currentThread.getName()));
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
        presenter.setUiThreadExecutor(mImmediatelySameThread);
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
        presenter.setUiThreadExecutor(mImmediatelySameThread);
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
