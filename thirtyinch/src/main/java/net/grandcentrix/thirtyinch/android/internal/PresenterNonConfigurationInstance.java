package net.grandcentrix.thirtyinch.android.internal;

import net.grandcentrix.thirtyinch.Presenter;

public class PresenterNonConfigurationInstance<P extends Presenter> {

    private Object OtherNonConfigurationInstance;

    private P mPresenter;

    public PresenterNonConfigurationInstance(final P presenter,
            final Object otherNonConfigurationInstance) {
        mPresenter = presenter;
        OtherNonConfigurationInstance = otherNonConfigurationInstance;
    }

    public Object getOtherNonConfigurationInstance() {
        return OtherNonConfigurationInstance;
    }

    public P getPresenter() {
        return mPresenter;
    }
}
