package net.grandcentrix.thirtyinch.android.internal;

import android.support.annotation.NonNull;

public interface PresenterProvider<P> {

    @NonNull
    P providePresenter();
}
