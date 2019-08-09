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


import android.app.Activity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import net.grandcentrix.thirtyinch.TiPresenter;

/**
 * Store for presenters to survive when their associated context gets destroyed (e.g. Activity or
 * Fragment).
 */
public interface TiPresenterSavior {

    /**
     * Frees a certain presenter from the store.
     *
     * @param presenterId the id of the presenter
     * @param host        host of the presenter, see {@link #save(TiPresenter, Object)}
     */
    void free(String presenterId, @NonNull Object host);

    /**
     * Gets a presenter from the store.
     *
     * @param presenterId the id of the presenter
     * @param host        host of the presenter, see {@link #save(TiPresenter, Object)}
     * @return the presenter of {@code null} if no presenter could be found
     */
    @Nullable
    TiPresenter recover(String presenterId, @NonNull Object host);

    /**
     * Stores a presenter in the store for a given host. When the host gets destroyed the presenter
     * will be destroyed automatically. For {@link Activity} the host is the {@link Activity}
     * itself, for {@link Fragment} the host is {@link Fragment#getHost()}
     *
     * @param presenter the presenter that should be stored
     * @param host      host of the presenter
     * @return the id of the stored presenter
     */
    String save(@NonNull TiPresenter presenter, @NonNull Object host);
}
