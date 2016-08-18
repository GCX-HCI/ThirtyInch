package net.grandcentrix.thirtyinch.test;

import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.TiView;

public class TiPresenterInstructor<V extends TiView> {

    private TiPresenter<V> mPresenter;

    public TiPresenterInstructor(final TiPresenter<V> presenter) {
        mPresenter = presenter;
    }

    /**
     * attaches the new view and takes care for removing the old view when one is attached
     * @param view
     */
    public void attachView(final V view) {
        detachView();

        mPresenter.bindNewView(view);
        mPresenter.wakeUp();
    }

    public void create() {
        mPresenter.create();
    }

    public void destroy() {
        detachView();
        mPresenter.destroy();
    }

    /**
     * moves the presenter into state {@link net.grandcentrix.thirtyinch.TiPresenter.State#CREATED_WITH_DETACHED_VIEW}
     * from every state
     */
    public void detachView() {
        final TiPresenter.State state = mPresenter.getState();
        switch (state) {
            case INITIALIZED:
                mPresenter.create();
                break;
            case CREATED_WITH_DETACHED_VIEW:
                // already there
                break;
            case VIEW_ATTACHED_AND_AWAKE:
                mPresenter.sleep();
                break;
            case DESTROYED:
                throw new IllegalStateException(
                        "Presenter is already destroyed, further lifecycle changes aren't allowed");
        }
    }
}
