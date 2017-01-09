package net.grandcentrix.thirtyinch.rx2;

import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.TiView;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.reactivex.observers.TestObserver;

import static org.mockito.Mockito.mock;

public class RxTiPresenterUtilsTest {

    private TiPresenter mPresenter;

    private TiView mView;

    @Before
    public void setUp() throws Exception {
        mView = mock(TiView.class);
        mPresenter = new TiPresenter() {
        };
    }

    @After
    public void tearDown() throws Exception {
        mPresenter = null;
        mView = null;
    }

    @Test
    public void testIsViewReady_AttachView_ShouldCallValueFalseTrue() throws Exception {
        mPresenter.create();

        final TestObserver<Boolean> test = RxTiPresenterUtils.isViewReady(mPresenter).test();

        mPresenter.attachView(mView);
        test.assertValues(false, true);
    }

    @Test
    public void testIsViewReady_BeforeAttachView_ShouldCallValueFalse() throws Exception {
        mPresenter.create();

        final TestObserver<Boolean> test = RxTiPresenterUtils.isViewReady(mPresenter).test();

        test.assertValue(false);
    }

    @Test
    public void testIsViewReady_DisposeBeforeAttachView_ShouldRemoveCallback() throws Exception {
        mPresenter.create();

        final TestObserver<Boolean> test = RxTiPresenterUtils.isViewReady(mPresenter).test();

        test.assertValue(false);
        test.dispose();
        test.isDisposed();
        mPresenter.attachView(mView);
        test.assertValue(false);
    }
}