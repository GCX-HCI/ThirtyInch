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

package net.grandcentrix.thirtyinch.test;

import static junit.framework.Assert.fail;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import android.support.annotation.NonNull;
import java.util.concurrent.Executor;
import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.TiPresenter.State;
import net.grandcentrix.thirtyinch.TiView;
import net.grandcentrix.thirtyinch.ViewAction;
import org.junit.*;

public class TiTestPresenterTest {

    interface MockTiView extends TiView {

        void helloWorld();

    }

    private TiPresenter<MockTiView> mMockTiPresenter;

    private MockTiView mMockTiView;

    @Before
    public void setUp() throws Exception {
        mMockTiPresenter = new TiPresenter<MockTiView>() {

            @Override
            protected void onAttachView(@NonNull MockTiView view) {
                super.onAttachView(view);
                sendToView(new ViewAction<MockTiView>() {
                    @Override
                    public void call(MockTiView tiView) {
                        tiView.helloWorld();
                    }
                });
            }
        };
        mMockTiPresenter.create();
        mMockTiView = mock(MockTiView.class);
    }

    @Test
    public void testAttachView_ShouldReplaceUIThreadExecutor() throws Exception {
        final TiPresenter mockPresenter = mock(TiPresenter.class);
        when(mockPresenter.getState()).thenReturn(State.VIEW_DETACHED);
        final TiTestPresenter<TiView> tiTestPresenter = new TiTestPresenter<TiView>(mockPresenter);
        tiTestPresenter.attachView(mMockTiView);

        verify(mockPresenter).setUiThreadExecutor(any(Executor.class));
        verify(mockPresenter).attachView(mMockTiView);
    }

    @Test
    public void testSendToView_InUnitTestWithTiTestPresenter_ShouldNotThrow() throws Exception {
        final TiTestPresenter<MockTiView> testPresenter = new TiTestPresenter<>(mMockTiPresenter);
        testPresenter.attachView(mMockTiView);

        verify(mMockTiView).helloWorld();
    }

    @Test
    public void testSendToView_InUnitTest_ShouldThrow() throws Exception {
        try {
            mMockTiPresenter.attachView(mMockTiView);
            fail("No exception");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage()).contains("no ui thread executor available");
        }
    }

    @Test
    public void testSimpleViewInvocationWithTestPresenter() throws Exception {
        final TiPresenter<MockTiView> presenter = new TiPresenter<MockTiView>() {
            @Override
            protected void onAttachView(@NonNull MockTiView view) {
                super.onAttachView(view);
                sendToView(new ViewAction<MockTiView>() {
                    @Override
                    public void call(MockTiView tiView) {
                        tiView.helloWorld();
                    }
                });
            }
        };
        presenter.test();

        final MockTiView view = mock(MockTiView.class);
        presenter.attachView(view);

        verify(view).helloWorld();
    }
}