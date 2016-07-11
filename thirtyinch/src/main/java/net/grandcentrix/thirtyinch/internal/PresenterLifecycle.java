package net.grandcentrix.thirtyinch.internal;

import net.grandcentrix.thirtyinch.TiView;

import android.support.annotation.NonNull;

public interface PresenterLifecycle<V extends TiView> {

    /**
     * bind a new view to this presenter.
     *
     * @param view the new view, can't be null. To set the view to {@code null} call {@link
     *             #sleep()}
     */
    void bindNewView(@NonNull V view);

    void create();

    void destroy();

    void sleep();

    void wakeUp();
}
