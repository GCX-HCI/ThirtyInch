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
import android.app.Application;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import java.util.HashMap;
import net.grandcentrix.thirtyinch.TiLog;

/**
 * Keeps track of {@link Activity}s across orientation changes using a id when added via
 * {@link #startTracking(Activity, String)}. When the {@link Activity} finishes the
 * {@link ActivityFinishListener} is triggered.
 */
public class ActivityInstanceObserver implements Application.ActivityLifecycleCallbacks {

    /**
     * Callback when an {@link Activity} will be completely destroyed
     */
    public interface ActivityFinishListener {

        /**
         * called when the {@link Activity} finishes completely. Doesn't get called when the
         * Activity changes its configuration
         */
        void onActivityFinished(final Activity activity, final String hostId);
    }

    @VisibleForTesting
    static final String TI_ACTIVITY_ID_KEY = "ThirtyInch_Activity_id";

    private static final String TAG = ActivityInstanceObserver.class.getSimpleName();

    private ActivityFinishListener mListener;

    private final HashMap<Activity, String> mScopeIdForActivity = new HashMap<>();

    public ActivityInstanceObserver(@NonNull final ActivityFinishListener listener) {
        mListener = listener;
    }

    @Override
    public void onActivityCreated(final Activity activity, final Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            final String scopeId = savedInstanceState.getString(TI_ACTIVITY_ID_KEY);
            if (scopeId != null) {
                // refresh mapping
                mScopeIdForActivity.put(activity, scopeId);
            }
        }
    }

    /**
     * Returns the id provided by {@link #startTracking(Activity, String)}
     *
     * @return a unique id for each {@link Activity} which doesn't change when the {@link Activity}
     * changes its configuration
     */
    @Nullable
    public String getActivityId(final Activity activity) {
        return mScopeIdForActivity.get(activity);
    }

    @Override
    public void onActivityDestroyed(final Activity activity) {
        TiLog.v(TAG, "destroying " + activity);
        TiLog.v(TAG, "isFinishing = " + activity.isFinishing());

        if (activity.isFinishing()) {
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
        outState.putString(TI_ACTIVITY_ID_KEY, id);
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
     * tracks the Activity over orientation changes using the passed in id. Use
     * {@link #getActivityId(Activity)} to get the current Activity instance with the id
     *
     * @param activity to be tracked {@link Activity}
     * @see #getActivityId(Activity)
     */
    public void startTracking(final Activity activity, final String activityId) {
        mScopeIdForActivity.put(activity, activityId);
    }
}
