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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivityScopedPresenters {

    private final HashMap<String, TiPresenter> store = new HashMap<>();

    private final String TAG = ActivityScopedPresenters.class.getSimpleName() + "@" + Integer.toHexString(hashCode());

    public void clear() {
        final ArrayList<Map.Entry<String, TiPresenter>> entries = new ArrayList<>(store.entrySet());
        for (final Map.Entry<String, TiPresenter> entry : entries) {
            entry.getValue().destroy();
            store.remove(entry.getKey());
        }
    }

    public TiPresenter get(final String id) {
        return store.get(id);
    }

    public List<TiPresenter> getAll() {
        return new ArrayList<>(store.values());
    }

    public TiPresenter remove(final String id) {
        final TiPresenter presenter = store.remove(id);
        TiLog.d(TAG, "remove " + id + " " + presenter);
        return presenter;
    }

    public void save(final String id, final TiPresenter presenter) {
        TiLog.d(TAG, "save " + id + " " + presenter);
        store.put(id, presenter);
    }

}
