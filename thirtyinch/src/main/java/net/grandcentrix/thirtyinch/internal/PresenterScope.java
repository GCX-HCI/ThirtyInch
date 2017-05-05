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


import net.grandcentrix.thirtyinch.TiLog;
import net.grandcentrix.thirtyinch.TiPresenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple wrapper around a {@link HashMap} to save {@link TiPresenter} by id. For every host of a
 * {@link TiPresenter} (i.e. {@link android.app.Activity}) a corresponding {@link PresenterScope}
 * will be created.
 * It contains the {@link TiPresenter} of the Activity itself and of all of its Fragments.
 */
public class PresenterScope {

    private final String TAG = PresenterScope.class.getSimpleName()
            + "@" + Integer.toHexString(hashCode());

    private final HashMap<String, TiPresenter> mStore = new HashMap<>();

    public TiPresenter get(final String id) {
        return mStore.get(id);
    }

    public List<TiPresenter> getAll() {
        return Collections.unmodifiableList(new ArrayList<>(mStore.values()));
    }

    public List<Map.Entry<String, TiPresenter>> getAllMappings() {
        return Collections.unmodifiableList(new ArrayList<>(mStore.entrySet()));
    }

    public boolean isEmpty() {
        return mStore.isEmpty();
    }

    public TiPresenter remove(final String id) {
        final TiPresenter presenter = mStore.remove(id);
        TiLog.d(TAG, "remove " + id + " " + presenter);
        return presenter;
    }

    public void save(final String id, final TiPresenter presenter) {
        TiLog.d(TAG, "save " + id + " " + presenter);
        mStore.put(id, presenter);
    }

    public int size() {
        return mStore.size();
    }

}
