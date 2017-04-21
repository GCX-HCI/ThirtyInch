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

import net.grandcentrix.thirtyinch.TiLog;
import net.grandcentrix.thirtyinch.TiPresenter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
public class PresenterSavior implements TiPresenterSavior, ActivityInstanceObserver.Listener {

    private static PresenterSavior INSTANCE;

    private static final String TAG = PresenterSavior.class.getSimpleName();

    @VisibleForTesting
    static final String TI_ACTIVITY_PRESENTER_SCOPE_KEY = "ThirtyInch_presenter_scope_id";

    @VisibleForTesting
    ActivityInstanceObserver mActivityInstanceObserver;

    /**
     * Holds a scope for every Activity with one or more presenters. There is no direct mapping for
     * {@link Activity} to {@link ActivityScopedPresenters} because Activity instances can be
     * destroyed. The {@link ActivityInstanceObserver} takes care to manage unique Ids for each
     * Activity which are used as keys here.
     */
    @VisibleForTesting
    final HashMap<String, ActivityScopedPresenters> mScopes = new HashMap<>();

    /**
     * Access to the {@link PresenterSavior} singleton to save presenters across orientation changes
     *
     * @return singleton for
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
    public void free(final String presenterId, @NonNull final Activity activity) {
        final ActivityScopedPresenters scope = getScope(activity);
        if (scope != null) {
            scope.remove(presenterId);
            if (scope.isEmpty()) {
                String scopeId = mActivityInstanceObserver.getActivityId(activity);
                if (scopeId != null) {
                    mScopes.remove(scopeId);
                }
            }
        }
        if (mScopes.isEmpty()) {
            if (mActivityInstanceObserver != null) {
                activity.getApplication()
                        .unregisterActivityLifecycleCallbacks(mActivityInstanceObserver);
                mActivityInstanceObserver = null;
            }
        }

        printRemainingStore();
    }

    @Override
    public void onActivityFinished(final Activity activity, final String activityId) {
        final ActivityScopedPresenters scope = mScopes.remove(activityId);

        if (mScopes.isEmpty()) {
            // unregister detector because there are no presenters which could be recovered.
            // next #save call will create a new one
            if (mActivityInstanceObserver != null) {
                TiLog.v(TAG, "unregistering lifecycle callback");
                activity.getApplication()
                        .unregisterActivityLifecycleCallbacks(mActivityInstanceObserver);
                mActivityInstanceObserver = null;
            }
        }
        TiLog.d(TAG, "Activity is really finishing!");
        if (scope != null) {
            for (final Map.Entry<String, TiPresenter> entry : scope.getAllMappings()) {
                final String presenterId = entry.getKey();
                final TiPresenter presenter = entry.getValue();

                // when the presenter is not destroyed yet, destroy it.
                if (!presenter.isDestroyed()) {
                    presenter.destroy();
                }
                scope.remove(presenterId);
            }
        }

        printRemainingStore();
    }

    @Override
    @Nullable
    public TiPresenter recover(final String presenterId, @NonNull final Activity activity) {
        final ActivityScopedPresenters scope = getScope(activity);
        if (scope == null) {
            return null;
        }
        return scope.get(presenterId);
    }

    @Override
    public String save(@NonNull final TiPresenter presenter, @NonNull final Activity activity) {
        final String id = generatePresenterId(presenter);
        TiLog.v(TAG, "save presenter with id " + id + " " + presenter);

        final ActivityScopedPresenters scope = getScopeOrCreate(activity);
        scope.save(id, presenter);
        printRemainingStore();

        return id;
    }

    @VisibleForTesting
    int getPresenterCount() {
        if (mScopes.isEmpty()) {
            return 0;
        }

        int size = 0;
        for (final ActivityScopedPresenters scope : mScopes.values()) {
            size += scope.size();
        }
        return size;
    }

    private String generatePresenterId(@NonNull final TiPresenter presenter) {
        return presenter.getClass().getSimpleName()
                + ":" + presenter.hashCode()
                + ":" + System.nanoTime();
    }

    @Nullable
    private synchronized ActivityScopedPresenters getScope(final Activity activity) {
        final ActivityInstanceObserver detector = mActivityInstanceObserver;
        if (detector == null) {
            return null;
        }

        final String scopeId = detector.getActivityId(activity);
        if (scopeId == null) {
            return null;
        }

        return mScopes.get(scopeId);
    }

    /**
     * Returns an existing scope or creates a new one for the given Activity. Registers a callback
     * so the scope will be cleaned up when the Activity finishes
     *
     * @return an existing or new created scope for presenters
     */
    @NonNull
    private synchronized ActivityScopedPresenters getScopeOrCreate(final Activity activity) {
        if (mActivityInstanceObserver == null) {
            mActivityInstanceObserver = new ActivityInstanceObserver(this);
            TiLog.v(TAG, "registering lifecycle callback");
            activity.getApplication().registerActivityLifecycleCallbacks(mActivityInstanceObserver);
        }

        final String scopeId = mActivityInstanceObserver.getActivityId(activity);
        final ActivityScopedPresenters scope;

        if (scopeId == null) {
            final String newScopeId = mActivityInstanceObserver.startTracking(activity);
            scope = new ActivityScopedPresenters();
            mScopes.put(newScopeId, scope);
        } else {
            final ActivityScopedPresenters savedScope = mScopes.get(scopeId);
            if (savedScope == null) {
                scope = new ActivityScopedPresenters();
                mScopes.put(scopeId, scope);
            } else {
                scope = savedScope;
            }
        }

        return scope;
    }

    private void printRemainingStore() {
        final ArrayList<TiPresenter> presenters = new ArrayList<>();
        for (final Map.Entry<String, ActivityScopedPresenters> entry : mScopes.entrySet()) {
            presenters.addAll(entry.getValue().getAll());
        }

        TiLog.d(TAG, "presenter count " + presenters.size());
        for (final TiPresenter presenter : presenters) {
            TiLog.v(TAG, " - " + presenter);
        }
    }
}
