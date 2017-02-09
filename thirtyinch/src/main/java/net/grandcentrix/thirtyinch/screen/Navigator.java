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

package net.grandcentrix.thirtyinch.screen;

import net.grandcentrix.thirtyinch.TiActivity;
import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.TiView;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import java.util.Stack;

public class Navigator {

    private Stack<TiScreen> mBackStack = new Stack<>();

    private final ViewGroup mRootView;

    private final TiActivity mTiActivity;

    public Navigator(@NonNull final TiActivity tiActivity, @NonNull final ViewGroup rootView) {
        mTiActivity = tiActivity;
        mRootView = rootView;
    }

    public synchronized void clear() {
        while (mBackStack.size() > 0) {
            removeCurrentScreen();
        }
    }

    public synchronized <P extends TiPresenter<V>, V extends TiView> void goTo(
            TiScreen<P, V> screen) {

        final TiScreen lastScreen = removeCurrentScreen();

        screen.onCreate();
        final View androidView = screen.onCreateView(mTiActivity, mRootView);
        mRootView.removeAllViews();
        mRootView.addView(androidView);

        screen.onStart();
    }

    public synchronized TiScreen removeCurrentScreen() {
        final TiScreen lastScreen;
        if (mBackStack.size() > 0) {
            lastScreen = mBackStack.pop();
        } else {
            lastScreen = null;
        }

        if (lastScreen != null) {
            lastScreen.onStop();
        }

        mRootView.removeAllViews();

        return lastScreen;
    }
}
