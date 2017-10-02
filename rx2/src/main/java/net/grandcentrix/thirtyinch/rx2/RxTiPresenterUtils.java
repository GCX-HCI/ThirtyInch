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

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposable;
import net.grandcentrix.thirtyinch.Removable;
import net.grandcentrix.thirtyinch.TiLifecycleObserver;
import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.TiView;

public class RxTiPresenterUtils {

    /**
     * Observable of the view state. The View is ready to receive calls after calling {@link
     * TiPresenter#attachView(TiView)} and before calling {@link TiPresenter#detachView()}.
     */
    public static Observable<Boolean> isViewReady(final TiPresenter presenter) {
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(final ObservableEmitter<Boolean> emitter)
                    throws Exception {
                if (!emitter.isDisposed()) {
                    emitter.onNext(presenter.getState() == TiPresenter.State.VIEW_ATTACHED);
                }

                final Removable removable = presenter
                        .addLifecycleObserver(new TiLifecycleObserver() {
                            @Override
                            public void onChange(final TiPresenter.State state,
                                    final boolean hasLifecycleMethodBeenCalled) {
                                if (!emitter.isDisposed()) {
                                    emitter.onNext(state == TiPresenter.State.VIEW_ATTACHED
                                            && hasLifecycleMethodBeenCalled);
                                }
                            }
                        });

                emitter.setDisposable(new Disposable() {
                    @Override
                    public void dispose() {
                        removable.remove();
                    }

                    @Override
                    public boolean isDisposed() {
                        return removable.isRemoved();
                    }
                });
            }
        }).distinctUntilChanged();
    }

}
