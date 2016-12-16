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

import android.app.Activity;
import android.support.v4.app.Fragment;

public interface DelegatedTiFragment {

    /**
     * @return true when the developer option "Don't keep Activities" is enabled
     */
    boolean isDontKeepActivitiesEnabled();

    /**
     * @return {@link Fragment#isAdded()}
     */
    boolean isFragmentAdded();

    /**
     * @return {@link Fragment#isDetached()}
     */
    boolean isFragmentDetached();

    /**
     * @return {@link Activity#isChangingConfigurations()}
     */
    boolean isHostingActivityChangingConfigurations();

    /**
     * @return {@link Activity#isFinishing()}
     */
    boolean isHostingActivityFinishing();

    /**
     * Post the runnable on the UI queue
     */
    boolean postToMessageQueue(Runnable runnable);

    /**
     * Call {@link Fragment#setRetainInstance(boolean)}
     */
    void setFragmentRetainInstance(final boolean retain);
}
