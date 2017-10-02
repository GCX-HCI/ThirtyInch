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

package net.grandcentrix.thirtyinch.rx;

import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import net.grandcentrix.thirtyinch.TiView;
import org.junit.*;
import org.junit.runner.*;
import org.junit.runners.*;
import rx.Observable;
import rx.observers.TestSubscriber;

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