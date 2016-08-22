package net.grandcentrix.thirtyinch.internal;

import net.grandcentrix.thirtyinch.TiActivity;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Internal used interface to allow different implementations of retaining the presenter by the
 * user of the {@link TiActivityDelegate} such as the {@link TiActivity}
 * or {@code TiActivityPlugin}
 */
public interface TiActivityRetainedPresenterProvider<P> {

    /**
     * @return the retained presenter from {@link AppCompatActivity#getLastCustomNonConfigurationInstance()}
     * or equivalent implementations
     */
    @Nullable
    P getRetainedPresenter();
}
