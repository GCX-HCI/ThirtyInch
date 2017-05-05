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

import java.util.concurrent.Executor;

/**
 * This interface, implemented by Activities allows easy testing of the {@link TiActivityDelegate}
 * without mocking Android classes such as {@link Activity}
 */
public interface DelegatedTiActivity<P> {

    /**
     * This Object is used identify the correct scope where the presenter should be saved in the
     * {@link PresenterSavior}. This object is only used for identity comparison.
     *
     * @return the {@link Activity} instance itself, which is it's own host
     */
    Object getHostingContainer();

    /**
     * @return {@link UiThreadExecutor}
     */
    Executor getUiThreadExecutor();

    /**
     * @return {@link Activity#isChangingConfigurations()}
     */
    boolean isActivityChangingConfigurations();

    /**
     * @return {@link Activity#isFinishing()}
     */
    boolean isActivityFinishing();
}
