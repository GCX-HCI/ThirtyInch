package net.grandcentrix.thirtyinch.rx;

import net.grandcentrix.thirtyinch.TiView;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.Collections;

import rx.Observable;
import rx.observers.TestSubscriber;

import static org.mockito.Mockito.mock;

@RunWith(JUnit4.class)
public class RxTiPresenterUtilsTest {

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
    public void testDeliverLatestCacheToViewViewNotReady() throws Exception {
        mPresenter.create();

        TestSubscriber<Integer> testSubscriber = new TestSubscriber<>();
        Observable.just(1, 2, 3)
                .compose(RxTiPresenterUtils.<Integer>deliverLatestCacheToView(mPresenter))
                .subscribe(testSubscriber);

        mPresenter.attachView(mView);

        testSubscriber.assertNotCompleted();
        testSubscriber.assertNoErrors();
        testSubscriber.assertReceivedOnNext(Collections.singletonList(3));
    }

    @Test
    public void testDeliverLatestCacheToViewViewReady() throws Exception {
        mPresenter.create();
        mPresenter.attachView(mView);

        TestSubscriber<Integer> testSubscriber = new TestSubscriber<>();
        Observable.just(1, 2, 3)
                .compose(RxTiPresenterUtils.<Integer>deliverLatestCacheToView(mPresenter))
                .subscribe(testSubscriber);

        testSubscriber.assertNotCompleted();
        testSubscriber.assertNoErrors();
        testSubscriber.assertReceivedOnNext(Arrays.asList(1, 2, 3));
    }

    @Test
    public void testDeliverLatestToViewViewNotReady() throws Exception {
        mPresenter.create();

        TestSubscriber<Integer> testSubscriber = new TestSubscriber<>();
        Observable.just(1, 2, 3)
                .compose(RxTiPresenterUtils.<Integer>deliverLatestToView(mPresenter))
                .subscribe(testSubscriber);

        mPresenter.attachView(mView);

        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
        testSubscriber.assertReceivedOnNext(Collections.singletonList(3));
    }

    @Test
    public void testDeliverLatestToViewViewReady() throws Exception {
        mPresenter.create();
        mPresenter.attachView(mView);

        TestSubscriber<Integer> testSubscriber = new TestSubscriber<>();
        Observable.just(1, 2, 3)
                .compose(RxTiPresenterUtils.<Integer>deliverLatestToView(mPresenter))
                .subscribe(testSubscriber);

        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
        testSubscriber.assertReceivedOnNext(Arrays.asList(1, 2, 3));
    }

    @Test
    public void testDeliverToViewViewNotReady() throws Exception {
        mPresenter.create();
        mPresenter.attachView(mView);

        TestSubscriber<Integer> testSubscriber = new TestSubscriber<>();
        Observable.just(1, 2, 3)
                .compose(RxTiPresenterUtils.<Integer>deliverToView(mPresenter))
                .subscribe(testSubscriber);

        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
        testSubscriber.assertReceivedOnNext(Arrays.asList(1, 2, 3));
    }

    @Test
    public void testDeliverToViewViewReady() throws Exception {
        mPresenter.create();
        mPresenter.attachView(mView);

        TestSubscriber<Integer> testSubscriber = new TestSubscriber<>();
        Observable.just(1, 2, 3)
                .compose(RxTiPresenterUtils.<Integer>deliverToView(mPresenter))
                .subscribe(testSubscriber);

        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
        testSubscriber.assertReceivedOnNext(Arrays.asList(1, 2, 3));
    }


}