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

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashMap;

public class MockSavior implements TiPresenterSavior {

    private HashMap<String, TiPresenter> mPresenters = new HashMap<>();

    public void clear() {
        mPresenters.clear();
    }

    @Override
    public void free(final String presenterId, @NonNull Activity activity) {
        mPresenters.remove(presenterId);
    }

    public int presenterCount() {
        return mPresenters.size();
    }

    @Nullable
    @Override
    public TiPresenter recover(final String presenterId, @NonNull Activity activity) {
        return mPresenters.get(presenterId);
    }

    @Override
    public String save(@NonNull final TiPresenter presenter, @NonNull Activity activity) {
        final String id = presenter.getClass().getSimpleName()
                + ":" + presenter.hashCode()
                + ":" + System.nanoTime();
        mPresenters.put(id, presenter);
        return id;
    }
}
