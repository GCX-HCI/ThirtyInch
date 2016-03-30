package net.grandcentrix.divorce.internal;

public enum UiPlatform {
    /**
     * default, no special behavior
     */
    PLAIN_JAVA,
    /**
     * calls void methods on the android main thread
     */
    ANDROID
}
