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

import net.grandcentrix.thirtyinch.TiPresenter;

import java.util.HashMap;

public class PresenterSaviorTestHelper {

    /**
     * helper to clear the savior without exposing this to the public api
     */
    public static void clear() {
        PresenterSavior.INSTANCE.clear();
    }

    public static HashMap<String, TiPresenter> presenters() {
        return PresenterSavior.INSTANCE.mPresenters;
    }

    public static int presenterCount() {
        return presenters().entrySet().size();
    }

}