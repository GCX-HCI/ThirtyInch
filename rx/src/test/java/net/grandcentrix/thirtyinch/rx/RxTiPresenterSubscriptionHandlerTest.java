package net.grandcentrix.thirtyinch.rx;

import net.grandcentrix.thirtyinch.TiView;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import rx.observers.TestSubscriber;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.Mockito.mock;

@RunWith(JUnit4.class)
public class RxTiPresenterSubscriptionHandlerTest {

    private TiMockPresenter mPresenter;

    private RxTiPresenterSubscriptionHandler mSubscriptionHandler;

    private TiView mView;

    @Before
    public void setUp() throws Exception {
        mView = mock(TiView.class);
        mPresenter = new TiMockPresenter();
        mSubscriptionHandler = new RxTiPresenterSubscriptionHandler(mPresenter);
    }

    @After
    public void tearDown() throws Exception {
        mPresenter = null;
        mView = null;
        mSubscriptionHandler = null;
    }

    @Test
    public void testDetach() throws Exception {
        mPresenter.create();
        mPresenter.attachView(mView);
        TestSubscriber<Integer> testSubscriber = new TestSubscriber<>();

        mSubscriptionHandler.manageViewSubscription(testSubscriber);
        mPresenter.detachView();

        testSubscriber.assertUnsubscribed();
        assertThat(mPresenter.getView(), nullValue());
        assertThat(mPresenter.onDetachCalled, equalTo(1));
    }

    @Test
    public void testDetachBeforeAttach() throws Exception {
        mPresenter.create();
        TestSubscriber<Integer> testSubscriber = new TestSubscriber<>();

        mSubscriptionHandler.manageViewSubscription(testSubscriber);
        mPresenter.detachView();

        assertThat(testSubscriber.isUnsubscribed(), equalTo(false));
        assertThat(mPresenter.onDetachCalled, equalTo(0));
    }

    @Test
    public void testManageSubscription() throws Exception {
        mPresenter.create();
        TestSubscriber<Integer> testSubscriber = new TestSubscriber<>();

        mSubscriptionHandler.manageSubscription(testSubscriber);

        assertThat(testSubscriber.isUnsubscribed(), equalTo(false));

        mPresenter.destroy();

        testSubscriber.assertUnsubscribed();
    }

    @Test
    public void testManageSubscriptionDestroyed() throws Exception {
        mPresenter.create();
        mPresenter.destroy();
        TestSubscriber<Integer> testSubscriber = new TestSubscriber<>();

        try {
            mSubscriptionHandler.manageSubscription(testSubscriber);
            Assert.fail("no exception");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), containsString("DESTROYED"));
        }
    }

    @Test
    public void testManageSubscriptionUnsubscribed() throws Exception {
        TestSubscriber<Integer> testSubscriber = new TestSubscriber<>();
        testSubscriber.unsubscribe();
        mSubscriptionHandler.manageSubscription(testSubscriber);
        testSubscriber.assertUnsubscribed();
    }

    @Test
    public void testManageViewSubscription() throws Exception {
        mPresenter.create();
        mPresenter.attachView(mock(TiView.class));
        TestSubscriber<Integer> testSubscriber = new TestSubscriber<>();

        mSubscriptionHandler.manageViewSubscription(testSubscriber);

        assertThat(testSubscriber.isUnsubscribed(), equalTo(false));

        mPresenter.detachView();

        testSubscriber.assertUnsubscribed();
    }

}