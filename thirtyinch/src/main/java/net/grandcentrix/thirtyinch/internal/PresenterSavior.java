/*
 * Copyright (C) 2017 grandcentrix GmbH
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.grandcentrix.thirtyinch.internal;

import android.app.Activity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import net.grandcentrix.thirtyinch.TiLog;
import net.grandcentrix.thirtyinch.TiPresenter;

/**
 * When a {@link TiPresenter} is created with
 * {@link net.grandcentrix.thirtyinch.TiConfiguration.Builder#setRetainPresenterEnabled(boolean)}
 * set to {@code true} this singleton is responsible to save the presenter across orientation
 * changes. This works far better than {@link Activity#onRetainNonConfigurationInstance()} which
 * doesn't work when "Don't keep Activities" is on.
 * <p>
 * {@link net.grandcentrix.thirtyinch.TiActivity} is able to detect when its presenter should be
 * destroyed and tells this savior when to free a presenter instance.
 * </p>
 * <p>
 * {@link net.grandcentrix.thirtyinch.TiFragment} can't detect on its own when its hosting Activity
 * is about to finish to free its presenter. For example when the Presenter is in the backstack.
 * This savior uses {@link android.app.Application.ActivityLifecycleCallbacks} to detect those
 * cases
 * and destroys and cleans the presenters to prevents leaks.
 * </p>
 */
