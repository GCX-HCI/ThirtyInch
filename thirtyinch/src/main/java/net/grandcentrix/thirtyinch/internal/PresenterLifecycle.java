package net.grandcentrix.thirtyinch.internal;

import net.grandcentrix.thirtyinch.View;

public interface PresenterLifecycle<V extends View> {

    void bindNewView(V view);

    void create();

    void destroy();

    void sleep();

    void wakeUp();
}
