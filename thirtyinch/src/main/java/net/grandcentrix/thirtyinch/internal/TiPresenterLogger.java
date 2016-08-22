package net.grandcentrix.thirtyinch.internal;

import net.grandcentrix.thirtyinch.TiActivity;

/**
 * Super simple logging interface because the {@link TiActivityDelegate}
 * is not responsible for actually logging. The using {@link TiActivity}
 * or {@code TiActivityPlugin} takes care of logging and providing the correct logging TAG.
 */
public interface TiPresenterLogger {

    /**
     * logs a debug message from the presenter
     *
     * @param msg message from the presenter to display
     */
    void logTiMessages(final String msg);
}