public class PresenterSavior implements TiPresenterSavior,
        ActivityInstanceObserver.ActivityFinishListener {

    /**
     * Thrown for not supported host types
     */
    public static class IllegalHostException extends RuntimeException {

        public IllegalHostException(final Object host) {
            super("Host has unknown type " + host.getClass().getSimpleName()
                    + " and is not supported.");
        }
    }

    private static PresenterSavior INSTANCE;

    private static final String TAG = PresenterSavior.class.getSimpleName();

    /**
     * enable debug logging for testing
     */
    private static final boolean DEBUG = false;

    @VisibleForTesting
    ActivityInstanceObserver mActivityInstanceObserver;

    /**
     * Holds a scope for every Activity with one or more presenters. There is no direct mapping for
     * {@link Activity} to {@link PresenterScope} because Activity instances can be
     * destroyed. The {@link ActivityInstanceObserver} takes care to manage unique Ids for each
     * Activity which are used as keys here.
     */
    @VisibleForTesting
    final HashMap<String, PresenterScope> mScopes = new HashMap<>();

    /**
     * Access to the {@link PresenterSavior} singleton to save presenters across orientation changes
     */
    public static synchronized PresenterSavior getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PresenterSavior();
        }
        return INSTANCE;
    }

    @VisibleForTesting
    PresenterSavior() {

    }

    @Override
    public void free(final String presenterId, @NonNull final Object host) {
        final PresenterScope scope = getScope(host);
        if (scope != null) {
            scope.remove(presenterId);

            // cleanup empty PresenterScope
            if (scope.isEmpty()) {
                mScopes.values().remove(scope);
            }
        }

        // unregister host observer
        if (host instanceof Activity) {
            final Activity activity = (Activity) host;
            unregisterActivityObserver(activity);
        } else {
            // currently only Activity is supported as host
            throw new IllegalStateException(
                    "Host has unknown type " + host.getClass().getSimpleName()
                            + " and is not supported.");
        }

        printRemainingPresenter();
    }

    @Override
    public void onActivityFinished(final Activity activity, final String hostId) {
        // First remove the scope, and don't leak it when the Activity got finished
        final PresenterScope scope = mScopes.remove(hostId);
        unregisterActivityObserver(activity);

        TiLog.d(TAG, "Activity is finishing, free remaining presenters " + activity);
        if (scope != null) {
            for (final Map.Entry<String, TiPresenter> entry : scope.getAllMappings()) {
                final String presenterId = entry.getKey();
                final TiPresenter presenter = entry.getValue();

                // when the presenter is not destroyed yet, destroy it.
                if (!presenter.isDestroyed()) {
                    if (presenter.isViewAttached()) {
                        presenter.detachView();
                    }
                    if (!presenter.isDestroyed()) {
                        presenter.destroy();
                    }
                }
                scope.remove(presenterId);
            }
        }

        printRemainingPresenter();
    }

    @Override
    @Nullable
    public TiPresenter recover(final String presenterId, @NonNull final Object host) {
        final PresenterScope scope = getScope(host);
        if (scope == null) {
            return null;
        }
        return scope.get(presenterId);
    }

    @Override
    public String save(@NonNull final TiPresenter presenter, @NonNull final Object host) {

        // hostId will be non null for new hosts
        String hostId = null;

        PresenterScope scope = getScope(host);
        if (scope == null) {
            // create a new scope
            scope = new PresenterScope();
            hostId = generateId(host);
            mScopes.put(hostId, scope);
        }
        final String presenterId = generateId(presenter);
        scope.save(presenterId, presenter);

        if (hostId != null) {
            // register host observer when a new host was detected
            if (host instanceof Activity) {
                final Activity activity = (Activity) host;
                observeActivityFinish(activity, hostId);
            } else {
                // currently only Activity is supported as host
                throw new IllegalHostException(host);
            }
        }

        printRemainingPresenter();

        return presenterId;
    }

    /**
     * Generates a unique id for a given object
     */
    private String generateId(final Object object) {
        return object.getClass().getSimpleName()
                + ":" + object.hashCode()
                + ":" + System.nanoTime();
    }

    /**
     * retrieves an existing scope for a {@link Activity} but doesn't create on when the scope
     * doesn't exist
     */
    @Nullable
    private synchronized PresenterScope getScope(final Object host) {
        if (host instanceof Activity) {
            final ActivityInstanceObserver detector = mActivityInstanceObserver;
            if (detector == null) {
                return null;
            }

            final Activity activity = (Activity) host;
            final String scopeId = detector.getActivityId(activity);
            if (scopeId == null) {
                return null;
            }

            return mScopes.get(scopeId);
        } else {
            // currently only Activity is supported as host
            throw new IllegalHostException(host);
        }
    }

    /**
     * registers the {@link ActivityInstanceObserver.ActivityFinishListener} for the activity
     *
     * @param activity to listen for the finish event
     * @param hostId   id to track the Activity across orientation changes
     */
    private void observeActivityFinish(final Activity activity, @NonNull final String hostId) {
        final ActivityInstanceObserver observer = registerActivityObserver(activity);
        observer.startTracking(activity, hostId);
    }

    /**
     * print all presenters saved in the savior for debug
     *
     * @see #DEBUG
     */
    private void printRemainingPresenter() {
        if (DEBUG) {
            final ArrayList<TiPresenter> presenters = new ArrayList<>();
            for (final Map.Entry<String, PresenterScope> entry : mScopes.entrySet()) {
                presenters.addAll(entry.getValue().getAll());
            }

            TiLog.d(TAG, "presenter count: " + presenters.size());
            for (final TiPresenter presenter : presenters) {
                TiLog.v(TAG, " - " + presenter);
            }
        }
    }

    /**
     * registers the {@link #mActivityInstanceObserver}
     */
    private ActivityInstanceObserver registerActivityObserver(final Activity activity) {
        if (mActivityInstanceObserver == null) {
            mActivityInstanceObserver = new ActivityInstanceObserver(this);
            TiLog.v(TAG, "registering lifecycle callback");
            activity.getApplication().registerActivityLifecycleCallbacks(mActivityInstanceObserver);
        }
        return mActivityInstanceObserver;
    }

    /**
     * unregister {@link #mActivityInstanceObserver} when scopes are empty
     */
    private void unregisterActivityObserver(final Activity activity) {
        if (mScopes.isEmpty()) {
            // unregister detector because there are no presenters which could be recovered.
            // next #save call will create a new one
            if (mActivityInstanceObserver != null) {
                if (DEBUG) {
                    TiLog.v(TAG, "unregistering lifecycle callback");
                }
                activity.getApplication()
                        .unregisterActivityLifecycleCallbacks(mActivityInstanceObserver);
                mActivityInstanceObserver = null;
            }
        }
    }
}
