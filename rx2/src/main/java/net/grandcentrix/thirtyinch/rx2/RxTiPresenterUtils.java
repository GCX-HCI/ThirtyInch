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

package net.grandcentrix.thirtyinch.rx2;

import net.grandcentrix.thirtyinch.TiPresenter;

import io.reactivex.Observable;

public class RxTiPresenterUtils {

    /**
     * @deprecated use {@link RxTiUtils#isViewReady(TiPresenter)} instead
     */
    public static Observable<Boolean> isViewReady(final TiPresenter presenter) {
        return RxTiUtils.isViewReady(presenter);
    }

}
