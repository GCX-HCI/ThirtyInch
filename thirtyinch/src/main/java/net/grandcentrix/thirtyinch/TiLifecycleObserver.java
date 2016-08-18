package net.grandcentrix.thirtyinch;

/**
 * Can be added to a {@link TiPresenter} with {@link TiPresenter#addLifecycleObserver(TiLifecycleObserver)}
 * to get notifications when the lifecycle changes
 */
public interface TiLifecycleObserver {

    /**
     * gets called when the {@link net.grandcentrix.thirtyinch.TiPresenter.State} changes
     *
     * @param state                the new state of the {@link TiPresenter}
     * @param beforeLifecycleEvent {@code true} when called before the {@code on...} lifecycle
     *                             methods, {@code false} when called after
     */
    void onChange(final TiPresenter.State state, final boolean beforeLifecycleEvent);
}
