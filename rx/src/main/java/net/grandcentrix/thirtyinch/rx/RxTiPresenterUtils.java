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

public class RxTiPresenterUtils {

    /**
     * Deprecated. Please use {@link RxTiObservableUtils#deliverLatestCacheToView(TiPresenter)}.
     */
    @Deprecated
    public static <T> Observable.Transformer<T, T> deliverLatestCacheToView(
            final TiPresenter presenter) {
        return RxTiObservableUtils.deliverLatestCacheToView(presenter);
    }

    /**
     * Deprecated. Please use {@link RxTiObservableUtils#deliverLatestToView(TiPresenter)}.
     */
    @Deprecated
    public static <T> Observable.Transformer<T, T> deliverLatestToView(
            final TiPresenter presenter) {
        return RxTiObservableUtils.deliverLatestCacheToView(presenter);
    }

    /**
     * @deprecated use {@link RxTiObservableUtils#deliverToView(TiPresenter)} instead
     */
    public static <T> Observable.Transformer<T, T> deliverToView(final TiPresenter presenter) {
        return RxTiObservableUtils.deliverToView(presenter);
    }

    /**
     * @deprecated use {@link RxTiUtils#isViewReady(TiPresenter)} instead
     */
    public static Observable<Boolean> isViewReady(final TiPresenter presenter) {
        return RxTiUtils.isViewReady(presenter);
    }
}
