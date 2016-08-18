package net.grandcentrix.thirtyinch;

/**
 * Removable returns from {@link TiPresenter#addLifecycleObserver(TiLifecycleObserver)} to allow
 * removing the added {@link TiLifecycleObserver}.
 * <p>
 * This interface is the equivalent of RxJava {@code Subscription}
 */
public interface Removable {

    /**
     * Indicates whether this {@code TiLifecycleObserver} is currently added.
     *
     * @return {@code true} if this {@code TiLifecycleObserver} is currently added, {@code false}
     * otherwise
     */
    boolean isRemoved();

    /**
     * Stops the receipt of new lifecycle events on the {@link TiLifecycleObserver} that was added
     * when this Removable was received.
     * <p>
     * This allows removing a {@link TiLifecycleObserver} before the {@link TiPresenter} reaches
     * its final destroyed state.
     */
    void remove();
}
