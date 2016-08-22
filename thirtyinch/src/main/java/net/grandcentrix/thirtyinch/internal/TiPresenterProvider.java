package net.grandcentrix.thirtyinch.internal;

import net.grandcentrix.thirtyinch.TiPresenter;

import android.os.Bundle;
import android.support.annotation.NonNull;

public interface TiPresenterProvider<P extends TiPresenter> {

    /**
     * Function returning a {@link net.grandcentrix.thirtyinch.TiPresenter}
     * <p>
     * called only once when the Activity got initiated. Doesn't get called when the Activity gets
     * recreated. The old presenter will be recovered in {@link android.app.Activity#onCreate(Bundle)}.
     */
    @NonNull
    P providePresenter();
}
