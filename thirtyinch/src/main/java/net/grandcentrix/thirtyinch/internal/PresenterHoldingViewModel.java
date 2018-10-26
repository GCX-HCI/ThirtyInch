package net.grandcentrix.thirtyinch.internal;

import android.arch.lifecycle.ViewModel;
import net.grandcentrix.thirtyinch.TiPresenter;

public class PresenterHoldingViewModel extends ViewModel {

    private TiPresenter mPresenter;

    public TiPresenter getPresenter() {
        return mPresenter;
    }

    public void setPresenter(final TiPresenter presenter) {
        mPresenter = presenter;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (mPresenter == null) {
            return;
        }

        // when the presenter is not destroyed yet, destroy it.
        if (!mPresenter.isDestroyed()) {
            if (mPresenter.isViewAttached()) {
                mPresenter.detachView();
            }
            if (!mPresenter.isDestroyed()) {
                mPresenter.destroy();
            }
        }
    }
}
