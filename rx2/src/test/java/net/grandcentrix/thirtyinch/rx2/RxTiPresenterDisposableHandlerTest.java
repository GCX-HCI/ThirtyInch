/*
 * Copyright (C) 2016 grandcentrix GmbH
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

package net.grandcentrix.thirtyinch.rx2;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.TestObserver;
import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.TiView;
import org.junit.*;
import org.junit.runner.*;
import org.junit.runners.*;

@RunWith(JUnit4.class)
public class RxTiPresenterDisposableHandlerTest {

    private RxTiPresenterDisposableHandler mDisposableHandler;

    private TiPresenter<TiView> mPresenter;

    private TiView mView;

    @Before
    public void setUp() throws Exception {
        mPresenter = new TiPresenter<TiView>() {
        };
        mDisposableHandler = new RxTiPresenterDisposableHandler(mPresenter);
        mView = mock(TiView.class);
    }

    @Test
    public void testManageDisposable_AfterDestroy_ShouldThrowIllegalState() throws Exception {
        mPresenter.create();
        mPresenter.destroy();
        final TestObserver<Object> testObserver = new TestObserver<>();

        try {
            mDisposableHandler.manageDisposable(testObserver);
            fail("no exception");
        } catch (IllegalStateException e) {
            assertThat(e).hasMessageContaining("DESTROYED");
        }
    }

    @Test
    public void testManageDisposable_ShouldReturnSameDisposable() throws Exception {
        mPresenter.create();
        mPresenter.attachView(mView);
        final TestObserver<Integer> testObserver = new TestObserver<>();

        final Disposable disposable = mDisposableHandler.manageDisposable(testObserver);

        assertThat(testObserver).isEqualTo(disposable);
    }

    @Test
    public void testManageDisposable_WithAlreadyDisposedDisposable_ShouldDoNothing()
            throws Exception {
        final TestObserver<Integer> testObserver = new TestObserver<>();
        testObserver.dispose();
        assertThat(testObserver.isDisposed()).isTrue();

        mDisposableHandler.manageDisposable(testObserver);

        assertThat(testObserver.isDisposed()).isTrue();
    }

    @Test
    public void testManageDisposable_WithDestroy_ShouldDispose() throws Exception {
        mPresenter.create();
        final TestObserver<Integer> testObserver = new TestObserver<>();

        mDisposableHandler.manageDisposable(testObserver);
        assertThat(testObserver.isDisposed()).isFalse();

        mPresenter.destroy();
        assertThat(testObserver.isDisposed()).isTrue();
    }

    @Test
    public void testManageDisposables_WithOneAlreadyDisposed_ShouldNotAddToDisposable()
            throws Exception {
        mPresenter.create();
        final TestObserver<Integer> firstTestObserver = new TestObserver<>();
        final TestObserver<Integer> secondTestObserver = new TestObserver<>();
        secondTestObserver.dispose();

        mDisposableHandler.manageDisposables(firstTestObserver, secondTestObserver);

        assertThat(firstTestObserver.isDisposed()).isFalse();
        assertThat(secondTestObserver.isDisposed()).isTrue();
    }

    @Test
    public void testManageViewDisposable_ShouldReturnSameDisposable() throws Exception {
        mPresenter.create();
        mPresenter.attachView(mView);
        final TestObserver<Integer> testObserver = new TestObserver<>();

        final Disposable disposable = mDisposableHandler.manageViewDisposable(testObserver);

        assertThat(testObserver).isEqualTo(disposable);
    }

    @Test
    public void testManageViewDisposable_WithDetachSingleDispose_ShouldDispose()
            throws Exception {
        mPresenter.create();
        mPresenter.attachView(mView);
        final TestObserver<Integer> testObserver = new TestObserver<>();

        mDisposableHandler.manageViewDisposable(testObserver);
        assertThat(testObserver.isDisposed()).isFalse();

        mPresenter.detachView();
        assertThat(testObserver.isDisposed()).isTrue();
    }

    @Test
    public void testManageViewDisposable_manageAfterDetach_ShouldThrowIllegalStateException()
            throws Exception {
        mPresenter.create();
        mPresenter.attachView(mView);
        mPresenter.detachView();

        final TestObserver<Integer> testObserver = new TestObserver<>();

        try {
            mDisposableHandler.manageViewDisposable(testObserver);
            fail("no exception");
        } catch (Exception e) {
            assertThat(e).hasMessageContaining("when there is no view");
        }
    }

    @Test
    public void testManageViewDisposable_manageBeforeViewAttached_ShouldThrowIllegalStateException()
            throws Exception {
        mPresenter.create();
        final TestObserver<Integer> testObserver = new TestObserver<>();

        try {
            mDisposableHandler.manageViewDisposable(testObserver);
            fail("no exception");
        } catch (Exception e) {
            assertThat(e).hasMessageContaining("when there is no view");
        }
    }

    @Test
    public void testManageViewDisposables_WithOneAlreadyDisposed_ShouldNotAddToDisposable()
            throws Exception {
        mPresenter.create();
        mPresenter.attachView(mView);
        final TestObserver<Integer> firstTestObserver = new TestObserver<>();
        final TestObserver<Integer> secondTestObserver = new TestObserver<>();
        secondTestObserver.dispose();

        mDisposableHandler.manageViewDisposables(firstTestObserver, secondTestObserver);

        assertThat(firstTestObserver.isDisposed()).isFalse();
        assertThat(secondTestObserver.isDisposed()).isTrue();
    }

    @Test
    public void testManageViewSubscription_InOnDetachView_ShouldThrow() throws Exception {
        final TiPresenter presenter = new TiPresenter() {

            private RxTiPresenterDisposableHandler mSubscriptionHandler =
                    new RxTiPresenterDisposableHandler(this);

            @Override
            protected void onDetachView() {
                super.onDetachView();
                mSubscriptionHandler.manageViewDisposable(Observable.just("test").subscribe());
            }
        };
        presenter.create();
        presenter.attachView(mView);

        try {
            presenter.detachView();
            fail("did not throw");
        } catch (Throwable e) {
            assertThat(e).hasMessageContaining("no view");
        }
    }

    @Test
    public void testManagerDisposables_Destroy_ShouldDispose() throws Exception {
        mPresenter.create();
        final TestObserver<Integer> firstTestObserver = new TestObserver<>();
        final TestObserver<Integer> secondTestObserver = new TestObserver<>();
        final TestObserver<Integer> thirdTestObserver = new TestObserver<>();

        mDisposableHandler
                .manageDisposables(firstTestObserver, secondTestObserver, thirdTestObserver);

        mPresenter.destroy();
        assertThat(firstTestObserver.isDisposed()).isTrue();
        assertThat(secondTestObserver.isDisposed()).isTrue();
        assertThat(thirdTestObserver.isDisposed()).isTrue();
    }

    @Test
    public void testManagerDisposables_WithDestroyed_ShouldThrow() throws Exception {
        mPresenter.create();
        mPresenter.destroy();
        final TestObserver<Integer> firstTestObserver = new TestObserver<>();
        final TestObserver<Integer> secondTestObserver = new TestObserver<>();
        final TestObserver<Integer> thirdTestObserver = new TestObserver<>();

        try {
            mDisposableHandler
                    .manageDisposables(firstTestObserver, secondTestObserver, thirdTestObserver);
            fail("no exception");
        } catch (IllegalStateException e) {
            assertThat(e).hasMessageContaining("DESTROYED");
        }
    }

    @Test
    public void testManagerViewDisposables_WithDetach_ShouldDispose() throws Exception {
        mPresenter.create();
        mPresenter.attachView(mView);
        final TestObserver<Integer> firstTestObserver = new TestObserver<>();
        final TestObserver<Integer> secondTestObserver = new TestObserver<>();
        final TestObserver<Integer> thirdTestObserver = new TestObserver<>();

        mDisposableHandler
                .manageViewDisposables(firstTestObserver, secondTestObserver, thirdTestObserver);
        assertThat(firstTestObserver.isDisposed()).isFalse();
        assertThat(secondTestObserver.isDisposed()).isFalse();
        assertThat(thirdTestObserver.isDisposed()).isFalse();

        mPresenter.detachView();
        assertThat(firstTestObserver.isDisposed()).isTrue();
        assertThat(secondTestObserver.isDisposed()).isTrue();
        assertThat(thirdTestObserver.isDisposed()).isTrue();
    }
}