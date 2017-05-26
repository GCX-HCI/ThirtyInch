package net.grandcentrix.thirtyinch.rx;

import net.grandcentrix.thirtyinch.TiPresenter;

import rx.Single;

import static net.grandcentrix.thirtyinch.rx.RxTiUtils.isViewReady;

public class RxTiSingleUtils {

    /**
     * Returns a transformer that will delay onSuccess and onError emissions unless a view
     * become available. getView() is guaranteed to be != null during all emissions. This
     * transformer can only be used on application's main thread. See the correct order:
     * <pre>
     * <code>
     *
     * .observeOn(AndroidSchedulers.mainThread())
     * .compose(this.&lt;T&gt;deliverToView())
     * </code>
     * </pre>
     *
     * @param <T>       a type of onNext value.
     * @param presenter the presenter waiting for the view
     * @return the delaying operator.
     */
    public static <T> Single.Transformer<T, T> deliverToView(final TiPresenter presenter) {
        return new Single.Transformer<T, T>() {
            @Override
            public Single<T> call(final Single<T> single) {
                return single.lift(OperatorSemaphore.<T>semaphore(isViewReady(presenter)));
            }
        };
    }

}
