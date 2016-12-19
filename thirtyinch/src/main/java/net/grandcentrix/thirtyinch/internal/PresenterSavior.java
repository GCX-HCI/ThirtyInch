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

package net.grandcentrix.thirtyinch.internal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import net.grandcentrix.thirtyinch.TiActivity;
import net.grandcentrix.thirtyinch.TiLog;
import net.grandcentrix.thirtyinch.TiPresenter;

import java.util.HashMap;

/**
 * Activities can be destroyed when the device runs out of memory. Sometimes it doesn't work to
 * save objects via {@code Activity#onRetainNonConfigurationInstance()} for example when the user
 * has enabled "Do not keep activities" in the developer options. This singleton holds strong
 * references to those presenters and returns them when needed.
 *
 * {@link TiActivity} is responsible to manage the references
 */
public enum PresenterSavior {

    INSTANCE;

    private static final String TAG = PresenterSavior.class.getSimpleName();

    @VisibleForTesting
    HashMap<String, TiPresenter> mPresenters = new HashMap<>();

    public void free(final String presenterId) {
        mPresenters.remove(presenterId);
    }

    @Nullable
    public TiPresenter recover(final String id) {
        return mPresenters.get(id);
    }

    public String safe(@NonNull final TiPresenter presenter) {
        final String id = presenter.getId();
        TiLog.v(TAG, "safe presenter with id " + id + " " + presenter);
        mPresenters.put(id, presenter);
        return id;
    }

    @VisibleForTesting
    void clear() {
        mPresenters.clear();
    }
}
