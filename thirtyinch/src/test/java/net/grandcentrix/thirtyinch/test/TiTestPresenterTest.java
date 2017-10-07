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
import org.mockito.*;
import org.mockito.junit.*;

public class TiTestPresenterTest {

    interface MockTiView extends TiView {

        void helloWorld();

    }

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private TiPresenter<TiView> mMockPresenter;

    private TiPresenter<MockTiView> mMockTiPresenter;

    private MockTiView mMockTiView;

    @Mock
    private TiView mMockView;

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

        // Given the presenter is currently in the state VIEW_DETACHED.
        when(mMockPresenter.getState()).thenReturn(State.VIEW_DETACHED);

        final TiTestPresenter<TiView> tiTestPresenter = new TiTestPresenter<>(mMockPresenter);

        // When a new View is attached to the TiTestPresenter.
        tiTestPresenter.attachView(mMockView);

        // Then the TiTestPresenter should set any ui thread executor on the Presenter.
        verify(mMockPresenter).setUiThreadExecutor(any(Executor.class));

        // And then the TiTestPresenter should attach the new View to the Presenter.
        verify(mMockPresenter).attachView(mMockView);
    }

    @Test
    public void testAttachView_WithAttachedView_ShouldDetachPreviousView() {

        // Given the presenter is currently in the state VIEW_ATTACHED.
        when(mMockPresenter.getState()).thenReturn(State.VIEW_ATTACHED);

        final TiTestPresenter<TiView> tiTestPresenter = new TiTestPresenter<>(mMockPresenter);

        // When a new View is attached to the TiTestPresenter.
        tiTestPresenter.attachView(mMockTiView);

        // Then the TiTestPresenter should call detachView() on the Presenter.
        verify(mMockPresenter).detachView();
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
        final TiTestPresenter<MockTiView> testPresenter = presenter.test();

        final MockTiView view = mock(MockTiView.class);
        testPresenter.attachView(view);

        verify(view).helloWorld();
    }
}