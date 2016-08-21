package net.grandcentrix.thirtyinch.internal;

/**
 * Super simple logging interface because the {@link net.grandcentrix.thirtyinch.android.internal.TiActivityDelegate}
 * is not responsible for actually logging. The using {@link net.grandcentrix.thirtyinch.android.TiActivity}
 * or {@code TiActivityPlugin} takes care of logging and providing the correct logging TAG.
 */
public interface TiPresenterLogger {

    /**
     * logs a debug message from the presenter
     *
     * @param msg message from the presenter to display
     */
    void log(final String msg);
}
