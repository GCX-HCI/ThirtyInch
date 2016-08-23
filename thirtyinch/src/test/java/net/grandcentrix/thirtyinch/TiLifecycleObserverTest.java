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

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mock;

public class TiLifecycleObserverTest {

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
    public void testDestroy() throws Exception {
        final List<Object[]> states = new ArrayList<>();
        mPresenter.addLifecycleObserver(new TiLifecycleObserver() {
            @Override
            public void onChange(final TiPresenter.State state,
                    final boolean beforeLifecycleEvent) {
                states.add(new Object[]{state, beforeLifecycleEvent});
            }
        });

        mPresenter.create();
        mPresenter.bindNewView(mView);
        mPresenter.wakeUp();
        mPresenter.sleep();
        mPresenter.destroy();

        final Object[] beforeLast = states.get(states.size() - 2);
        assertEquals(beforeLast[0], TiPresenter.State.DESTROYED);
        assertEquals(beforeLast[1], false);

        final Object[] last = states.get(states.size() - 1);
        assertEquals(last[0], TiPresenter.State.DESTROYED);
        assertEquals(last[1], true);
    }

    @Test
    public void testCreate() throws Exception {
        final List<Object[]> states = new ArrayList<>();
        mPresenter.addLifecycleObserver(new TiLifecycleObserver() {
            @Override
            public void onChange(final TiPresenter.State state,
                    final boolean beforeLifecycleEvent) {
                states.add(new Object[]{state, beforeLifecycleEvent});
            }
        });

        mPresenter.create();

        final Object[] beforeLast = states.get(states.size() - 2);
        assertEquals(beforeLast[0], TiPresenter.State.CREATED_WITH_DETACHED_VIEW);
        assertEquals(beforeLast[1], false);

        final Object[] last = states.get(states.size() - 1);
        assertEquals(last[0], TiPresenter.State.CREATED_WITH_DETACHED_VIEW);
        assertEquals(last[1], true);
    }

    @Test
    public void testRemoveObserver() throws Exception {
        final List<Object[]> states = new ArrayList<>();
        final Removable removable = mPresenter.addLifecycleObserver(new TiLifecycleObserver() {
            @Override
            public void onChange(final TiPresenter.State state,
                    final boolean beforeLifecycleEvent) {
                states.add(new Object[]{state, beforeLifecycleEvent});
            }
        });

        mPresenter.create();

        final Object[] beforeLast = states.get(states.size() - 2);
        assertEquals(beforeLast[0], TiPresenter.State.CREATED_WITH_DETACHED_VIEW);
        assertEquals(beforeLast[1], false);

        final Object[] last = states.get(states.size() - 1);
        assertEquals(last[0], TiPresenter.State.CREATED_WITH_DETACHED_VIEW);
        assertEquals(last[1], true);

        removable.remove();

        mPresenter.bindNewView(mView);
        mPresenter.wakeUp();

        final Object[] beforeLast2 = states.get(states.size() - 2);
        assertNotEquals(beforeLast2[0], TiPresenter.State.VIEW_ATTACHED_AND_AWAKE);

        final Object[] last2 = states.get(states.size() - 1);
        assertNotEquals(last2[0], TiPresenter.State.VIEW_ATTACHED_AND_AWAKE);
    }

    @Test
    public void testRemoveObserverTwice() throws Exception {
        final List<Object[]> states = new ArrayList<>();
        final TiLifecycleObserver observer = new TiLifecycleObserver() {
            @Override
            public void onChange(final TiPresenter.State state,
                    final boolean beforeLifecycleEvent) {
                states.add(new Object[]{state, beforeLifecycleEvent});
            }
        };

        final Removable removable = mPresenter.addLifecycleObserver(observer);
        assertEquals(1, mPresenter.mLifecycleObservers.size());
        removable.remove();
        assertEquals(0, mPresenter.mLifecycleObservers.size());

        final Removable removable2 = mPresenter.addLifecycleObserver(observer);
        // remove should only remove once
        removable.remove();
        assertEquals(1, mPresenter.mLifecycleObservers.size());

        removable2.remove();
        assertEquals(0, mPresenter.mLifecycleObservers.size());
    }

    @Test
    public void testSleep() throws Exception {
        final List<Object[]> states = new ArrayList<>();
        mPresenter.addLifecycleObserver(new TiLifecycleObserver() {
            @Override
            public void onChange(final TiPresenter.State state,
                    final boolean beforeLifecycleEvent) {
                states.add(new Object[]{state, beforeLifecycleEvent});
            }
        });

        mPresenter.create();
        mPresenter.bindNewView(mView);
        mPresenter.wakeUp();
        mPresenter.sleep();

        final Object[] beforeLast = states.get(states.size() - 2);
        assertEquals(beforeLast[0], TiPresenter.State.CREATED_WITH_DETACHED_VIEW);
        assertEquals(beforeLast[1], false);

        final Object[] last = states.get(states.size() - 1);
        assertEquals(last[0], TiPresenter.State.CREATED_WITH_DETACHED_VIEW);
        assertEquals(last[1], true);
    }

    @Test
    public void testWakeup() throws Exception {
        final List<Object[]> states = new ArrayList<>();
        mPresenter.addLifecycleObserver(new TiLifecycleObserver() {
            @Override
            public void onChange(final TiPresenter.State state,
                    final boolean beforeLifecycleEvent) {
                states.add(new Object[]{state, beforeLifecycleEvent});
            }
        });

        mPresenter.create();
        mPresenter.bindNewView(mView);
        mPresenter.wakeUp();

        final Object[] beforeLast = states.get(states.size() - 2);
        assertEquals(beforeLast[0], TiPresenter.State.VIEW_ATTACHED_AND_AWAKE);
        assertEquals(beforeLast[1], false);

        final Object[] last = states.get(states.size() - 1);
        assertEquals(last[0], TiPresenter.State.VIEW_ATTACHED_AND_AWAKE);
        assertEquals(last[1], true);
    }
}
