package net.grandcentrix.thirtyinch.android.internal;

import net.grandcentrix.thirtyinch.Presenter;
import net.grandcentrix.thirtyinch.View;

public class PresenterNonConfigurationInstance<V extends View> {

    private Object OtherNonConfigurationInstance;

    private Presenter<V> mPresenter;

    public PresenterNonConfigurationInstance(final Presenter<V> presenter,
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
