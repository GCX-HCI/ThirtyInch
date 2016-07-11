package net.grandcentrix.thirtyinch.android.internal;

import net.grandcentrix.thirtyinch.TiPresenter;

public class PresenterNonConfigurationInstance<P extends TiPresenter> {

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
