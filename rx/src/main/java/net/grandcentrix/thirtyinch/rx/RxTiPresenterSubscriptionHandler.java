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

import net.grandcentrix.thirtyinch.TiLifecycleObserver;
import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.TiView;

import android.support.annotation.NonNull;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

public class RxTiPresenterSubscriptionHandler {

    private CompositeSubscription mPresenterSubscriptions = new CompositeSubscription();

    private CompositeSubscription mUiSubscriptions;

    public RxTiPresenterSubscriptionHandler(final TiPresenter presenter) {
        presenter.addLifecycleObserver(new TiLifecycleObserver() {
            @Override
            public void onChange(final TiPresenter.State state,
                    final boolean beforeLifecycleEvent) {
                if (state == TiPresenter.State.VIEW_DETACHED && beforeLifecycleEvent) {
                    // unsubscribe all UI subscriptions created in onAttachView() and added
                    // via manageViewSubscription(Subscription)
                    if (mUiSubscriptions != null) {
                        mUiSubscriptions.unsubscribe();
                        mUiSubscriptions = null;
                    }
                }

                if (state == TiPresenter.State.VIEW_ATTACHED && beforeLifecycleEvent) {
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
     * Add your subscriptions here and they will automatically unsubscribed when
     * {@link TiPresenter#destroy()} gets called
     *
     * @throws IllegalStateException when the presenter has reached {@link net.grandcentrix.thirtyinch.TiPresenter.State#DESTROYED}
     */
    public void manageSubscription(@NonNull final Subscription... subscriptions) {
        if (mPresenterSubscriptions == null) {
            throw new IllegalStateException("subscription handling doesn't work"
                    + " when the presenter has reached the DESTROYED state");
        }

        addSubscriptions(mPresenterSubscriptions, subscriptions);
    }

    /**
     * Add your subscriptions for View events to this method to get them automatically cleaned up
     * in {@link TiPresenter#detachView()}. typically call this in {@link
     * TiPresenter#attachView(TiView)} where you subscribe to the UI events.
     *
     * @throws IllegalStateException when no view is attached
     */
    public void manageViewSubscription(@NonNull final Subscription... subscriptions) {
        if (mUiSubscriptions == null) {
            throw new IllegalStateException("view subscriptions can't be handled"
                    + " when there is no view");
        }

        addSubscriptions(mUiSubscriptions, subscriptions);
    }

    /**
     * Adds all subscriptions to the given compositeSubscription if not already unsubscribed
     */
    private static void addSubscriptions(final CompositeSubscription compositeSubscription,
            final Subscription... subscriptions) {
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < subscriptions.length; i++) {
            final Subscription subscription = subscriptions[i];
            if (subscription.isUnsubscribed()) {
                continue;
            }

            compositeSubscription.add(subscriptions[i]);
        }
    }

}
