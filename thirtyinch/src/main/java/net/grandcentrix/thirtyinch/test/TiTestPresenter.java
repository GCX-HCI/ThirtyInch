package net.grandcentrix.thirtyinch.test;

import android.support.annotation.NonNull;
import java.util.concurrent.Executor;
import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.TiView;
import net.grandcentrix.thirtyinch.ViewAction;

/**
 * Designed for unit testing of {@link TiPresenter#sendToView(ViewAction)}.
 * <p>
 * The problem is that {@link TiPresenter#sendToView(ViewAction)} needs a ui executor thread.
 * Unfortunately a ui executor thread isn't available in unit test.
 * </p>
 * <p>
 * This {@link TiTestPresenter} holds the {@link TiPresenter} under test and replace the "ui executor" with a stubbed
 * implementation.
 * </p>
 */
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