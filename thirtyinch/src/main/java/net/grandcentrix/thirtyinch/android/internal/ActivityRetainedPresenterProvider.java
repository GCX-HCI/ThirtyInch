package net.grandcentrix.thirtyinch.android.internal;

import android.support.annotation.Nullable;

public interface ActivityRetainedPresenterProvider<P> {

    @Nullable
    P getRetainedPresenter();
}
