package net.grandcentrix.divorce;

public class RxPresenterNonConfigurationInstance<V extends View> {

    private Object OtherNonConfigurationInstance;

    private Presenter<V> mPresenter;

    public RxPresenterNonConfigurationInstance(final Presenter<V> presenter,
            final Object otherNonConfigurationInstance) {
        mPresenter = presenter;
        OtherNonConfigurationInstance = otherNonConfigurationInstance;
    }

    public Object getOtherNonConfigurationInstance() {
        return OtherNonConfigurationInstance;
    }

    public Presenter<V> getPresenter() {
        return mPresenter;
    }
}
