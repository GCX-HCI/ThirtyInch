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

package net.grandcentrix.thirtyinch;


import net.grandcentrix.thirtyinch.internal.ActivityPresenterBinder;
import net.grandcentrix.thirtyinch.internal.FragmentPresenterBinder;
import net.grandcentrix.thirtyinch.internal.PresenterAccessor;
import net.grandcentrix.thirtyinch.internal.TiPresenterProvider;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

public class TiPresenterBinders {

    /**
     * must be called in {@link Activity#onCreate(Bundle)}
     */
    public static <P extends TiPresenter<V>, V extends TiView> TiPresenterBinder<P, V> attachPresenter(
            final Activity activity, final Bundle savedInstanceState,
            final TiPresenterProvider<P> provider) {

        final ActivityPresenterBinder<P, V> binder =
                new ActivityPresenterBinder<>(activity, savedInstanceState, provider);

        Application app = activity.getApplication();
        app.registerActivityLifecycleCallbacks(binder);

        return binder;
    }

    /**
     * must be called in {@link Fragment#onCreate(Bundle)}
     */
    public static <P extends TiPresenter<V>, V extends TiView> PresenterAccessor<P, V> attachPresenter(
            final Fragment fragment, final Bundle savedInstanceState,
            final TiPresenterProvider<P> provider) {

        if (fragment instanceof TiFragment) {
            throw new IllegalStateException(
                    "Can't attach a TiPresenter to a Fragment which already has a TiPresenter");
        }

        final FragmentPresenterBinder<P, V> binder =
                new FragmentPresenterBinder<>(fragment, savedInstanceState, provider);

        FragmentManager fragmentManager = fragment.getActivity().getSupportFragmentManager();
        fragmentManager.registerFragmentLifecycleCallbacks(binder, false);

        return binder;
    }
}
