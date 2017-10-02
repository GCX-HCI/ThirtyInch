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

import android.support.annotation.NonNull;
import net.grandcentrix.thirtyinch.TiLifecycleObserver;
import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.TiView;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

public class RxTiPresenterSubscriptionHandler {

    private CompositeSubscription mPresenterSubscriptions = new CompositeSubscription();

    private CompositeSubscription mUiSubscriptions;

    public RxTiPresenterSubscriptionHandler(final TiPresenter presenter) {
        presenter.addLifecycleObserver(new TiLifecycleObserver() {
            @Override
            public void onChange(final TiPresenter.State state,
                    final boolean hasLifecycleMethodBeenCalled) {
                if (state == TiPresenter.State.VIEW_DETACHED && !hasLifecycleMethodBeenCalled) {
                    // unsubscribe all UI subscriptions created in onAttachView() and added
                    // via manageViewSubscriptions(Subscription)
                    if (mUiSubscriptions != null) {
                        mUiSubscriptions.unsubscribe();
                        mUiSubscriptions = null;
                    }
                }

                if (state == TiPresenter.State.VIEW_ATTACHED && !hasLifecycleMethodBeenCalled) {
                    mUiSubscriptions = new CompositeSubscription();
                }

                if (state == TiPresenter.State.DESTROYED && !hasLifecycleMethodBeenCalled) {
                    mPresenterSubscriptions.unsubscribe();
                    mPresenterSubscriptions = null;
                }
            }
        });
    }

    /**
     * Add your subscription here and they will automatically unsubscribed when
     * {@link TiPresenter#destroy()} gets called
     *
     * @throws IllegalStateException when the presenter has reached {@link net.grandcentrix.thirtyinch.TiPresenter.State#DESTROYED}
     * @see #manageSubscriptions(Subscription...)
     */
    public Subscription manageSubscription(@NonNull final Subscription subscription) {
        if (mPresenterSubscriptions == null) {
            throw new IllegalStateException("subscription handling doesn't work"
                    + " when the presenter has reached the DESTROYED state");
        }

        mPresenterSubscriptions.add(subscription);
        return subscription;
    }

    /**
     * Add your subscriptions here and they will automatically unsubscribed when
     * {@link TiPresenter#destroy()} gets called
     *
     * @throws IllegalStateException when the presenter has reached {@link net.grandcentrix.thirtyinch.TiPresenter.State#DESTROYED}
     * @see #manageSubscription(Subscription)
     */
    public void manageSubscriptions(@NonNull final Subscription... subscriptions) {
        for (int i = 0; i < subscriptions.length; i++) {
            manageSubscription(subscriptions[i]);
        }
    }

    /**
     * Add your subscription for View events to this method to get them automatically cleaned up
     * in {@link TiPresenter#detachView()}. Typically call this in
     * {@link TiPresenter#attachView(TiView)} where you subscribe to the UI events.
     *
     * @throws IllegalStateException when no view is attached
     * @see #manageViewSubscriptions(Subscription...)
     */
    public Subscription manageViewSubscription(@NonNull final Subscription subscription) {
        if (mUiSubscriptions == null) {
            throw new IllegalStateException("view subscription can't be handled"
                    + " when there is no view");
        }

        mUiSubscriptions.add(subscription);
        return subscription;
    }

    /**
     * Add your subscriptions for View events to this method to get them automatically cleaned up
     * in {@link TiPresenter#detachView()}. Typically call this in
     * {@link TiPresenter#attachView(TiView)} where you subscribe to the UI events.
     *
     * @throws IllegalStateException when no view is attached
     * @see #manageViewSubscription(Subscription)
     */
    public void manageViewSubscriptions(@NonNull final Subscription... subscriptions) {
        for (int i = 0; i < subscriptions.length; i++) {
            manageViewSubscription(subscriptions[i]);
        }
    }

}
