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
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;
import net.grandcentrix.thirtyinch.Removable;
import net.grandcentrix.thirtyinch.TiLifecycleObserver;
import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.TiView;

public class RxTiPresenterUtils {

    /**
     * Wrapper for an emitted value, along with a record of whether or not the view was attached when the value was
     * emitted.
     */
    private static class ViewReadyValue<T> {

        T value;

        boolean viewReady;

        ViewReadyValue(final T t, final Boolean viewReady) {
            this.value = t;
            this.viewReady = viewReady;
        }
    }

    /**
     * Returns a transformer that will delay onNext, onError and onComplete emissions until a view
     * become available. getView() is guaranteed to be != null during all emissions, provided that this
     * transformer is only used on the application's main thread.
     * <p/>
     * If this transformer receives a next value while the previous value has not been delivered,
     * the previous value will be dropped.
     * <p/>
     * Use this operator when you need to show updatable data.
     *
     * @param <T>       a type of onNext value.
     * @param presenter the presenter waiting for the view
     * @return the delaying operator.
     */
    public static <T> ObservableTransformer<T, T> deliverLatestToView(
            final TiPresenter presenter) {
        return new ObservableTransformer<T, T>() {
            @Override
            public ObservableSource<T> apply(final Observable<T> observable) {

                // make sure we never complete
                final Observable<T> source = observable.concatWith(Observable.<T>never());

                // The order of the sources is important here! We want the viewReady emission to be captured first so that any synchronous
                // source emissions are not skipped.
                // See https://github.com/ReactiveX/RxJava/issues/5325
                return Observable
                        .combineLatest(isViewReady(presenter), source,
                                new BiFunction<Boolean, T, ViewReadyValue<T>>() {
                                    @Override
                                    public ViewReadyValue<T> apply(final Boolean viewReady, final T t)
                                            throws Exception {
                                        return new ViewReadyValue<>(t, viewReady);
                                    }
                                })
                        .flatMap(new Function<ViewReadyValue<T>, ObservableSource<T>>() {
                            @Override
                            public ObservableSource<T> apply(final ViewReadyValue<T> viewReadyValue)
                                    throws Exception {
                                if (viewReadyValue.viewReady) {
                                    return Observable.just(viewReadyValue.value);
                                } else {
                                    return Observable.empty();
                                }
                            }
                        });
            }
        };
    }

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
