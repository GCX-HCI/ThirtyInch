package net.grandcentrix.thirtyinch.internal;

import net.grandcentrix.thirtyinch.Removable;
import net.grandcentrix.thirtyinch.TiBindViewInterceptor;
import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.TiView;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Binds a {@link TiView} to a {@link TiPresenter<V>} and allows {@link TiBindViewInterceptor}s to
 * pivot the view before attaching
 *
 * @param <V> the {@link TiView}
 */
public class PresenterViewBinder<V extends TiView> implements InterceptableViewBinder<V> {

    private List<TiBindViewInterceptor> mBindViewInterceptors = new ArrayList<>();

    private HashMap<TiBindViewInterceptor, V> mIntercepterViewOutput = new HashMap<>();

    /**
     * the cached version of the view send to the presenter after it passed the interceptors
     */
    private V mLastView;

    private final TiPresenterLogger mLogger;

    public PresenterViewBinder(final TiPresenterLogger logger) {
        mLogger = logger;
    }

    @NonNull
    @Override
    public Removable addBindViewInterceptor(final TiBindViewInterceptor interceptor) {
        mBindViewInterceptors.add(interceptor);
        invalidateView();

        return new OnTimeRemovable() {
            @Override
            public void onRemove() {
                mBindViewInterceptors.remove(interceptor);
                invalidateView();
            }
        };
    }

    /**
     * binds the view (this Activity) to the {@code presenter}. Allows interceptors to change,
     * delegate or wrap the view before it gets attached to the presenter.
     */
    public void bindView(final TiPresenter<V> presenter, final TiViewProvider<V> viewProvider) {
        if (mLastView == null) {
            invalidateView();
            V interceptedView = viewProvider.provideView();
            for (final TiBindViewInterceptor interceptor : mBindViewInterceptors) {
                interceptedView = interceptor.intercept(interceptedView);
                mIntercepterViewOutput.put(interceptor, interceptedView);
            }
            mLastView = interceptedView;
            mLogger.logTiMessages("binding NEW view to Presenter " + mLastView);
            presenter.bindNewView(mLastView);
        } else {
            mLogger.logTiMessages("binding the cached view to Presenter " + mLastView);
            presenter.bindNewView(mLastView);
        }
    }

    @Nullable
    @Override
    public V getInterceptedViewOf(final TiBindViewInterceptor interceptor) {
        return mIntercepterViewOutput.get(interceptor);
    }

    @NonNull
    @Override
    public List<TiBindViewInterceptor> getInterceptors(
            final Filter<TiBindViewInterceptor> predicate) {
        final ArrayList<TiBindViewInterceptor> result = new ArrayList<>();
        for (int i = 0; i < mBindViewInterceptors.size(); i++) {
            final TiBindViewInterceptor interceptor = mBindViewInterceptors.get(i);
            if (predicate.apply(interceptor)) {
                result.add(interceptor);
            }
        }
        return result;
    }

    @Override
    public void invalidateView() {
        mLastView = null;
        mIntercepterViewOutput.clear();
    }
}
