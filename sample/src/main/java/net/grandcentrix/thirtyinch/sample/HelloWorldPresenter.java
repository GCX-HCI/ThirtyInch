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

package net.grandcentrix.thirtyinch.sample;

import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.rx.RxTiPresenterSubscriptionHandler;
import net.grandcentrix.thirtyinch.rx.RxTiPresenterUtils;

import android.support.annotation.NonNull;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public class HelloWorldPresenter extends TiPresenter<HelloWorldView> {

    private int mCounter = 0;

    private BehaviorSubject<String> mText = BehaviorSubject.create();

    private RxTiPresenterSubscriptionHandler rxSubscriptionHelper
            = new RxTiPresenterSubscriptionHandler(this);

    private PublishSubject<Void> triggerHeavyCalculation = PublishSubject.create();

    @Override
    protected void onAttachView(@NonNull final HelloWorldView view) {
        super.onAttachView(view);

        rxSubscriptionHelper.manageViewSubscription(mText.asObservable()
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(final String text) {
                        view.showText(text);
                    }
                }));

        rxSubscriptionHelper.manageViewSubscription(view.onButtonClicked()
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(final Void aVoid) {
                        triggerHeavyCalculation.onNext(null);
                    }
                }));
    }

    @Override
    protected void onCreate() {
        super.onCreate();

        mText.onNext("Hello World!");

        rxSubscriptionHelper.manageSubscription(Observable.interval(0, 1, TimeUnit.SECONDS)
                .compose(RxTiPresenterUtils.<Long>deliverLatestToView(this))
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(final Long uptime) {
                        getView().showPresenterUpTime(uptime);
                    }
                }));

        rxSubscriptionHelper.manageSubscription(triggerHeavyCalculation
                .doOnNext(new Action1<Void>() {
                    @Override
                    public void call(final Void aVoid) {
                        mText.onNext("calculating next number...");
                    }
                })
                .onBackpressureDrop(new Action1<Void>() {
                    @Override
                    public void call(final Void aVoid) {
                        mText.onNext("Don't hurry me!");
                    }
                })
                .flatMap(new Func1<Void, Observable<Integer>>() {
                    @Override
                    public Observable<Integer> call(final Void aVoid) {
                        return increaseCounter();
                    }
                }, 1)
                .doOnNext(new Action1<Integer>() {
                    @Override
                    public void call(final Integer integer) {
                        mText.onNext("Count: " + mCounter);
                    }
                })
                .subscribe());
    }

    /**
     * fake a heavy calculation
     */
    private Observable<Integer> increaseCounter() {
        return Observable.just(mCounter)
                .subscribeOn(Schedulers.computation())
                // fake heavy calculation
                .delay(2, TimeUnit.SECONDS)
                .doOnNext(new Action1<Integer>() {
                    @Override
                    public void call(final Integer integer) {
                        mCounter++;
                        mText.onNext("value: " + mCounter);
                    }
                });
    }
}
