package net.grandcentrix.thirtyinch;

/**
 * A Removable returns from a method which adds an object to something. It allows removing the
 * added object without keeping track of the added or the object which got something added. Only
 * holding a reference to this Removable is required.
 * <p>
 * This interface is the equivalent of RxJava {@code Subscription} or {@code IDisposable} in
 * Microsoft's Rx implementation.
 */
public interface Removable {

    /**
     * Indicates whether the added Object is still added
     *
     * @return {@code true} if the added Object is currently added, {@code false} otherwise
     */
    boolean isRemoved();

    /**
     * Removes the Object which got previously added
     */
    void remove();
}
