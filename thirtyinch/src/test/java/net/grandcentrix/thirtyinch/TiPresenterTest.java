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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import android.support.annotation.NonNull;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.mock;

/**
 * @author jannisveerkamp
 * @since 11.07.16.
 */
public class TiPresenterTest {

    private TiMockPresenter mPresenter;

    private TiView mView;

    @Test
    public void attachDifferentView() throws Exception {
        TiView viewOverride = mock(TiView.class);
        mPresenter.create();

        mPresenter.attachView(mView);
        assertThat(mPresenter.getView(), equalTo(mView));

        try {
            mPresenter.attachView(viewOverride);
            fail("no exception thrown");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("detachView"));
        }
    }

    @Test
    public void attachNullView() throws Exception {

        mPresenter.create();
        try {
            mPresenter.attachView(null);
            fail("no exception thrown");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("detachView()"));
        }
    }

    @Test
    public void attachSameViewTwice() throws Exception {
        mPresenter.create();

        mPresenter.attachView(mView);
        assertThat(mPresenter.getView(), equalTo(mView));

        mPresenter.attachView(mView);
        assertThat(mPresenter.getView(), equalTo(mView));
    }

    @Test
    public void attachViewToDestroyedPresenter() throws Exception {
        mPresenter.create();
        mPresenter.destroy();

        try {
            mPresenter.attachView(mView);
            fail("no exception thrown");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("terminal state"));
        }
    }

    @Test
    public void attachWithoutInitialize() throws Exception {
        try {
            mPresenter.attachView(mView);
            fail("no exception thrown");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("create()"));
        }
    }

    @Test
    public void destroyPresenterWithAttachedView() throws Exception {
        mPresenter.create();
        mPresenter.attachView(mView);

        try {
            mPresenter.destroy();
            fail("error expected");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("attached"));
            assertTrue(e.getMessage().contains("detachView()"));
        }

    }

    @Test
    public void destroyWithoutAttachingView() throws Exception {
        mPresenter.create();
        mPresenter.destroy();
    }

    @Test
    public void detachView() throws Exception {
        mPresenter.create();
        assertEquals(null, mPresenter.getView());

        final TiView view = mock(TiView.class);
        mPresenter.attachView(view);
        assertEquals(view, mPresenter.getView());

        mPresenter.detachView();
        assertEquals(null, mPresenter.getView());
    }

    @Test
    public void detachViewNotAttached() throws Exception {
        mPresenter.create();

        // no exception, just ignoring
        mPresenter.detachView();

        mPresenter.attachView(mock(TiView.class));
        // no exception, just ignoring
        mPresenter.detachView();
        mPresenter.detachView();
    }

    @Before
    public void setUp() throws Exception {
        mView = mock(TiView.class);
        mPresenter = new TiMockPresenter();
    }

    @After
    public void tearDown() throws Exception {
        mPresenter = null;
        mView = null;
    }

    @Test
    public void testCallingOnAttachViewDirectly() throws Exception {
        try {
            mPresenter.onAttachView(mock(TiView.class));
            fail("no exception thrown");
        } catch (IllegalAccessError e) {
            assertTrue(e.getMessage().contains("attachView(TiView)"));
            assertTrue(e.getMessage().contains("#onAttachView(TiView)"));
        }
    }

    @Test
    public void testCallingOnCreateDirectly() throws Exception {
        try {
            mPresenter.onCreate();
            fail("no exception thrown");
        } catch (IllegalAccessError e) {
            assertTrue(e.getMessage().contains("create()"));
            assertTrue(e.getMessage().contains("#onCreate()"));
        }
    }

    @Test
    public void testCallingOnDestroyDirectly() throws Exception {
        try {
            mPresenter.onDestroy();
            fail("no exception thrown");
        } catch (IllegalAccessError e) {
            assertTrue(e.getMessage().contains("destroy()"));
            assertTrue(e.getMessage().contains("#onDestroy()"));
        }
    }

    @Test
    public void testCallingOnDetachViewDirectly() throws Exception {
        try {
            mPresenter.onDetachView();
            fail("no exception thrown");
        } catch (IllegalAccessError e) {
            assertTrue(e.getMessage().contains("detachView()"));
            assertTrue(e.getMessage().contains("#onDetachView()"));
        }
    }

    @Test
    public void testCallingOnSleepDirectly() throws Exception {
        try {
            mPresenter.onSleep();
            fail("no exception thrown");
        } catch (IllegalAccessError e) {
            assertTrue(e.getMessage().contains("detachView()"));
            assertTrue(e.getMessage().contains("#onSleep()"));
        }
    }

    @Test
    public void testCallingOnWakeUpDirectly() throws Exception {
        try {
            mPresenter.onWakeUp();
            fail("no exception thrown");
        } catch (IllegalAccessError e) {
            assertTrue(e.getMessage().contains("attachView(TiView)"));
            assertTrue(e.getMessage().contains("#onWakeUp()"));
        }
    }

    @Test
    public void testCreate() throws Exception {
        assertThat(mPresenter.onCreateCalled, equalTo(0));
        mPresenter.create();
        assertThat(mPresenter.onCreateCalled, equalTo(1));

        // onCreate can only be called once
        mPresenter.create();
        assertThat(mPresenter.onCreateCalled, equalTo(1));
    }

    @Test(expected = SuperNotCalledException.class)
    public void testCreateSuperNotCalled() throws Exception {
        TiPresenter<TiView> presenter = new TiPresenter<TiView>() {
            @Override
            protected void onCreate() {
                // Intentionally not calling super.onCreate()
            }
        };
        presenter.create();
    }

    @Test
    public void testDestroy() throws Exception {
        mPresenter.create();

        assertThat(mPresenter.onDestroyCalled, equalTo(0));
        mPresenter.destroy();
        assertThat(mPresenter.onDestroyCalled, equalTo(1));

        mPresenter.destroy();
        assertThat(mPresenter.onDestroyCalled, equalTo(1));
    }

    @Test
    public void testDestroyCreateNotCalled() throws Exception {
        assertThat(mPresenter.onDestroyCalled, equalTo(0));
        mPresenter.destroy();
        assertThat(mPresenter.onDestroyCalled, equalTo(0));
    }

    @Test(expected = SuperNotCalledException.class)
    public void testDestroySuperNotCalled() throws Exception {
        TiPresenter<TiView> presenter = new TiPresenter<TiView>() {
            @Override
            protected void onDestroy() {
                // Intentionally not calling super.onDestroy()
            }
        };
        presenter.create();
        presenter.destroy();
    }

    @Test
    public void testGetViewOrThrow() {
        mPresenter.create();
        mPresenter.attachView(mView);
        mPresenter.detachView();

        try {
            mPresenter.getViewOrThrow();
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(),
                    equalTo("The view is currently not attached. Use 'sendToView(ViewAction)' instead."));
        }
    }

    @Test
    public void testGetViewOrThrowReturnsView() {
        mPresenter.create();
        mPresenter.attachView(mView);
        assertThat(mPresenter.getViewOrThrow(), equalTo(mView));
    }

    @Test
    public void testGetViewReturnsNull() {
        mPresenter.create();
        mPresenter.attachView(mView);
        mPresenter.detachView();
        assertNull(mPresenter.getView());
    }

    @Test
    public void testGetViewReturnsView() {
        mPresenter.create();
        mPresenter.attachView(mView);
        assertThat(mPresenter.getView(), equalTo(mView));
    }

    @Test
    public void testMissingUiExecutorAndDetachedView() throws Exception {
        final TiPresenter<TiView> presenter = new TiPresenter<TiView>() {
        };

        try {
            presenter.runOnUiThread(mock(Runnable.class));
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), containsString("view"));
            assertThat(e.getMessage(), containsString("no executor"));
        }
    }

    @Test
    public void testMissingUiExecutorAttachedView() throws Exception {
        final TiPresenter<TiView> presenter = new TiPresenter<TiView>() {
        };
        presenter.create();
        presenter.attachView(mock(TiView.class));

        try {
            presenter.runOnUiThread(mock(Runnable.class));
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), not(containsString("view")));
            assertThat(e.getMessage(), containsString("no ui thread executor"));
        }
    }

    @Test
    public void testOnAttachViewSuperNotCalled() throws Exception {
        TiPresenter<TiView> presenter = new TiPresenter<TiView>() {

            @Override
            protected void onAttachView(@NonNull final TiView view) {
                // Intentionally not calling super.onSleep()
            }
        };
        presenter.create();
        try {
            presenter.attachView(mock(TiView.class));
            fail("no exception thrown");
        } catch (SuperNotCalledException e) {
            assertTrue(e.getMessage().contains("super.onAttachView(TiView)"));
        }
    }

    @Test
    public void testOnDetachViewSuperNotCalled() throws Exception {
        TiPresenter<TiView> presenter = new TiPresenter<TiView>() {

            @Override
            protected void onDetachView() {
                // Intentionally not calling super.onSleep()
            }
        };
        presenter.create();
        presenter.attachView(mock(TiView.class));
        try {
            presenter.detachView();
            fail("no exception thrown");
        } catch (SuperNotCalledException e) {
            assertTrue(e.getMessage().contains("super.onDetachView()"));
        }
    }

    @Test
    public void testRunOnUiExecutor() throws Exception {

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

        // When scheduling work to the UI thread
        final CountDownLatch latch = new CountDownLatch(1);

        presenter.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Then the work gets executed on the correct thread
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
    public void testSleepSuperNotCalled() throws Exception {
        TiPresenter<TiView> presenter = new TiPresenter<TiView>() {
            @Override
            protected void onSleep() {
                // Intentionally not calling super.onSleep()
            }
        };
        presenter.create();
        presenter.attachView(mock(TiView.class));
        try {
            presenter.detachView();
            fail("no exception thrown");
        } catch (SuperNotCalledException e) {
            assertTrue(e.getMessage().contains("super.onSleep()"));
        }
    }

    @Test
    public void testToString() throws Exception {
        mPresenter.create();
        assertThat(mPresenter.toString(), containsString("TiMockPresenter"));
        assertThat(mPresenter.toString(), containsString("{view = null}"));
        mPresenter.attachView(mView);
        assertThat(mPresenter.toString(), containsString("TiMockPresenter"));
        assertThat(mPresenter.toString(), containsString("{view = Mock for TiView, hashCode: "));
    }

    @Test
    public void testWakeUpSuperNotCalled() throws Exception {
        TiPresenter<TiView> presenter = new TiPresenter<TiView>() {
            @Override
            protected void onWakeUp() {
                // Intentionally not calling super.onWakeup()
            }
        };
        presenter.create();
        try {
            presenter.attachView(mock(TiView.class));
            fail("no exception thrown");
        } catch (SuperNotCalledException e) {
            assertTrue(e.getMessage().contains("super.onWakeUp()"));
        }
    }
}