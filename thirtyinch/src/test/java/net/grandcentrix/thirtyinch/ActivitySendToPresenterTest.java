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


import net.grandcentrix.thirtyinch.internal.TiActivityDelegate;
import net.grandcentrix.thirtyinch.internal.TiActivityDelegateBuilder;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ActivitySendToPresenterTest {

    private class MyPresenter extends TiPresenter<TiView> {

        MyPresenter() {
            super();
        }

        public MyPresenter(final TiConfiguration config) {
            super(config);
        }

        void doSomething1() {

        }

        void doSomething2() {

        }

        void doSomething3() {

        }
    }

    @Test
    public void testSendToPresenterInOrder() throws Exception {

        final MyPresenter presenter = Mockito.spy(new MyPresenter());
        final TiActivityDelegate<TiPresenter<TiView>, TiView> delegate
                = new TiActivityDelegateBuilder().setPresenter(presenter).build();

        // presenter not attached yet
        assertThat(delegate.getPresenter()).isNull();
        verify(presenter, never()).doSomething1();

        // send action3
        delegate.sendToPresenter(new PresenterAction<TiPresenter<TiView>>() {
            @Override
            public void call(final TiPresenter<TiView> p) {
                ((MyPresenter) p).doSomething3();
            }
        });
        // send action1
        delegate.sendToPresenter(new PresenterAction<TiPresenter<TiView>>() {
            @Override
            public void call(final TiPresenter<TiView> p) {
                ((MyPresenter) p).doSomething1();
            }
        });
        // send action2
        delegate.sendToPresenter(new PresenterAction<TiPresenter<TiView>>() {
            @Override
            public void call(final TiPresenter<TiView> p) {
                ((MyPresenter) p).doSomething2();
            }
        });

        // nothing has been executed
        Mockito.verifyZeroInteractions(presenter);

        // attach presenter
        delegate.onCreate_afterSuper(null);
        assertThat(delegate.getPresenter()).isNotNull();

        final InOrder inOrder = inOrder(presenter);

        // automatically executes the actions in order
        inOrder.verify(presenter, times(1)).doSomething3();
        inOrder.verify(presenter, times(1)).doSomething1();
        inOrder.verify(presenter, times(1)).doSomething2();

    }

    @Test
    public void testSendToPresenter_AfterPresenterIsAttached() throws Exception {

        final MyPresenter presenter = Mockito.spy(new MyPresenter());
        final TiActivityDelegate<TiPresenter<TiView>, TiView> delegate
                = new TiActivityDelegateBuilder().setPresenter(presenter).build();

        // attach presenter
        delegate.onCreate_afterSuper(null);
        assertThat(delegate.getPresenter()).isNotNull();

        // send action
        delegate.sendToPresenter(new PresenterAction<TiPresenter<TiView>>() {
            @Override
            public void call(final TiPresenter<TiView> p) {
                ((MyPresenter) p).doSomething1();
            }
        });

        // will be executed
        verify(presenter, times(1)).doSomething1();
    }

    @Test
    public void testSendToPresenter_BeforePresenterIsAttached() throws Exception {

        final MyPresenter presenter = Mockito.spy(new MyPresenter());
        final TiActivityDelegate<TiPresenter<TiView>, TiView> delegate
                = new TiActivityDelegateBuilder().setPresenter(presenter).build();

        // presenter not attached yet
        assertThat(delegate.getPresenter()).isNull();
        verify(presenter, never()).doSomething1();

        // send action
        delegate.sendToPresenter(new PresenterAction<TiPresenter<TiView>>() {
            @Override
            public void call(final TiPresenter<TiView> p) {
                ((MyPresenter) p).doSomething1();
            }
        });
        // will not be executed
        verify(presenter, never()).doSomething1();

        // attach presenter
        delegate.onCreate_afterSuper(null);
        assertThat(delegate.getPresenter()).isNotNull();

        // automatically executes the action
        verify(presenter, times(1)).doSomething1();
    }
}
