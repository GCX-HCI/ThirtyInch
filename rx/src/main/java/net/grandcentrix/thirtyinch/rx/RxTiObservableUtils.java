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

import net.grandcentrix.thirtyinch.TiPresenter;

import rx.Observable;

import static net.grandcentrix.thirtyinch.rx.RxTiUtils.isViewReady;

public class RxTiObservableUtils {

    /**
     * Returns a transformer that will delay onNext, onError and onComplete emissions unless a view
     * become available. getView() is guaranteed to be != null during all emissions. This
     * transformer can only be used on application's main thread.
     * <p/>
     * If the transformer receives a next value while the previous value has not been delivered,
     * the
     * previous value will be dropped.
     * <p/>
     * The transformer will duplicate the latest onNext emission in case if a view has been
     * reattached.
     * <p/>
     * This operator ignores onComplete emission and never sends one.
     * <p/>
     * Use this operator when you need to show updatable data that needs to be cached in memory.
     *
     * @param <T>       a type of onNext value.
     * @param presenter the presenter waiting for the view
     * @return the delaying operator.
     */
    public static <T> Observable.Transformer<T, T> deliverLatestCacheToView(
            final TiPresenter presenter) {
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> observable) {
                return observable
                        .lift(OperatorSemaphore.<T>semaphoreLatestCache(isViewReady(presenter)));
            }
        };
    }

    /**
     * Returns a transformer that will delay onNext, onError and onComplete emissions unless a view
     * become available. getView() is guaranteed to be != null during all emissions. This
     * transformer can only be used on application's main thread.
     * <p/>
     * If this transformer receives a next value while the previous value has not been delivered,
     * the previous value will be dropped.
     * <p/>
     * Use this operator when you need to show updatable data.
     *
     * @param <T>       a type of onNext value.
     * @param presenter the presenter waiting for the view
     * @return the delaying operator.
     */
    public static <T> Observable.Transformer<T, T> deliverLatestToView(
            final TiPresenter presenter) {
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> observable) {
                return observable
                        .lift(OperatorSemaphore.<T>semaphoreLatest(isViewReady(presenter)));
            }
        };
    }

    /**
     * Returns a transformer that will delay onNext, onError and onComplete emissions unless a view
     * become available. getView() is guaranteed to be != null during all emissions. This
     * transformer can only be used on application's main thread. See the correct order:
     * <pre>
     * <code>
     *
     * .observeOn(AndroidSchedulers.mainThread())
     * .compose(this.&lt;T&gt;deliverToView())
     * </code>
     * </pre>
     * Use this operator if you need to deliver *all* emissions to a view, in example when you're
     * sending items into adapter one by one.
     *
     * @param <T>       a type of onNext value.
     * @param presenter the presenter waiting for the view
     * @return the delaying operator.
     */
    public static <T> Observable.Transformer<T, T> deliverToView(final TiPresenter presenter) {
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> observable) {
                return observable.lift(OperatorSemaphore.<T>semaphore(isViewReady(presenter)));
            }
        };
    }

}
