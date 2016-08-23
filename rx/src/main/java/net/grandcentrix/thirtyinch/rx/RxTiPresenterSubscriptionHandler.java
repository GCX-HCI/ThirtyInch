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

package net.grandcentrix.thirtyinch.rx;

import net.grandcentrix.thirtyinch.TiLifecycleObserver;
import net.grandcentrix.thirtyinch.TiPresenter;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

public class RxTiPresenterSubscriptionHandler {

    private TiPresenter mPresenter;

    private CompositeSubscription mPresenterSubscriptions = new CompositeSubscription();

    private CompositeSubscription mUiSubscriptions;

    public RxTiPresenterSubscriptionHandler(final TiPresenter presenter) {
        mPresenter = presenter;
        mPresenter.addLifecycleObserver(new TiLifecycleObserver() {
            @Override
            public void onChange(final TiPresenter.State state,
                    final boolean beforeLifecycleEvent) {
                if (state == TiPresenter.State.CREATED_WITH_DETACHED_VIEW && beforeLifecycleEvent) {
                    // unsubscribe all UI subscriptions created in wakeUp() and added
                    // via manageViewSubscription(Subscription)
                    if (mUiSubscriptions != null) {
                        mUiSubscriptions.unsubscribe();
                    }
                    // there is no reuse possible. recreation works fine
                    mUiSubscriptions = new CompositeSubscription();
                }

                if (state == TiPresenter.State.DESTROYED && beforeLifecycleEvent) {
                    mPresenterSubscriptions.unsubscribe();
                    mPresenterSubscriptions = null;
                }
            }
        });
    }

    /**
     * Add your subscriptions here and they will automatically unsubscribed when {@link
     * TiPresenter#destroy()} gets called
     *
     * @throws IllegalStateException when the presenter has reached {@link net.grandcentrix.thirtyinch.TiPresenter.State#DESTROYED}
     */
    public void manageSubscription(final Subscription subscription) {
        if (mPresenterSubscriptions == null) {
            throw new IllegalStateException("subscription handling doesn't work"
                    + " when the presenter has reached the DESTROYED state");
        }

        if (subscription.isUnsubscribed()) {
            return;
        }
        mPresenterSubscriptions.add(subscription);
    }

    /**
     * Add your subscriptions for View events to this method to get them automatically cleaned up
     * in {@link TiPresenter#sleep()}. typically call this in {@link TiPresenter#wakeUp()} where
     * you subscribe to the UI events.
     *
     * @throws IllegalStateException when no view is attached
     */
    public void manageViewSubscription(final Subscription subscription) {
        if (mUiSubscriptions == null) {
            throw new IllegalStateException("view subscriptions can't be handled"
                    + " when there is no view");
        }
        mUiSubscriptions.add(subscription);
    }
}
