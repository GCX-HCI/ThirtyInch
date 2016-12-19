package net.grandcentrix.thirtyinch.serialize;

import android.support.annotation.NonNull;

import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.TiView;

/**
 * Interface to serialize and deserialize presenters. The interface should be implemented, if
 * a {@link TiPresenter} should be restored, after a process has died, but the UI component is
 * being recreated.
 */
public interface TiPresenterSerializer {

    /**
     * Serialize the given presenter and write the data to disk.
     *
     * @param presenter The {@link TiPresenter} which should be serialized.
     * @param presenterId The ID of the given presenter.
     */
    void serialize(@NonNull TiPresenter presenter, @NonNull String presenterId);

    /**
     * Deserialize the given presenter. Implementations can either return the same value
     * with adjusted fields or a new instance.
     *
     * @param presenter The {@link TiPresenter} which should be deserialized and recreated.
     * @param presenterId The ID of the given presenter.
     * @return Either the same instance as the argument or a new object.
     */
    @NonNull
    <V extends TiView, P extends TiPresenter<V>> P deserialize(@NonNull P presenter, @NonNull String presenterId);

    /**
     * Optionally clean up temporary data.
     *
     * @param presenterId The ID of the presenter which should be freed.
     */
    void cleanup(@NonNull String presenterId);
}
