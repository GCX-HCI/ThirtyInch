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

package net.grandcentrix.thirtyinch.rx2;

import net.grandcentrix.thirtyinch.TiLifecycleObserver;
import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.TiView;

import android.support.annotation.NonNull;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class RxTiPresenterDisposableHandler {

    private CompositeDisposable mPresenterDisposables = new CompositeDisposable();

    private CompositeDisposable mUiDisposables;

    public RxTiPresenterDisposableHandler(final TiPresenter presenter) {
        presenter.addLifecycleObserver(new TiLifecycleObserver() {
            @Override
            public void onChange(final TiPresenter.State state,
                    final boolean hasLifecycleMethodBeenCalled) {
                if (state == TiPresenter.State.VIEW_DETACHED && hasLifecycleMethodBeenCalled) {
                    // dispose all UI disposable created in onAttachView(TiView) and added
                    // via manageViewDisposable(Disposable...)
                    if (mUiDisposables != null) {
                        mUiDisposables.dispose();
                        mUiDisposables = null;
                    }
                }

                if (state == TiPresenter.State.VIEW_ATTACHED && hasLifecycleMethodBeenCalled) {
                    mUiDisposables = new CompositeDisposable();
                }

                if (state == TiPresenter.State.DESTROYED && hasLifecycleMethodBeenCalled) {
                    mPresenterDisposables.dispose();
                    mPresenterDisposables = null;
                }
            }
        });

    }

    /**
     * Add your disposables here and they will automatically disposed when
     * {@link TiPresenter#destroy()} gets called
     *
     * @throws IllegalStateException when the presenter has reached {@link net.grandcentrix.thirtyinch.TiPresenter.State#DESTROYED}
     */
    public void manageDisposable(@NonNull final Disposable... disposables) {
        if (mPresenterDisposables == null) {
            throw new IllegalStateException("disposable handling doesn't work"
                    + " when the presenter has reached the DESTROYED state");
        }

        addDisposables(mPresenterDisposables, disposables);
    }

    /**
     * Add your disposables for View events to this method to get them automatically cleaned up
     * in {@link TiPresenter#detachView()}. typically call this in {@link
     * TiPresenter#attachView(TiView)} where you dispose to the UI events.
     *
     * @throws IllegalStateException when no view is attached
     */
    public void manageViewDisposable(@NonNull final Disposable... disposables) {
        if (mUiDisposables == null) {
            throw new IllegalStateException("view disposable can't be handled"
                    + " when there is no view");
        }

        addDisposables(mUiDisposables, disposables);
    }

    /**
     * Adds all disposables to the given compositeDisposable if not already disposed
     */
    private static void addDisposables(final CompositeDisposable compositeDisposable,
            final Disposable... disposables) {
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < disposables.length; i++) {
            final Disposable disposable = disposables[i];
            if (disposable.isDisposed()) {
                continue;
            }

            compositeDisposable.add(disposable);
        }
    }

}
