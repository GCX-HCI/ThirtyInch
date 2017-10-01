package net.grandcentrix.thirtyinch.test;

import android.support.annotation.NonNull;

import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.TiView;

import java.util.concurrent.Executor;

public class TiTestPresenter<V extends TiView> {

    private TiPresenter<V> mPresenter;

    public TiTestPresenter(final TiPresenter<V> presenter) {
        mPresenter = presenter;
    }

    /**
     * attaches the new view and takes care for removing the old view when one is attached
     */
    public void attachView(final V view) {
        detachView();

        mPresenter.setUiThreadExecutor(new Executor() {
            @Override
            public void execute(@NonNull final Runnable action) {
                action.run();
            }
        });
        mPresenter.attachView(view);
    }

    public void create() {
        mPresenter.create();
    }

    public void destroy() {
        detachView();
        mPresenter.destroy();
    }

    /**
     * moves the presenter into state {@link TiPresenter.State#VIEW_DETACHED}
     * from every state
     */
    public void detachView() {
        final TiPresenter.State state = mPresenter.getState();
        switch (state) {
            case INITIALIZED:
                mPresenter.create();
                break;
            case VIEW_DETACHED:
                // already there
                break;
            case VIEW_ATTACHED:
                mPresenter.detachView();
                mPresenter.setUiThreadExecutor(null);
                break;
            case DESTROYED:
                throw new IllegalStateException(
                        "Presenter is already destroyed, further lifecycle changes aren't allowed");
        }
    }
}