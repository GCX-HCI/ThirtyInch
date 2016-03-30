package net.grandcentrix.divorce;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.HashMap;

/**
 * Activities can be destroyed when the device runs out of memory. Sometimes it doesn't work to
 * save objects via {@link Activity#onRetainNonConfigurationInstance()} for example when the user
 * has enabled "Do not keep activities" in the developer options. This singleton holds strong
 * references to those presenters and returns them when needed.
 *
 * {@link DivorceActivity} is responsible to manage the references
 */
/*package*/ enum PresenterSavior {

    INSTANCE;

    private static final String TAG = PresenterSavior.class.getSimpleName();

    private HashMap<String, Presenter> mPresenters = new HashMap<>();

    /*package*/ void free(final String presenterId) {
        mPresenters.remove(presenterId);
    }

    @Nullable
    /*package*/ Presenter recover(final String id) {
        return mPresenters.get(id);
    }

    /*package*/ String safe(@NonNull final Presenter presenter) {
        final String id = generateId(presenter);
        Log.v(TAG, "safe presenter with id " + id + " " + presenter);
        mPresenters.put(id, presenter);
        return id;
    }

    private String generateId(@NonNull final Presenter presenter) {
        return presenter.getClass().getSimpleName()
                + ":" + presenter.hashCode()
                + ":" + System.nanoTime();
    }
}
