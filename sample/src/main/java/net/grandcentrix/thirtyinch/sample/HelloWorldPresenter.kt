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

package net.grandcentrix.thirtyinch.sample

import net.grandcentrix.thirtyinch.TiPresenter
import net.grandcentrix.thirtyinch.rx.RxTiPresenterSubscriptionHandler
import net.grandcentrix.thirtyinch.rx.RxTiPresenterUtils
import rx.Observable
import rx.schedulers.Schedulers
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class HelloWorldPresenter : TiPresenter<HelloWorldView>() {

    private var counter = 0
    private val textSubject = BehaviorSubject.create<String>()
    private val rxSubscriptionHelper = RxTiPresenterSubscriptionHandler(this)
    private val triggerHeavyCalculation = PublishSubject.create<Void>()

    override fun onCreate() {
        super.onCreate()

        textSubject.onNext("Hello World!")

        rxSubscriptionHelper.manageSubscription(Observable.interval(0, 1, TimeUnit.SECONDS)
                .compose(RxTiPresenterUtils.deliverLatestToView(this))
                .subscribe { uptime -> sendToView { view -> view.showPresenterUpTime(uptime) } })

        rxSubscriptionHelper.manageSubscription(triggerHeavyCalculation
                .onBackpressureDrop { textSubject.onNext("Don't hurry me!") }
                .doOnNext { textSubject.onNext("calculating next number...") }
                .flatMap({ increaseCounter() }, 1)
                .doOnNext { textSubject.onNext("Count: $counter") }
                .subscribe())
    }

    override fun onAttachView(view: HelloWorldView) {
        super.onAttachView(view)

        val showTextSub = textSubject.asObservable()
                .subscribe { view.showText(it) }
        val onButtonClickSub = view.onButtonClicked()
                .subscribe { triggerHeavyCalculation.onNext(null) }

        rxSubscriptionHelper.manageViewSubscriptions(showTextSub, onButtonClickSub)
    }

    /**
     * fake a heavy calculation
     */
    private fun increaseCounter(): Observable<Int> = Observable.just(counter)
            .subscribeOn(Schedulers.computation())
            // fake heavy calculation
            .delay(2, TimeUnit.SECONDS)
            .doOnNext {
                counter++
                textSubject.onNext("value: $counter")
            }
}
