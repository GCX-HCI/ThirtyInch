package net.grandcentrix.thirtyinch.internal;

import net.grandcentrix.thirtyinch.Presenter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Activities can be destroyed when the device runs out of memory. Sometimes it doesn't work to
 * save objects via {@code Activity#onRetainNonConfigurationInstance()} for example when the user
 * has enabled "Do not keep activities" in the developer options. This singleton holds strong
 * references to those presenters and returns them when needed.
 *
 * {@code ThrityInchActivity} is responsible to manage the references
 */
public enum PresenterSavior {

    INSTANCE;

    private Logger mLogger = Logger.getLogger(PresenterSavior.class.getSimpleName());

    private HashMap<String, Presenter> mPresenters = new HashMap<>();

    public void free(final String presenterId) {
        mPresenters.remove(presenterId);
    }

    @Nullable
    public Presenter recover(final String id) {
        return mPresenters.get(id);
    }

    public String safe(@NotNull final Presenter presenter) {
        final String id = generateId(presenter);
        mLogger.log(Level.FINER, "safe presenter with id " + id + " " + presenter);
        mPresenters.put(id, presenter);
        return id;
    }

    private String generateId(@NotNull final Presenter presenter) {
        return presenter.getClass().getSimpleName()
                + ":" + presenter.hashCode()
                + ":" + System.nanoTime();
    }
}
