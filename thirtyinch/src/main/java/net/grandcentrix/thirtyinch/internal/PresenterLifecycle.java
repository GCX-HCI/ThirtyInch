package net.grandcentrix.thirtyinch.internal;

import net.grandcentrix.thirtyinch.TiView;

public interface PresenterLifecycle<V extends TiView> {

    void bindNewView(V view);

    void create();

    void destroy();

    void sleep();

    void wakeUp();
}
