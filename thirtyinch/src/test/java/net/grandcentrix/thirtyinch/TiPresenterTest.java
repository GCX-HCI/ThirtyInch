package net.grandcentrix.thirtyinch;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import rx.Observable;
import rx.observers.TestSubscriber;

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
    public void testDeliverLatestCacheToViewViewNotReady() throws Exception {
        mPresenter.create();
        mPresenter.bindNewView(mView);

        TestSubscriber<Integer> testSubscriber = new TestSubscriber<>();
        Observable.just(1, 2, 3)
                .compose(mPresenter.<Integer>deliverLatestCacheToView())
                .subscribe(testSubscriber);

        testSubscriber.assertNotCompleted();
        testSubscriber.assertNoErrors();
        testSubscriber.assertNoValues();

        mPresenter.wakeUp();

        testSubscriber.assertNotCompleted();
        testSubscriber.assertNoErrors();
        testSubscriber.assertReceivedOnNext(Collections.singletonList(3));
    }

    @Test
    public void testDeliverLatestCacheToViewViewReady() throws Exception {
        mPresenter.create();
        mPresenter.bindNewView(mView);

        mPresenter.wakeUp();
        TestSubscriber<Integer> testSubscriber = new TestSubscriber<>();
        Observable.just(1, 2, 3)
                .compose(mPresenter.<Integer>deliverLatestCacheToView())
                .subscribe(testSubscriber);

        testSubscriber.assertNotCompleted();
        testSubscriber.assertNoErrors();
        testSubscriber.assertReceivedOnNext(Arrays.asList(1, 2, 3));
    }

    @Test
    public void testDeliverLatestToViewViewNotReady() throws Exception {
        mPresenter.create();
        mPresenter.bindNewView(mView);

        TestSubscriber<Integer> testSubscriber = new TestSubscriber<>();
        Observable.just(1, 2, 3)
                .compose(mPresenter.<Integer>deliverLatestToView())
                .subscribe(testSubscriber);

        testSubscriber.assertNotCompleted();
        testSubscriber.assertNoErrors();
        testSubscriber.assertNoValues();

        mPresenter.wakeUp();

        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
        testSubscriber.assertReceivedOnNext(Collections.singletonList(3));
    }

    @Test
    public void testDeliverLatestToViewViewReady() throws Exception {
        mPresenter.create();
        mPresenter.bindNewView(mView);

        mPresenter.wakeUp();
        TestSubscriber<Integer> testSubscriber = new TestSubscriber<>();
        Observable.just(1, 2, 3)
                .compose(mPresenter.<Integer>deliverLatestToView())
                .subscribe(testSubscriber);

        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
        testSubscriber.assertReceivedOnNext(Arrays.asList(1, 2, 3));
    }

    @Test
    public void testDeliverToViewViewNotReady() throws Exception {
        mPresenter.create();
        mPresenter.bindNewView(mView);

        TestSubscriber<Integer> testSubscriber = new TestSubscriber<>();
        Observable.just(1, 2, 3)
                .compose(mPresenter.<Integer>deliverToView())
                .subscribe(testSubscriber);

        testSubscriber.assertNotCompleted();
        testSubscriber.assertNoErrors();
        testSubscriber.assertNoValues();

        mPresenter.wakeUp();

        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
        testSubscriber.assertReceivedOnNext(Arrays.asList(1, 2, 3));
    }

    @Test
    public void testDeliverToViewViewReady() throws Exception {
        mPresenter.create();
        mPresenter.bindNewView(mView);

        mPresenter.wakeUp();
        TestSubscriber<Integer> testSubscriber = new TestSubscriber<>();
        Observable.just(1, 2, 3)
                .compose(mPresenter.<Integer>deliverToView())
                .subscribe(testSubscriber);

        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
        testSubscriber.assertReceivedOnNext(Arrays.asList(1, 2, 3));
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

    @Test
    public void testManageSubscription() throws Exception {
        mPresenter.create();
        TestSubscriber<Integer> testSubscriber = new TestSubscriber<>();

        mPresenter.manageSubscription(testSubscriber);

        assertThat(testSubscriber.isUnsubscribed(), equalTo(false));

        mPresenter.destroy();

        testSubscriber.assertUnsubscribed();
    }

    @Test
    public void testManageSubscriptionDestroyed() throws Exception {
        mPresenter.create();
        mPresenter.destroy();
        TestSubscriber<Integer> testSubscriber = new TestSubscriber<>();

        mPresenter.manageSubscription(testSubscriber);

        testSubscriber.assertUnsubscribed();
    }

    @Test
    public void testManageSubscriptionUnsubscribed() throws Exception {
        TestSubscriber<Integer> testSubscriber = new TestSubscriber<>();
        testSubscriber.unsubscribe();
        mPresenter.manageSubscription(testSubscriber);
        testSubscriber.assertUnsubscribed();
    }

    @Test
    public void testManageViewSubscription() throws Exception {
        mPresenter.create();
        mPresenter.wakeUp();
        TestSubscriber<Integer> testSubscriber = new TestSubscriber<>();

        mPresenter.manageViewSubscription(testSubscriber);

        assertThat(testSubscriber.isUnsubscribed(), equalTo(false));

        mPresenter.sleep();

        testSubscriber.assertUnsubscribed();
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

    @Test
    public void testSleep() throws Exception {
        mPresenter.create();
        mPresenter.bindNewView(mView);
        mPresenter.wakeUp();
        TestSubscriber<Integer> testSubscriber = new TestSubscriber<>();

        mPresenter.manageViewSubscription(testSubscriber);
        mPresenter.sleep();

        testSubscriber.assertUnsubscribed();
        assertThat(mPresenter.getView(), nullValue());
        assertThat(mPresenter.onSleepCalled, equalTo(1));
    }

    @Test
    public void testSleepBeforeWakeUp() throws Exception {
        mPresenter.create();
        mPresenter.bindNewView(mView);
        TestSubscriber<Integer> testSubscriber = new TestSubscriber<>();

        mPresenter.manageViewSubscription(testSubscriber);
        mPresenter.sleep();

        assertThat(testSubscriber.isUnsubscribed(), equalTo(false));
        assertThat(mPresenter.getView(), equalTo(mView));
        assertThat(mPresenter.onSleepCalled, equalTo(0));
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