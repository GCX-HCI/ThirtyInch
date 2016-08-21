package net.grandcentrix.thirtyinch.android.internal;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

/**
 * Getter for the {@link AppCompatActivity} where the {@link TiActivityDelegate} is attached to.
 * The Activity can't be passed in the {@link TiActivityDelegate} constructor because the {@link
 * TiActivityDelegate} may be created outside of the scope of the Activity.
 * <p>
 * The first usage is when the Activity Lifecycle reaches {@link AppCompatActivity#onStart()}
 */
public interface AppCompatActivityProvider {

    /**
     * @return the Activity associated with the {@link TiActivityDelegate}
     */
    @NonNull
    AppCompatActivity getAppCompatActivity();
}
