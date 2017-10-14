/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Konstantin Mikheev sirstripy-at-gmail-com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.grandcentrix.thirtyinch.rx;

import java.util.ArrayList;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;

/**
 * This operator delays onNext, onComplete and onError emissions until a True value received from a
 * given observable.
 * When the given observable emits False, the operator starts delaying emissions again.
 * <p/>
 * semaphoreLatest variant drops older not emitted onNext value if a new value has been received.
 * <p/>
 * semaphoreLatestCache keeps the latest value after emission and sends it on each True value
 * from a given observable received. This variant never emits onCompleted.
 *
 * @param <T> a type of onNext value
 * @author konmik https://github.com/konmik/nucleus/blob/4167820fbb4061d8a6f303de3903751a8e136793/nucleus/src/main/java/nucleus/presenter/OperatorSemaphore.java
 */
public class OperatorSemaphore<T> implements Observable.Operator<T, T> {

    private boolean cache;

    private Observable<Boolean> go;

    private boolean latest;

    /**
     * Returns an operator that delays onNext, onComplete and onError emissions until a True value
     * received from a given observable.
     * When the given observable emits False, the operator starts delaying emissions again.
     *
     * @param go  an operator that controls emission.
     * @param <T> a type of onNext value.
     * @return an operator that delays onNext, onComplete and onError emissions until a True value
     * received from a given observable.
     * When the given observable emits False, the operator starts delaying emissions again.
     */
    public static <T> OperatorSemaphore<T> semaphore(Observable<Boolean> go) {
        return new OperatorSemaphore<>(go);
    }

    /**
     * Returns an operator that delays onNext, onComplete and onError emissions until a True value
     * received from a given observable.
     * When the given observable emits False, the operator starts delaying emissions again.
     * <p/>
     * This variant drops older not emitted value if a new value has been received.
     *
     * @param go  an operator that controls emission.
     * @param <T> a type of onNext value.
     * @return an operator that delays onNext, onComplete and onError emissions until a True value
     * received from a given observable.
     * When the given observable emits False, the operator starts delaying emissions again.
     * <p/>
     * This variant drops older not emitted value if a new value has been received.
     */
    public static <T> OperatorSemaphore<T> semaphoreLatest(Observable<Boolean> go) {
        return new OperatorSemaphore<>(go, true);
    }

    /**
     * Returns an operator that delays onNext, onComplete and onError emissions until a True value
     * received from a given observable.
     * When the given observable emits False, the operator starts delaying emissions again.
     * <p/>
     * This variant drops older not emitted value if a new value has been received.
     * <p/>
     * It also keeps the latest value after emission and sends it on each True value
     * from a given observable received. This variant never emits onCompleted.
     *
     * @param go  an operator that controls emission.
     * @param <T> a type of onNext value.
     * @return an operator that delays onNext, onComplete and onError emissions until a True value
     * received from a given observable.
     * When the given observable emits False, the operator starts delaying emissions again.
     * <p/>
     * This variant drops older not emitted value if a new value has been received.
     * <p/>
     * It also keeps the latest value after emission and sends it on each True value
     * from a given observable received. This variant never emits onCompleted.
     */
    public static <T> OperatorSemaphore<T> semaphoreLatestCache(Observable<Boolean> go) {
        return new OperatorSemaphore<>(go, true, true);
    }

    private OperatorSemaphore(Observable<Boolean> go) {
        this.go = go;
    }

    private OperatorSemaphore(Observable<Boolean> go, boolean latest) {
        this.go = go;
        this.latest = latest;
    }

    private OperatorSemaphore(Observable<Boolean> go, boolean latest, boolean cache) {
        this.go = go;
        this.latest = latest;
        this.cache = cache;
    }

    @Override
    public Subscriber<? super T> call(final Subscriber<? super T> child) {
        return new Subscriber<T>() {

            boolean completed; // should SafeSubscriber be used instead?

            boolean deliverCompleted;

            boolean deliverError;

            Throwable error;

            boolean hasCache;

            boolean isOpen;

            ArrayList<T> next = new ArrayList<>();

            T nextCache;

            @Override
            public void onStart() {
                super.onStart();
                child.add(go.subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean open) {
                        isOpen = open;
                        tick(cache);
                    }
                }));
                child.add(this);
            }

            @Override
            public void onCompleted() {
                if (!cache) {
                    deliverCompleted = true;
                    tick(false);
                }
                unsubscribe();
            }

            @Override
            public void onError(Throwable e) {
                error = e;
                deliverError = true;
                tick(false);
                unsubscribe();
            }

            @Override
            public void onNext(T o) {
                if (latest) {
                    next.clear();
                }
                next.add(o);
                tick(false);
            }

            void tick(boolean deliverCache) {
                if (!child.isUnsubscribed() && isOpen && !completed) {

                    while (next.size() > 0) {
                        T value = next.remove(0);
                        child.onNext(value);
                        deliverCache = false;
                        if (cache) {
                            nextCache = value;
                            hasCache = true;
                        }
                    }

                    if (deliverCache && hasCache) {
                        child.onNext(nextCache);
                    }

                    if (deliverCompleted) {
                        child.onCompleted();
                        completed = true;
                    }

                    if (deliverError) {
                        child.onError(error);
                        completed = true;
                    }
                }
            }
        };
    }
}