package net.grandcentrix.thirtyinch;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Removable which allows removing only once
 */
public abstract class OnTimeRemovable implements Removable {

    private final AtomicBoolean removed = new AtomicBoolean(false);

    @Override
    public boolean isRemoved() {
        return removed.get();
    }

    /**
     * Called when the added Object should be removed. Only called once
     */
    public abstract void onRemove();

    @Override
    public void remove() {
        // allow calling remove only once
        if (removed.compareAndSet(false, true)) {
            onRemove();
        }
    }
}
