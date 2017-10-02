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

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import java.util.concurrent.Executor;

/**
 * Executes work on the UI thread. If the current thread is the UI thread, then the action is
 * executed immediately. If the current thread is not the UI thread, the action is posted to the
 * event queue of the UI thread.
 */
public class UiThreadExecutor implements Executor {

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private Thread mUiThread = Looper.getMainLooper().getThread();

    @Override
    public void execute(@NonNull Runnable command) {
        if (Thread.currentThread() == mUiThread) {
            // already on main thread, simply execute
            command.run();
        } else {
            mHandler.post(command);
        }
    }
}
