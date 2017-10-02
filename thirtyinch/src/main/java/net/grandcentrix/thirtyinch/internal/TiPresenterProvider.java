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

import android.support.annotation.NonNull;
import net.grandcentrix.thirtyinch.TiPresenter;

/**
 * Provides access to a new instance of a {@link TiPresenter} which will be attached to
 * {@link net.grandcentrix.thirtyinch.TiActivity} or
 * {@link net.grandcentrix.thirtyinch.TiFragment}
 */
public interface TiPresenterProvider<P extends TiPresenter> {

    /**
     * Must return a new {@link net.grandcentrix.thirtyinch.TiPresenter} instance in {@link
     * net.grandcentrix.thirtyinch.TiPresenter.State#INITIALIZED} state. Never return a {@link
     * TiPresenter} which was already instantiated or is already destroyed.
     * <p>
     * Called only once per Activity when the presenter should be retained. One call per Activity
     * instance is expected when the retain is disabled
     * </p>
     * <p>
     * Can be called multiple times per Fragment. The Presenter gets destroyed when the Fragment is
     * removed from the {@link android.support.v4.app.FragmentManager}. When the {@link
     * android.support.v4.app.Fragment} gets added again this method is called to create a new
     * presenter.
     * </p>
     */
    @NonNull
    P providePresenter();
}
