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

package net.grandcentrix.thirtyinch.internal;

import net.grandcentrix.thirtyinch.TiPresenter;

import android.os.Bundle;
import android.support.annotation.NonNull;

public interface TiPresenterProvider<P extends TiPresenter> {

    /**
     * Function returning a {@link net.grandcentrix.thirtyinch.TiPresenter}
     * <p>
     * called only once when the Activity got initiated. Doesn't get called when the Activity gets
     * recreated. The old presenter will be recovered in {@link android.app.Activity#onCreate(Bundle)}.
     */
    @NonNull
    P providePresenter();
}
