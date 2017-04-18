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

import net.grandcentrix.thirtyinch.TiActivity;
import net.grandcentrix.thirtyinch.TiLog;
import net.grandcentrix.thirtyinch.TiPresenter;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Activities can be destroyed when the device runs out of memory. Sometimes it doesn't work to
 * save objects via {@code Activity#onRetainNonConfigurationInstance()} for example when the user
 * has enabled "Do not keep activities" in the developer options. This singleton holds strong
 * references to those presenters and returns them when needed.
 *
 * {@link TiActivity} is responsible to manage the references
 */
public class PresenterSavior implements TiPresenterSavior {

    private static PresenterSavior INSTANCE;

    private static final String TAG = PresenterSavior.class.getSimpleName();

    static final String TI_ACTIVITY_PRESENTER_SCOPE_KEY = "ThirtyInch_presenter_scope_id";

    @VisibleForTesting
    Application.ActivityLifecycleCallbacks mLifecycleCallbacks;

    @VisibleForTesting
    final HashMap<String, ActivityScopedPresenters> mScopes = new HashMap<>();

    private final HashMap<Activity, String> mScopeIdForActivity = new HashMap<>();

    public static synchronized PresenterSavior getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PresenterSavior();
        }
        return INSTANCE;
    }

    @VisibleForTesting
    /*package*/ PresenterSavior() {

    }

    public void detectNewActivity(final Activity activity, final Bundle savedInstanceState) {
        TiLog.d(TAG, "detectNewActivity() called with: activity = [" + activity
                + "], savedInstanceState = [" + savedInstanceState + "]");
        if (savedInstanceState != null) {
            final String id = savedInstanceState.getString(TI_ACTIVITY_PRESENTER_SCOPE_KEY);
            if (id != null) {
                // refresh mapping
                mScopeIdForActivity.put(activity, id);
            }
        }
    }

    @Override
    public void free(final String presenterId, @NonNull final Activity activity) {
        final ActivityScopedPresenters scope = getScope(activity);
        if (scope != null) {
            scope.remove(presenterId);
            clearScopeWhenPossible(activity);
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
        final String id = generateId(presenter);
        TiLog.v(TAG, "save presenter with id " + id + " " + presenter);

        final ActivityScopedPresenters scope = getScopeOrCreate(activity);
        scope.save(id, presenter);
        printRemainingStore();

        return id;
    }

    /**
     * the {@link Activity} is in terminal state and got finished. cleanup all presenters
     */
    void cleanupAfterFinish(@NonNull final Activity activity) {
        TiLog.d(TAG, "cleanupAfterFinish() called with: activity = [" + activity + "]");

        final String scopeId = mScopeIdForActivity.remove(activity);
        final ActivityScopedPresenters scope = mScopes.remove(scopeId);

        if (mScopeIdForActivity.isEmpty()) {
            TiLog.v(TAG, "unregistering lifecycle callback");
            if (mLifecycleCallbacks != null) {
                activity.getApplication().unregisterActivityLifecycleCallbacks(mLifecycleCallbacks);
            }
        }

        TiLog.d(TAG, "Activity is really finishing!");
        if (scope != null) {
            for (final Map.Entry<String, TiPresenter> entry : scope.getAllMappings()) {
                final String key = entry.getKey();
                final TiPresenter presenter = entry.getValue();
                if (!presenter.isDestroyed()) {
                    presenter.destroy();
                }
                scope.remove(key);
            }
        }

        printRemainingStore();
    }

    @VisibleForTesting
    /*package*/ int getPresenterCount() {

        final ArrayList<TiPresenter> presenters = new ArrayList<>();
        for (final Map.Entry<String, ActivityScopedPresenters> entry : mScopes.entrySet()) {
            presenters.addAll(entry.getValue().getAll());
        }
        return presenters.size();
    }

    private synchronized void clearScopeWhenPossible(@NonNull final Activity activity) {
        final String scopeId = mScopeIdForActivity.get(activity);
        if (scopeId == null) {
            return;
        }
        final ActivityScopedPresenters scope = mScopes.get(scopeId);
        if (scope == null) {
            return;
        }
        if (scope.getAll().isEmpty()) {
            // remove empty scope
            mScopes.remove(mScopeIdForActivity.remove(activity));

            if (mScopes.isEmpty()) {
                if (mLifecycleCallbacks != null) {
                    activity.getApplication()
                            .unregisterActivityLifecycleCallbacks(mLifecycleCallbacks);
                    mLifecycleCallbacks = null;
                }
            }
        }
    }

    private String generateId(@NonNull final TiPresenter presenter) {
        return presenter.getClass().getSimpleName()
                + ":" + presenter.hashCode()
                + ":" + System.nanoTime();
    }

    @NonNull
    private Application.ActivityLifecycleCallbacks getActivityLifecycleCallbacks() {
        return new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(final Activity activity,
                    final Bundle savedInstanceState) {
                detectNewActivity(activity, savedInstanceState);
            }

            @Override
            public void onActivityDestroyed(final Activity activity) {

                TiLog.v(TAG, "destroying " + activity);
                TiLog.v(TAG, "isFinishing = " + activity.isFinishing());
                TiLog.v(TAG,
                        "isChangingConfigurations = " + activity.isChangingConfigurations());

                if (activity.isFinishing() && !activity.isChangingConfigurations()) {
                    // detected Activity finish, no new Activity instance will be created
                    // with savedInstanceState, clear saved presenters
                    cleanupAfterFinish(activity);
                } else {
                    // don't leak old activity instances
                    // scopeId is saved in savedInstanceState of finishing Activity.
                    mScopeIdForActivity.remove(activity);
                }
            }

            @Override
            public void onActivityPaused(final Activity activity) {

            }

            @Override
            public void onActivityResumed(final Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(final Activity activity,
                    final Bundle outState) {
                saveScopeId(activity, outState);
            }

            @Override
            public void onActivityStarted(final Activity activity) {

            }

            @Override
            public void onActivityStopped(final Activity activity) {

            }
        };
    }

    public void saveScopeId(final Activity activity, final Bundle outState) {
        String id = mScopeIdForActivity.get(activity);
        if (id == null) {
            id = UUID.randomUUID().toString();
            mScopeIdForActivity.put(activity, id);
        }
        outState.putString(TI_ACTIVITY_PRESENTER_SCOPE_KEY, id);
    }

    private synchronized ActivityScopedPresenters getScope(final Activity activity) {
        final String id = mScopeIdForActivity.get(activity);
        if (id != null) {
            return mScopes.get(id);
        }

        return null;
    }

    private synchronized ActivityScopedPresenters getScopeOrCreate(final Activity activity) {
        if (mLifecycleCallbacks == null) {
            mLifecycleCallbacks = getActivityLifecycleCallbacks();
            TiLog.v(TAG, "registering lifecycle callback");
            final Application application = activity.getApplication();
            application.registerActivityLifecycleCallbacks(mLifecycleCallbacks);
        }

        final String scopeId = mScopeIdForActivity.get(activity);
        final ActivityScopedPresenters scope;

        if (scopeId == null) {
            final String id = UUID.randomUUID().toString();
            mScopeIdForActivity.put(activity, id);
            scope = new ActivityScopedPresenters();
            mScopes.put(id, scope);
        } else {
            final ActivityScopedPresenters gotScope = mScopes.get(scopeId);
            if (gotScope == null) {
                scope = new ActivityScopedPresenters();
                mScopes.put(scopeId, scope);
            } else {
                scope = gotScope;
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
