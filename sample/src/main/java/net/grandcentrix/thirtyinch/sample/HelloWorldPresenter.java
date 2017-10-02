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

package net.grandcentrix.thirtyinch.sample;

import android.support.annotation.NonNull;
import java.util.concurrent.TimeUnit;
import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.rx.RxTiPresenterSubscriptionHandler;
import net.grandcentrix.thirtyinch.rx.RxTiPresenterUtils;
import rx.Observable;
import rx.Subscription;
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
    protected void onCreate() {
        super.onCreate();

        mText.onNext("Hello World!");

        rxSubscriptionHelper.manageSubscription(Observable.interval(0, 1, TimeUnit.SECONDS)
                .compose(RxTiPresenterUtils.deliverLatestToView(this))
                .subscribe(uptime -> {
                    sendToView(view -> view.showPresenterUpTime(uptime));
                }));

        rxSubscriptionHelper.manageSubscription(triggerHeavyCalculation
                .onBackpressureDrop(aVoid -> mText.onNext("Don't hurry me!"))
                .doOnNext(aVoid -> mText.onNext("calculating next number..."))
                .flatMap(new Func1<Void, Observable<Integer>>() {
                    @Override
                    public Observable<Integer> call(final Void aVoid) {
                        return increaseCounter();
                    }
                }, 1)
                .doOnNext(integer -> mText.onNext("Count: " + mCounter))
                .subscribe());
    }

    @Override
    protected void onAttachView(@NonNull final HelloWorldView view) {
        super.onAttachView(view);

        final Subscription showTextSub = mText.asObservable().subscribe(view::showText);
        final Subscription onButtonClickSub = view.onButtonClicked()
                .subscribe(aVoid -> {
                    triggerHeavyCalculation.onNext(null);
                });

        rxSubscriptionHelper.manageViewSubscriptions(showTextSub, onButtonClickSub);
    }

    /**
     * fake a heavy calculation
     */
    private Observable<Integer> increaseCounter() {
        return Observable.just(mCounter)
                .subscribeOn(Schedulers.computation())
                // fake heavy calculation
                .delay(2, TimeUnit.SECONDS)
                .doOnNext(integer -> {
                    mCounter++;
                    mText.onNext("value: " + mCounter);
                });
    }
}
