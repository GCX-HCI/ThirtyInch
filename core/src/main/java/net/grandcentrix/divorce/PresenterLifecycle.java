package net.grandcentrix.divorce;

public interface PresenterLifecycle<V extends View> {

    void bindNewView(V view);

    void create();

    void destroy();

    void sleep();

    void wakeUp();
}
