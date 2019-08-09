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

import androidx.annotation.NonNull;
import net.grandcentrix.thirtyinch.TiActivity;
import net.grandcentrix.thirtyinch.TiView;

/**
 * The {@link TiActivity} itself doesn't not have to implement
 * the {@link TiView} even though it's the default implementation. This interface allows the
 * possible separation.
 */
public interface TiViewProvider<V extends TiView> {

    /**
     * @return the {@link TiView} for the {@link net.grandcentrix.thirtyinch.TiPresenter}
     */
    @NonNull
    V provideView();
}
