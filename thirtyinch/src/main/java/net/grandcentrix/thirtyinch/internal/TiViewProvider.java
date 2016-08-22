package net.grandcentrix.thirtyinch.internal;

import net.grandcentrix.thirtyinch.TiView;

import android.support.annotation.NonNull;

/**
 * The {@link net.grandcentrix.thirtyinch.android.TiActivity} itself doesn't not have to implement
 * the {@link TiView} even though it's the default implementation. This interface allows the
 * possible separation.
 */
public interface TiViewProvider<V extends TiView> {

    /**
     * @return the {@link TiView} for the {@link net.grandcentrix.thirtyinch.TiPresenter}
     */
    @NonNull
    V provideView();
}
