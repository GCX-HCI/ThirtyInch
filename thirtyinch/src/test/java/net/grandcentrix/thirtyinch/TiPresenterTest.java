/*
 * Copyright (C) 2015 grandcentrix GmbH
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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

/**
 * @author jannisveerkamp
 * @since 11.07.16.
 */
public class TiPresenterTest {

    private TiMockPresenter mPresenter;

    private TiView mView;

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
    public void testBindNewView() throws Exception {
        TiView viewOverride = mock(TiView.class);
        mPresenter.create();
        mPresenter.bindNewView(mView);

        assertThat(mPresenter.getView(), equalTo(mView));

        mPresenter.bindNewView(mView);
        assertThat(mPresenter.getView(), equalTo(mView));

        mPresenter.bindNewView(viewOverride);
        assertThat(mPresenter.getView(), equalTo(viewOverride));

        try {
            mPresenter.bindNewView(null);
            fail();
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("sleep"));
        }

        mPresenter.wakeUp();
        assertThat(mPresenter.getView(), equalTo(viewOverride));
        mPresenter.sleep();
        assertThat(mPresenter.getView(), nullValue());
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
    public void testGetView() throws Exception {
        mPresenter.create();
        mPresenter.bindNewView(mView);
        assertThat(mPresenter.getView(), equalTo(mView));
    }


    @Test(expected = IllegalAccessError.class)
    public void testOnCreate() throws Exception {
        mPresenter.onCreate();
    }

    @Test(expected = IllegalAccessError.class)
    public void testOnDestroy() throws Exception {
        mPresenter.onDestroy();
    }

    @Test(expected = IllegalAccessError.class)
    public void testOnMoveToForeground() throws Exception {
        mPresenter.onMoveToForeground();
    }

    @Test(expected = IllegalAccessError.class)
    public void testOnMovedToBackground() throws Exception {
        mPresenter.onMovedToBackground();
    }

    @Test(expected = IllegalAccessError.class)
    public void testOnSleep() throws Exception {
        mPresenter.onSleep();
    }

    @Test(expected = IllegalAccessError.class)
    public void testOnWakeUp() throws Exception {
        mPresenter.onWakeUp();
    }


    @Test(expected = SuperNotCalledException.class)
    public void testSleepSuperNotCalled() throws Exception {
        TiPresenter<TiView> presenter = new TiPresenter<TiView>() {
            @Override
            protected void onSleep() {
                // Intentionally not calling super.onSleep()
            }
        };
        presenter.create();
        presenter.wakeUp();
        presenter.sleep();
    }

    @Test
    public void testToString() throws Exception {
        mPresenter.create();
        assertThat(mPresenter.toString(), containsString("TiMockPresenter"));
        assertThat(mPresenter.toString(), containsString("{view = null}"));
        mPresenter.bindNewView(mView);
        assertThat(mPresenter.toString(), containsString("TiMockPresenter"));
        assertThat(mPresenter.toString(), containsString("{view = Mock for TiView, hashCode: "));
    }

    @Test
    public void testWakeUp() throws Exception {
        mPresenter.create();
        assertThat(mPresenter.onWakeUpCalled, equalTo(0));
        mPresenter.wakeUp();
        assertThat(mPresenter.onWakeUpCalled, equalTo(1));
        // not calling again
        mPresenter.wakeUp();
        assertThat(mPresenter.onWakeUpCalled, equalTo(1));
    }

    @Test(expected = SuperNotCalledException.class)
    public void testWakeUpSuperNotCalled() throws Exception {
        TiPresenter<TiView> presenter = new TiPresenter<TiView>() {
            @Override
            protected void onWakeUp() {
                // Intentionally not calling super.onWakeup()
            }
        };
        presenter.create();
        presenter.wakeUp();
    }
}