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

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.UUID;

import static android.content.ContentValues.TAG;
import static net.grandcentrix.thirtyinch.internal.PresenterSavior.TI_ACTIVITY_PRESENTER_SCOPE_KEY;

/**
 * Keeps track of {@link Activity}s across orientation changes using a unique id when added via
 * {@link #startTracking(Activity)}. When the {@link Activity} finishes the {@link Listener} is
 * triggered.
 */
public class ActivityInstanceObserver implements Application.ActivityLifecycleCallbacks {

    /**
     * Callback when an {@link Activity} will be completely destroyed
     */
    interface Listener {

        /**
         * called when the {@link Activity} finishes completely. Doesn't get called when the
         * Activity changes its configuration
         */
        void onActivityFinished(final Activity activity, final String activityId);
    }

    private Listener mListener;

    private final HashMap<Activity, String> mScopeIdForActivity = new HashMap<>();

    public ActivityInstanceObserver(@NonNull final Listener listener) {
        mListener = listener;
    }

    /**
     * a unique id for each {@link Activity} which doesn't change when the {@link Activity} changes
     * its configuration
     */
    @Nullable
    public String getActivityId(final Activity activity) {
        return mScopeIdForActivity.get(activity);
    }

    @Override
    public void onActivityCreated(final Activity activity, final Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            final String scopeId = savedInstanceState.getString(TI_ACTIVITY_PRESENTER_SCOPE_KEY);
            if (scopeId != null) {
                // refresh mapping
                mScopeIdForActivity.put(activity, scopeId);
            }
        }
    }

    @Override
    public void onActivityDestroyed(final Activity activity) {

        TiLog.v(TAG, "destroying " + activity);
        TiLog.v(TAG, "isFinishing = " + activity.isFinishing());
        TiLog.v(TAG, "isChangingConfigurations = " + activity.isChangingConfigurations());

        // TODO check if "Don't keep Activities" case is handled correctly

        if (activity.isFinishing() && !activity.isChangingConfigurations()) {
            // detected Activity finish, no new Activity instance will be created
            // with savedInstanceState, clear saved presenters
            final String scopeId = mScopeIdForActivity.remove(activity);
            mListener.onActivityFinished(activity, scopeId);
        } else {
            // don't leak old activity instances
            // scopeId is saved in savedInstanceState of finishing Activity.
            mScopeIdForActivity.remove(activity);
        }
    }

    @Override
    public void onActivityPaused(final Activity activity) {
        // noop
    }

    @Override
    public void onActivityResumed(final Activity activity) {
        // noop
    }

    @Override
    public void onActivitySaveInstanceState(final Activity activity, final Bundle outState) {
        String id = mScopeIdForActivity.get(activity);
        if (id == null) {
            // activity not managed, don't add an id.
            return;
        }
        outState.putString(TI_ACTIVITY_PRESENTER_SCOPE_KEY, id);
    }

    @Override
    public void onActivityStarted(final Activity activity) {
        // noop
    }

    @Override
    public void onActivityStopped(final Activity activity) {
        // noop
    }

    /**
     * tracks the Activity over orientation changes using a unique id. Use
     * {@link #getActivityId(Activity)} to get the current Activity instance with the id returned
     * from this method
     *
     * @param activity to be tracked {@link Activity}
     * @return a unique id for this Activity
     * @see #getActivityId(Activity)
     */
    public String startTracking(final Activity activity) {
        final String activityId = UUID.randomUUID().toString();
        mScopeIdForActivity.put(activity, activityId);
        return activityId;
    }
}
