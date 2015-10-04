package net.grandcentrix.rxmvp;

/**
 * Created by pascalwelsch on 9/8/15.
 */
public class RxPresenterNonConfigurationInstance<V extends RxMvpView> {

    private Object OtherNonConfigurationInstance;

    private RxMvpPresenter<V> mPresenter;

    public RxPresenterNonConfigurationInstance(final RxMvpPresenter<V> presenter,
            final Object otherNonConfigurationInstance) {
        mPresenter = presenter;
        OtherNonConfigurationInstance = otherNonConfigurationInstance;
    }

    public Object getOtherNonConfigurationInstance() {
        return OtherNonConfigurationInstance;
    }

    public RxMvpPresenter<V> getPresenter() {
        return mPresenter;
    }
}
