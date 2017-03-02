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


import net.grandcentrix.thirtyinch.PresenterAction;
import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.TiView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Gives access to the attached presenter
 */
public interface PresenterAccessor<P extends TiPresenter<V>, V extends TiView> {

    /**
     * @return the attached presenter
     */
    P getPresenter();

    /**
     * Executes the {@link PresenterAction} after the presenter got created or available.
     * When the presenter is already available ({@code getPresenter() != null}) the
     * {@link PresenterAction} will be executed immediately.
     * <p>
     * This method is very useful for Activity methods like
     * {@link Activity#onRequestPermissionsResult(int, String[], int[])} or
     * {@link Activity#onActivityResult(int, int, Intent)}. Both methods can restart the Activity
     * entirely and cause NPEs because the callbacks get called before
     * {@link Activity#onCreate(Bundle)} where the presenter gets created or reattached to the
     * Activity.
     */
    void sendToPresenter(final PresenterAction<P> action);

}
