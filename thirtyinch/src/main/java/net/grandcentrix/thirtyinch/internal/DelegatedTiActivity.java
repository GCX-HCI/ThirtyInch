package net.grandcentrix.thirtyinch.internal;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * This interface, implemented by Activities allows easy testing of the {@link TiActivityDelegate}
 * without mocking Android classes such as {@link Activity}
 */
public interface DelegatedTiActivity<P> {

    /**
     * @return the retained presenter from {@link AppCompatActivity#getLastCustomNonConfigurationInstance()}
     * or equivalent implementations
     */
    @Nullable
    P getRetainedPresenter();

    /**
     * @return {@link Activity#isFinishing()}
     */
    boolean isActivityFinishing();

    /**
     * @return true when the developer option "Don't keep Activities" is enabled
     */
    boolean isDontKeepActivitiesEnabled();

    /**
     * Post the runnable on the UI queue
     */
    boolean postToMessageQueue(Runnable runnable);
}
