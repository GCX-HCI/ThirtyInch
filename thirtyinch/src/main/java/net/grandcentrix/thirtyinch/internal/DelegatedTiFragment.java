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

import androidx.fragment.app.Fragment;
import java.util.concurrent.Executor;

public interface DelegatedTiFragment {

    /**
     * This Object is used identify the correct scope where the presenter should be saved in the
     * {@link PresenterSavior}. This object is only used for identity comparison.
     *
     * @return the object hosting this {@link Fragment}, most likely {@link Fragment#getHost()}
     */
    Object getHostingContainer();

    /**
     * @return {@link UiThreadExecutor}
     */
    Executor getUiThreadExecutor();

    /**
     * @return {@link Fragment#isAdded()}
     */
    boolean isFragmentAdded();

    /**
     * @return {@link Fragment#isDetached()}
     */
    boolean isFragmentDetached();

    /**
     * @return {@link Fragment#isInBackStack()}
     */
    boolean isFragmentInBackstack();

    /**
     * @return {@link Fragment#isRemoving()}
     */
    boolean isFragmentRemoving();

}
