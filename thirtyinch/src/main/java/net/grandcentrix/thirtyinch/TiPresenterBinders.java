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
import net.grandcentrix.thirtyinch.internal.TiViewProvider;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

public class TiPresenterBinders {

    /**
     * must be called in {@link Activity#onCreate(Bundle)}
     */
    public static <P extends TiPresenter<V>, V extends TiView> TiPresenterBinder<P, V> attachPresenter(
            @NonNull final Activity activity,
            @Nullable final Bundle savedInstanceState,
            @NonNull final TiPresenterProvider<P> presenterProvider) {

        return attachPresenter(activity, savedInstanceState, presenterProvider, null);
    }

    /**
     * must be called in {@link Activity#onCreate(Bundle)}
     */
    public static <P extends TiPresenter<V>, V extends TiView> TiPresenterBinder<P, V> attachPresenter(
            @NonNull final Activity activity,
            @Nullable final Bundle savedInstanceState,
            @NonNull final TiPresenterProvider<P> presenterProvider,
            @Nullable final TiViewProvider<V> viewProvider) {

        if (activity instanceof TiActivity) {
            throw new IllegalStateException(
                    "Can't attach a TiPresenter to a TiActivity which already has a TiPresenter");
        }

        final ActivityPresenterBinder<P, V> binder = new ActivityPresenterBinder<>(activity,
                savedInstanceState, presenterProvider, viewProvider);

        Application app = activity.getApplication();
        app.registerActivityLifecycleCallbacks(binder);

        return binder;
    }

    /**
     * must be called in {@link Fragment#onCreate(Bundle)}
     */
    public static <P extends TiPresenter<V>, V extends TiView> PresenterAccessor<P, V> attachPresenter(
            @NonNull final Fragment fragment,
            @Nullable final Bundle savedInstanceState,
            @NonNull final TiPresenterProvider<P> presenterProvider,
            @Nullable final TiViewProvider<V> viewProvider) {

        if (fragment instanceof TiFragment) {
            throw new IllegalStateException(
                    "Can't attach a TiPresenter to a TiFragment which already has a TiPresenter");
        }

        final FragmentPresenterBinder<P, V> binder = new FragmentPresenterBinder<>(fragment,
                savedInstanceState, presenterProvider, viewProvider);

        FragmentManager fragmentManager = fragment.getActivity().getSupportFragmentManager();
        fragmentManager.registerFragmentLifecycleCallbacks(binder, false);

        return binder;
    }

    /**
     * must be called in {@link Fragment#onCreate(Bundle)}
     */
    public static <P extends TiPresenter<V>, V extends TiView> PresenterAccessor<P, V> attachPresenter(
            @NonNull final Fragment fragment,
            @Nullable final Bundle savedInstanceState,
            @NonNull final TiPresenterProvider<P> presenterProvider) {

        return attachPresenter(fragment, savedInstanceState, presenterProvider, null);
    }
}
