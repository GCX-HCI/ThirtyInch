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
import net.grandcentrix.thirtyinch.util.AndroidDeveloperOptions;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
public enum PresenterSavior implements TiPresenterSavior {

    INSTANCE;

    private static final String TAG = PresenterSavior.class.getSimpleName();

    private static final String TI_ACTIVITY_ID_KEY = "ThirtyInch_activity_id";

    private HashMap<String, Activity> mActivitys = new HashMap<>();

    private Application.ActivityLifecycleCallbacks mLifecycleCallbacks;

    private HashMap<Activity, String> mScopeIds = new HashMap<>();

    private HashMap<String, ActivityScopedPresenters> mScopes = new HashMap<>();

    @Override
    public void free(final String presenterId, final Activity activity) {
        final ActivityScopedPresenters scope = getScope(activity);
        scope.remove(presenterId);

        printRemainingStore();
    }

    public synchronized ActivityScopedPresenters getScope(final Activity activity) {

        if (mLifecycleCallbacks == null) {
            mLifecycleCallbacks = new Application.ActivityLifecycleCallbacks() {
                @Override
                public void onActivityCreated(final Activity activity,
                        final Bundle savedInstanceState) {
                    String activityId = null;
                    if (savedInstanceState != null) {
                        final String id = savedInstanceState.getString(TI_ACTIVITY_ID_KEY);
                        if (id != null) {
                            activityId = id;
                        }
                    }

                    if (activityId == null) {
                        activityId = UUID.randomUUID().toString();
                    }

                    mActivitys.put(activityId, activity);
                    mScopeIds.put(activity, activityId);
                }

                @Override
                public void onActivityDestroyed(final Activity activity) {

                    final String scopeId = mScopeIds.remove(activity);
                    if (scopeId != null) {
                        mActivitys.remove(scopeId);
                    }

                    TiLog.v(TAG, "destroying " + activity);
                    TiLog.v(TAG, "isFinishing = " + activity.isFinishing());
                    TiLog.v(TAG,
                            "isChangingConfigurations = " + activity.isChangingConfigurations());
                    TiLog.v(TAG, "dontKeepActivities = " + AndroidDeveloperOptions
                            .isDontKeepActivitiesEnabled(activity));

                    if (activity.isFinishing() && !activity.isChangingConfigurations()) {
                        TiLog.d(TAG, "Activity is really finishing!");
                        final ActivityScopedPresenters scope = mScopes.remove(scopeId);
                        if (scope != null) {
                            scope.clear();
                        }

                        printRemainingStore();
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
                    String id = mScopeIds.get(activity);
                    if (id == null) {
                        id = UUID.randomUUID().toString();
                        mActivitys.put(id, activity);
                        mScopeIds.put(activity, id);
                    }
                    outState.putString(TI_ACTIVITY_ID_KEY, id);
                }

                @Override
                public void onActivityStarted(final Activity activity) {

                }

                @Override
                public void onActivityStopped(final Activity activity) {

                }
            };
            activity.getApplication().registerActivityLifecycleCallbacks(mLifecycleCallbacks);
        }

        final String scopeId = mScopeIds.get(activity);
        final ActivityScopedPresenters scope;

        if (scopeId == null) {
            final String id = UUID.randomUUID().toString();
            mActivitys.put(id, activity);
            mScopeIds.put(activity, id);
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

    @Override
    @Nullable
    public TiPresenter recover(final String presenterId, final Activity activity) {
        final ActivityScopedPresenters scope = getScope(activity);
        return scope.get(presenterId);
    }

    @Override
    public String save(@NonNull final TiPresenter presenter, final Activity activity) {
        final String id = generateId(presenter);
        TiLog.v(TAG, "save presenter with id " + id + " " + presenter);

        final ActivityScopedPresenters scope = getScope(activity);
        scope.save(id, presenter);
        printRemainingStore();

        return id;
    }

    private String generateId(@NonNull final TiPresenter presenter) {
        return presenter.getClass().getSimpleName()
                + ":" + presenter.hashCode()
                + ":" + System.nanoTime();
    }

    private void printRemainingStore() {
        final ArrayList<TiPresenter> presenters = new ArrayList<>();
        final Iterator<Map.Entry<String, ActivityScopedPresenters>> iterator =
                mScopes.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry<String, ActivityScopedPresenters> entry = iterator.next();
            presenters.addAll(entry.getValue().getAll());
        }

        TiLog.d(TAG, "presenter count " + presenters.size());
        for (final TiPresenter presenter : presenters) {
            TiLog.v(TAG, " - " + presenter);
        }
    }
}
