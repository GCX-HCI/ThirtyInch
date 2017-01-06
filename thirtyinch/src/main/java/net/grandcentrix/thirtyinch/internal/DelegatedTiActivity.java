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
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * This interface, implemented by Activities allows easy testing of the {@link TiActivityDelegate}
 * without mocking Android classes such as {@link Activity}
 */
public interface DelegatedTiActivity<P> {

    /**
     * @return the retained presenter from {@link AppCompatActivity#getLastCustomNonConfigurationInstance()}
     * or equivalent implementations
     */
    @Nullable
    P getRetainedPresenter();

    /**
     * @return {@link Activity#isChangingConfigurations()}
     */
    boolean isActivityChangingConfigurations();

    /**
     * @return {@link Activity#isFinishing()}
     */
    boolean isActivityFinishing();

    /**
     * @return true when the developer option "Don't keep Activities" is enabled
     */
    boolean isDontKeepActivitiesEnabled();

    /**
     * Post the runnable on the UI queue
     */
    boolean postToMessageQueue(Runnable runnable);
}
