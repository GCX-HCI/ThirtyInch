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
import org.mockito.InOrder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class TiLifecycleObserverTest {

    private TiMockPresenter mPresenter;

    private TiView mView;

    @Before
    public void setUp() throws Exception {
        TiLog.setLogger(new TiLog.Logger() {
            @Override
            public void log(final int level, final String tag, final String msg) {
                // prevent RuntimeException: android.util.Log not mocked
            }
        });
        mView = mock(TiView.class);
        mPresenter = new TiMockPresenter();
    }

    @After
    public void tearDown() throws Exception {
        mPresenter = null;
        mView = null;
    }

    @Test
    public void testCalledAttachedInCorrectOrder() throws Exception {
        mPresenter.create();

        // Given 2 observers
        final TiLifecycleObserver observer1 = mock(TiLifecycleObserver.class);
        mPresenter.addLifecycleObserver(observer1);
        final TiLifecycleObserver observer2 = mock(TiLifecycleObserver.class);
        mPresenter.addLifecycleObserver(observer2);

        // When a view attaches
        mPresenter.attachView(mock(TiView.class));

        // Then the last added observer gets called last
        final InOrder inOrder = inOrder(observer1, observer2);
        inOrder.verify(observer1).onChange(TiPresenter.State.VIEW_ATTACHED, false);
        inOrder.verify(observer2).onChange(TiPresenter.State.VIEW_ATTACHED, false);

        inOrder.verify(observer1).onChange(TiPresenter.State.VIEW_ATTACHED, true);
        inOrder.verify(observer2).onChange(TiPresenter.State.VIEW_ATTACHED, true);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testCalledCreateInCorrectOrder() throws Exception {

        // Given 2 observers
        final TiLifecycleObserver observer1 = mock(TiLifecycleObserver.class);
        mPresenter.addLifecycleObserver(observer1);
        final TiLifecycleObserver observer2 = mock(TiLifecycleObserver.class);
        mPresenter.addLifecycleObserver(observer2);

        // When the presenter gets created and reached view detached state
        mPresenter.create();

        // Then the last added observer gets called first because it's a destructive event
        final InOrder inOrder = inOrder(observer1, observer2);
        inOrder.verify(observer2).onChange(TiPresenter.State.VIEW_DETACHED, false);
        inOrder.verify(observer1).onChange(TiPresenter.State.VIEW_DETACHED, false);

        inOrder.verify(observer2).onChange(TiPresenter.State.VIEW_DETACHED, true);
        inOrder.verify(observer1).onChange(TiPresenter.State.VIEW_DETACHED, true);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testCalledDestroyInCorrectOrder() throws Exception {

        // Given a presenter with 2 added observers
        mPresenter.create();
        final TiLifecycleObserver observer1 = mock(TiLifecycleObserver.class);
        mPresenter.addLifecycleObserver(observer1);
        final TiLifecycleObserver observer2 = mock(TiLifecycleObserver.class);
        mPresenter.addLifecycleObserver(observer2);

        // When the presenter gets destroyed
        mPresenter.destroy();

        // Then the last added observer gets called first
        final InOrder inOrder = inOrder(observer1, observer2);
        inOrder.verify(observer2).onChange(TiPresenter.State.DESTROYED, false);
        inOrder.verify(observer1).onChange(TiPresenter.State.DESTROYED, false);

        inOrder.verify(observer2).onChange(TiPresenter.State.DESTROYED, true);
        inOrder.verify(observer1).onChange(TiPresenter.State.DESTROYED, true);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testCalledDetachedInCorrectOrder() throws Exception {
        mPresenter.create();
        mPresenter.attachView(mock(TiView.class));

        // Given 2 observers
        final TiLifecycleObserver observer1 = mock(TiLifecycleObserver.class);
        mPresenter.addLifecycleObserver(observer1);
        final TiLifecycleObserver observer2 = mock(TiLifecycleObserver.class);
        mPresenter.addLifecycleObserver(observer2);

        // When the view detached
        mPresenter.detachView();

        // Then the last added observer gets called first
        final InOrder inOrder = inOrder(observer1, observer2);
        inOrder.verify(observer2).onChange(TiPresenter.State.VIEW_DETACHED, false);
        inOrder.verify(observer1).onChange(TiPresenter.State.VIEW_DETACHED, false);

        inOrder.verify(observer2).onChange(TiPresenter.State.VIEW_DETACHED, true);
        inOrder.verify(observer1).onChange(TiPresenter.State.VIEW_DETACHED, true);
    }

    @Test
    public void testCreate() throws Exception {
        final List<Object[]> states = new ArrayList<>();
        mPresenter.addLifecycleObserver(new TiLifecycleObserver() {
            @Override
            public void onChange(final TiPresenter.State state,
                    final boolean hasLifecycleMethodBeenCalled) {
                states.add(new Object[]{state, hasLifecycleMethodBeenCalled});
            }
        });

        mPresenter.create();

        final Object[] beforeLast = states.get(states.size() - 2);
        assertEquals(beforeLast[0], TiPresenter.State.VIEW_DETACHED);
        assertEquals(beforeLast[1], false);

        final Object[] last = states.get(states.size() - 1);
        assertEquals(last[0], TiPresenter.State.VIEW_DETACHED);
        assertEquals(last[1], true);
    }

    @Test
    public void testDestroy() throws Exception {
        final List<Object[]> states = new ArrayList<>();
        mPresenter.addLifecycleObserver(new TiLifecycleObserver() {
            @Override
            public void onChange(final TiPresenter.State state,
                    final boolean hasLifecycleMethodBeenCalled) {
                states.add(new Object[]{state, hasLifecycleMethodBeenCalled});
            }
        });

        mPresenter.create();
        mPresenter.attachView(mView);
        mPresenter.detachView();
        mPresenter.destroy();

        final Object[] beforeLast = states.get(states.size() - 2);
        assertEquals(beforeLast[0], TiPresenter.State.DESTROYED);
        assertEquals(beforeLast[1], false);

        final Object[] last = states.get(states.size() - 1);
        assertEquals(last[0], TiPresenter.State.DESTROYED);
        assertEquals(last[1], true);
    }

    @Test
    public void testRemoveObserver() throws Exception {
        final List<Object[]> states = new ArrayList<>();
        final Removable removable = mPresenter.addLifecycleObserver(new TiLifecycleObserver() {
            @Override
            public void onChange(final TiPresenter.State state,
                    final boolean hasLifecycleMethodBeenCalled) {
                states.add(new Object[]{state, hasLifecycleMethodBeenCalled});
            }
        });

        mPresenter.create();

        final Object[] beforeLast = states.get(states.size() - 2);
        assertEquals(beforeLast[0], TiPresenter.State.VIEW_DETACHED);
        assertEquals(beforeLast[1], false);

        final Object[] last = states.get(states.size() - 1);
        assertEquals(last[0], TiPresenter.State.VIEW_DETACHED);
        assertEquals(last[1], true);

        removable.remove();

        mPresenter.attachView(mView);

        final Object[] beforeLast2 = states.get(states.size() - 2);
        assertNotEquals(beforeLast2[0], TiPresenter.State.VIEW_ATTACHED);

        final Object[] last2 = states.get(states.size() - 1);
        assertNotEquals(last2[0], TiPresenter.State.VIEW_ATTACHED);
    }

    @Test
    public void testRemoveObserverTwice() throws Exception {
        final List<Object[]> states = new ArrayList<>();
        final TiLifecycleObserver observer = new TiLifecycleObserver() {
            @Override
            public void onChange(final TiPresenter.State state,
                    final boolean hasLifecycleMethodBeenCalled) {
                states.add(new Object[]{state, hasLifecycleMethodBeenCalled});
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
    public void testRemoveOtherObserver() throws Exception {
        mPresenter.create();

        // add observers only for attach event
        final TiLifecycleObserver observer1 = mock(TiLifecycleObserver.class);
        mPresenter.addLifecycleObserver(observer1);
        final TiLifecycleObserver observer2 = mock(TiLifecycleObserver.class);
        final Removable removable = mPresenter.addLifecycleObserver(observer2);

        // when observer1 receives the first event it unregisters observer2
        doAnswer(new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                removable.remove();
                return null;
            }
        }).when(observer1).onChange(any(TiPresenter.State.class), anyBoolean());

        mPresenter.attachView(mock(TiView.class));

        final InOrder inOrder = inOrder(observer1, observer2);

        //observer 1 receives pre onAttachView event
        inOrder.verify(observer1).onChange(TiPresenter.State.VIEW_ATTACHED, false);

        // observer2 receives the pre event even when observer1 removed observer2 before observer2 received the pre event
        inOrder.verify(observer2).onChange(TiPresenter.State.VIEW_ATTACHED, false);


        // observer 1 receives post onAttachView event
        inOrder.verify(observer1).onChange(TiPresenter.State.VIEW_ATTACHED, true);
        
        // observer2 never receives the post event, is unregistered at that time
        verifyNoMoreInteractions(observer2);
    }

    @Test
    public void testSleep() throws Exception {
        final List<Object[]> states = new ArrayList<>();
        mPresenter.addLifecycleObserver(new TiLifecycleObserver() {
            @Override
            public void onChange(final TiPresenter.State state,
                    final boolean hasLifecycleMethodBeenCalled) {
                states.add(new Object[]{state, hasLifecycleMethodBeenCalled, mPresenter.getView()});
            }
        });

        mPresenter.create();
        mPresenter.attachView(mView);
        mPresenter.detachView();

        final Object[] beforeLast = states.get(states.size() - 2);
        assertEquals(beforeLast[0], TiPresenter.State.VIEW_DETACHED);
        assertEquals(beforeLast[1], false);
        assertNotNull(beforeLast[2]);

        final Object[] last = states.get(states.size() - 1);
        assertEquals(last[0], TiPresenter.State.VIEW_DETACHED);
        assertEquals(last[1], true);
        assertNotNull(last[2]);
    }

    @Test
    public void testWakeup() throws Exception {
        final List<Object[]> states = new ArrayList<>();
        mPresenter.addLifecycleObserver(new TiLifecycleObserver() {
            @Override
            public void onChange(final TiPresenter.State state,
                    final boolean hasLifecycleMethodBeenCalled) {
                states.add(new Object[]{state, hasLifecycleMethodBeenCalled, mPresenter.getView()});
            }
        });

        mPresenter.create();
        mPresenter.attachView(mView);

        final Object[] beforeLast = states.get(states.size() - 2);
        assertEquals(beforeLast[0], TiPresenter.State.VIEW_ATTACHED);
        assertEquals(beforeLast[1], false);
        assertNotNull(beforeLast[2]);

        final Object[] last = states.get(states.size() - 1);
        assertEquals(last[0], TiPresenter.State.VIEW_ATTACHED);
        assertEquals(last[1], true);
        assertNotNull(last[2]);
    }
}
