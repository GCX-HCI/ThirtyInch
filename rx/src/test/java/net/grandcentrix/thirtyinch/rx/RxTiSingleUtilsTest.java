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

import net.grandcentrix.thirtyinch.TiView;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import rx.Single;
import rx.observers.TestSubscriber;

import static org.mockito.Mockito.mock;

@RunWith(JUnit4.class)
public class RxTiSingleUtilsTest {

    private TiMockPresenter mMockPresenter;

    private TiView mMockView;

    private TestSubscriber<String> mTestSubscriber;

    @Before
    public void setUp() throws Exception {
        mMockPresenter = new TiMockPresenter();
        mMockPresenter.create();

        mMockView = mock(TiView.class);

        mTestSubscriber = new TestSubscriber<>();
    }

    @After
    public void tearDown() throws Exception {
        mMockPresenter = null;
        mMockView = null;
        mTestSubscriber = null;
    }

    @Test
    public void testDeliverToView_AttachAfterSingleCreated_ShouldDeliver() throws Exception {
        Single.just("PleaseDeliverMe")
                .compose(RxTiSingleUtils.<String>deliverToView(mMockPresenter))
                .subscribe(mTestSubscriber);

        mMockPresenter.attachView(mMockView);

        mTestSubscriber.assertNoErrors();
        mTestSubscriber.assertValue("PleaseDeliverMe");
        mTestSubscriber.assertCompleted();
    }

    @Test
    public void testDeliverToView_WithAttachedView_ShouldDeliver() throws Exception {
        mMockPresenter.attachView(mMockView);

        Single.just("PleaseDeliverMe")
                .compose(RxTiSingleUtils.<String>deliverToView(mMockPresenter))
                .subscribe(mTestSubscriber);

        mTestSubscriber.assertNoErrors();
        mTestSubscriber.assertValue("PleaseDeliverMe");
        mTestSubscriber.assertCompleted();
    }

    @Test
    public void testDeliverToView_WithoutAttachedView_ShouldNotDeliver() throws Exception {
        Single.just("PleaseDeliverMe")
                .compose(RxTiSingleUtils.<String>deliverToView(mMockPresenter))
                .subscribe(mTestSubscriber);

        mTestSubscriber.assertNoErrors();
        mTestSubscriber.assertNoValues();
        mTestSubscriber.assertNotCompleted();
    }
}