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

package net.grandcentrix.thirtyinch.serialize;


import net.grandcentrix.thirtyinch.TiLog;
import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.TiPresenterSerializer;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.IOException;

public class PresenterStateSerializer implements TiPresenterSerializer {

    private static final String TAG = PresenterStateSerializer.class.getSimpleName();

    private final File mCacheDir;

    public PresenterStateSerializer(@NonNull final Context context) {
        mCacheDir = new File(context.getCacheDir(), "TiPresenterStates");
    }

    @NonNull
    @Override
    public byte[] deserialize(@NonNull final TiPresenter presenter) {
        final File file = getStateFile(presenter);
        TiLog.v(TAG, "deserialize " + file.getName());
        try {
            return FileUtils.readFile(file);
        } catch (IOException e) {
            TiLog.v(TAG, "could not read from file " + file.getName());
            e.printStackTrace();

        }
        return null;
    }

    @Override
    public void free(@NonNull final TiPresenter presenter) {
        final File file = getStateFile(presenter);
        TiLog.v(TAG, "free " + file.getName());
        try {
            FileUtils.delete(file);
        } catch (IOException e) {
            TiLog.v(TAG, "could not delete file " + file.getName());
            e.printStackTrace();
        }
    }

    @Override
    public void serialize(@NonNull final TiPresenter presenter, final byte[] data) {
        final File file = getStateFile(presenter);
        TiLog.v(TAG, "serialize " + file.getName());
        try {
            FileUtils.writeFile(file, data);
        } catch (IOException e) {
            TiLog.v(TAG, "could not write to file " + file.getName());
            e.printStackTrace();
        }
    }

    @NonNull
    private File getStateFile(final @NonNull TiPresenter presenter) {
        return new File(mCacheDir, presenter.getId());
    }
}
