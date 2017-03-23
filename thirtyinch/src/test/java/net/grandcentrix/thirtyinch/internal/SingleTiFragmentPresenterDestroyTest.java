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

import net.grandcentrix.thirtyinch.TiConfiguration;
import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.TiView;

import org.junit.Test;

import android.view.LayoutInflater;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class SingleTiFragmentPresenterDestroyTest extends TiFragmentPresenterDestroyTest {

    /**
     * Activity changing configuration without retain
     *
     * verified by:
     * - pascal
     */
    @Test
    public void saviorFalse_retainFalse_dontKeepActivitiesFalse_activityChangingConfiguration() {

        final TiFragmentDelegateBuilder.HostingActivity
                hostingActivity = new TiFragmentDelegateBuilder.HostingActivity();

        // Given a Presenter that does not use a static savior and does not retain itself.
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(false)
                .setRetainPresenterEnabled(false)
                .build());

        // And given a Fragment.
        final TiFragmentDelegate<TiPresenter<TiView>, TiView> delegate
                = new TiFragmentDelegateBuilder()
                .setDontKeepActivitiesEnabled(false)
                .setHostingActivity(hostingActivity)
                .setSavior(mSavior)
                .setPresenter(presenter)
                .build();

        // When the Fragment is added to the Activity.
        delegate.onCreate_afterSuper(null);
        delegate.onCreateView_beforeSuper(mock(LayoutInflater.class), null, null);
        delegate.onStart_afterSuper();

        // Then the presenter will be stored in the savior
        assertThat(mSavior.presenterCount()).isEqualTo(0);

        // And when the Activity is changing configurations.
        hostingActivity.setChangingConfiguration(true);
        delegate.onSaveInstanceState_afterSuper(mSavedState);
        delegate.onStop_beforeSuper();
        delegate.onDestroyView_beforeSuper();
        delegate.onDestroy_afterSuper();

        // Then assert that the Presenter is destroyed and not saved in the savior.
        assertThat(delegate.getPresenter().isDestroyed()).isTrue();
        assertThat(mSavior.presenterCount()).isEqualTo(0);

        // When the Activity gets recreated.
        final TiFragmentDelegateBuilder.HostingActivity
                hostingActivity2 = new TiFragmentDelegateBuilder.HostingActivity();

        // And generates a new Fragment instance.
        final TestPresenter presenter2 = new TestPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(false)
                .setRetainPresenterEnabled(false)
                .build());
        final TiFragmentDelegate<TiPresenter<TiView>, TiView> delegate2
                = new TiFragmentDelegateBuilder()
                .setDontKeepActivitiesEnabled(false)
                .setHostingActivity(hostingActivity2)
                .setSavior(mSavior)
                .setPresenter(presenter2)
                .build();

        delegate2.onCreate_afterSuper(mSavedState);
        delegate2.onCreateView_beforeSuper(mock(LayoutInflater.class), null, mSavedState);
        delegate2.onStart_afterSuper();

        // Then a new Presenter will be attached and the previous presenter doesn't get reattached.
        assertThat(delegate2.getPresenter()).isNotEqualTo(presenter).isEqualTo(presenter2);
    }

    /**
     * Activity move to background -> move to foreground without retain
     *
     * verified by:
     * - pascal
     */
    @Test
    public void saviorFalse_retainFalse_dontKeepActivitiesFalse_moveToBackground_moveToForeground() {

        final TiFragmentDelegateBuilder.HostingActivity
                hostingActivity = new TiFragmentDelegateBuilder.HostingActivity();

        // Given a Presenter that does not use a static savior and does not retain itself.
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(false)
                .setRetainPresenterEnabled(false)
                .build());

        // And given a Fragment.
        final TiFragmentDelegate<TiPresenter<TiView>, TiView> delegate
                = new TiFragmentDelegateBuilder()
                .setDontKeepActivitiesEnabled(false)
                .setHostingActivity(hostingActivity)
                .setSavior(mSavior)
                .setPresenter(presenter)
                .build();

        // When the Fragment is added to the Activity.
        delegate.onCreate_afterSuper(null);
        delegate.onCreateView_beforeSuper(mock(LayoutInflater.class), null, null);
        delegate.onStart_afterSuper();

        // Then the Presenter will *not* be stored in the savior
        assertThat(mSavior.presenterCount()).isEqualTo(0);

        // When the Activity is moved to background
        delegate.onSaveInstanceState_afterSuper(mSavedState);
        delegate.onStop_beforeSuper();

        // Then the Presenter gets not destroyed and is not saved in savior
        assertThat(delegate.getPresenter().isDestroyed()).isFalse();
        assertThat(mSavior.presenterCount()).isEqualTo(0);

        // When the Activity gets moved to foreground
        delegate.onStart_afterSuper();

        // Then the Presenter stays the same
        assertThat(delegate.getPresenter()).isEqualTo(presenter);
        assertThat(mSavior.presenterCount()).isEqualTo(0);
        assertThat(delegate.getPresenter().isDestroyed()).isFalse();
    }

    /**
     * Activity move to background -> move to foreground without retain (don't keep Activities)
     *
     * verified by:
     * - pascal
     */
    @Test
    public void saviorFalse_retainFalse_dontKeepActivitiesTrue_moveToBackground_moveToForeground() {

        final TiFragmentDelegateBuilder.HostingActivity
                hostingActivity = new TiFragmentDelegateBuilder.HostingActivity();

        // Given a Presenter that does not use a static savior and does not retain itself.
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(false)
                .setRetainPresenterEnabled(false)
                .build());

        // And given a Fragment.
        final TiFragmentDelegate<TiPresenter<TiView>, TiView> delegate
                = new TiFragmentDelegateBuilder()
                .setDontKeepActivitiesEnabled(true)
                .setHostingActivity(hostingActivity)
                .setSavior(mSavior)
                .setPresenter(presenter)
                .build();

        // When the Fragment is added to the Activity.
        delegate.onCreate_afterSuper(null);
        delegate.onCreateView_beforeSuper(mock(LayoutInflater.class), null, null);
        delegate.onStart_afterSuper();

        // Then the Presenter will *not* be stored in the savior
        assertThat(mSavior.presenterCount()).isEqualTo(0);

        // When the Activity gets moved to background
        delegate.onSaveInstanceState_afterSuper(mSavedState);
        delegate.onStop_beforeSuper();
        delegate.onDestroyView_beforeSuper();
        delegate.onDestroy_afterSuper();

        // Then the presenter gets destroyed
        assertThat(delegate.getPresenter().isDestroyed()).isTrue();
        assertThat(mSavior.presenterCount()).isEqualTo(0);

        // When the Activity moves to foreground again
        // A new Activity gets created by the Android Framework.
        final TiFragmentDelegateBuilder.HostingActivity
                hostingActivity2 = new TiFragmentDelegateBuilder.HostingActivity();

        // And generates a new Fragment instance.
        final TestPresenter presenter2 = new TestPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(false)
                .setRetainPresenterEnabled(false)
                .build());
        final TiFragmentDelegate<TiPresenter<TiView>, TiView> delegate2
                = new TiFragmentDelegateBuilder()
                .setDontKeepActivitiesEnabled(true)
                .setHostingActivity(hostingActivity2)
                .setSavior(mSavior)
                .setPresenter(presenter2)
                .build();

        delegate2.onCreate_afterSuper(mSavedState);
        delegate2.onCreateView_beforeSuper(mock(LayoutInflater.class), null, null);
        delegate2.onStart_afterSuper();

        // Then a new Presenter is created for the new Fragment instance
        assertThat(delegate2.getPresenter()).isNotEqualTo(presenter).isEqualTo(presenter2);
        assertThat(delegate2.getPresenter().isDestroyed()).isEqualTo(false);
        assertThat(mSavior.presenterCount()).isEqualTo(0);
    }

    /**
     * Activity changing configuration without retain (don't keep Activities)
     *
     * verified by:
     * - pascal
     */
    @Test
    public void saviorFalse_retainFalse_dontKeepActivitiesTrue_activityChangingConfiguration() {

        final TiFragmentDelegateBuilder.HostingActivity
                hostingActivity = new TiFragmentDelegateBuilder.HostingActivity();

        // Given a Presenter that does not use a static savior and does not retain itself.
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(false)
                .setRetainPresenterEnabled(false)
                .build());

        // And given a Fragment.
        final TiFragmentDelegate<TiPresenter<TiView>, TiView> delegate
                = new TiFragmentDelegateBuilder()
                .setDontKeepActivitiesEnabled(true)
                .setHostingActivity(hostingActivity)
                .setSavior(mSavior)
                .setPresenter(presenter)
                .build();

        // When the Fragment is added to the Activity.
        delegate.onCreate_afterSuper(null);
        delegate.onCreateView_beforeSuper(mock(LayoutInflater.class), null, null);
        delegate.onStart_afterSuper();

        // Then the presenter will be stored in the savior
        assertThat(mSavior.presenterCount()).isEqualTo(0);

        // And when the Activity is changing configurations.
        hostingActivity.setChangingConfiguration(true);
        delegate.onSaveInstanceState_afterSuper(mSavedState);
        delegate.onStop_beforeSuper();
        delegate.onDestroyView_beforeSuper();
        delegate.onDestroy_afterSuper();

        // Then assert that the Presenter is destroyed and not saved in the savior.
        assertThat(delegate.getPresenter().isDestroyed()).isTrue();
        assertThat(mSavior.presenterCount()).isEqualTo(0);

        // When the Activity gets recreated.
        final TiFragmentDelegateBuilder.HostingActivity
                hostingActivity2 = new TiFragmentDelegateBuilder.HostingActivity();

        // And a new Fragment instance is created by the framework.
        final TestPresenter presenter2 = new TestPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(false)
                .setRetainPresenterEnabled(false)
                .build());
        final TiFragmentDelegate<TiPresenter<TiView>, TiView> delegate2
                = new TiFragmentDelegateBuilder()
                .setDontKeepActivitiesEnabled(true)
                .setHostingActivity(hostingActivity2)
                .setSavior(mSavior)
                .setPresenter(presenter2)
                .build();

        delegate2.onCreate_afterSuper(mSavedState);
        delegate2.onCreateView_beforeSuper(mock(LayoutInflater.class), null, mSavedState);
        delegate2.onStart_afterSuper();

        // Then a new Presenter will be attached and the previous presenter doesn't get reattached.
        assertThat(delegate2.getPresenter()).isNotEqualTo(presenter).isEqualTo(presenter2);
    }

    /**
     * Activity finish without retain
     *
     * verified by:
     * - pascal
     */
    @Test
    public void saviorFalse_retainFalse_dontKeepActivitiesFalse_activityFinishing() {

        final TiFragmentDelegateBuilder.HostingActivity
                hostingActivity = new TiFragmentDelegateBuilder.HostingActivity();

        // Given a Presenter that does not use a static savior and does not retain itself.
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(false)
                .setRetainPresenterEnabled(false)
                .build());

        // And given a Fragment.
        final TiFragmentDelegate<TiPresenter<TiView>, TiView> delegate
                = new TiFragmentDelegateBuilder()
                .setDontKeepActivitiesEnabled(false)
                .setHostingActivity(hostingActivity)
                .setSavior(mSavior)
                .setPresenter(presenter)
                .build();

        // When the Fragment is added to the Activity.
        delegate.onCreate_afterSuper(null);
        delegate.onCreateView_beforeSuper(mock(LayoutInflater.class), null, null);
        delegate.onStart_afterSuper();

        // Then the presenter will be stored in the savior
        assertThat(mSavior.presenterCount()).isEqualTo(0);

        // And when the activity is finishing.
        hostingActivity.setFinishing(true);
        delegate.onStop_beforeSuper();
        delegate.onDestroyView_beforeSuper();
        delegate.onDestroy_afterSuper();

        // Then assert that the Presenter is destroyed and not saved in the savior.
        assertThat(delegate.getPresenter().isDestroyed()).isTrue();
        assertThat(mSavior.presenterCount()).isEqualTo(0);
    }

    /**
     * Activity finish without retain  (don't keep Activities)
     *
     * verified by:
     * - pascal
     */
    @Test
    public void saviorFalse_retainFalse_dontKeepActivitiesTrue_activityFinishing() {

        final TiFragmentDelegateBuilder.HostingActivity
                hostingActivity = new TiFragmentDelegateBuilder.HostingActivity();

        // Given a Presenter that does not use a static savior and does not retain itself.
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(false)
                .setRetainPresenterEnabled(false)
                .build());

        // And given a Fragment.
        final TiFragmentDelegate<TiPresenter<TiView>, TiView> delegate
                = new TiFragmentDelegateBuilder()
                .setDontKeepActivitiesEnabled(true)
                .setHostingActivity(hostingActivity)
                .setSavior(mSavior)
                .setPresenter(presenter)
                .build();

        // When the Fragment is added to the Activity.
        delegate.onCreate_afterSuper(null);
        delegate.onCreateView_beforeSuper(mock(LayoutInflater.class), null, null);
        delegate.onStart_afterSuper();

        // Then the presenter will be stored in the savior
        assertThat(mSavior.presenterCount()).isEqualTo(0);

        // And when the Activity is finishing.
        hostingActivity.setFinishing(true);
        delegate.onStop_beforeSuper();
        delegate.onDestroyView_beforeSuper();
        delegate.onDestroy_afterSuper();

        // Then assert that the Presenter is destroyed and not saved in the savior.
        assertThat(delegate.getPresenter().isDestroyed()).isTrue();
        assertThat(mSavior.presenterCount()).isEqualTo(0);
    }

    /**
     * Activity changing configuration without savior
     *
     * verified by:
     * - pascal
     */
    @Test
    public void saviorFalse_retainTrue_dontKeepActivitiesFalse_activityChangingConfiguration() {

        final TiFragmentDelegateBuilder.HostingActivity
                hostingActivity = new TiFragmentDelegateBuilder.HostingActivity();

        // Given a Presenter that does not use a static savior but does retain itself.
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(false)
                .setRetainPresenterEnabled(true)
                .build());

        // And given a Fragment.
        final TiFragmentDelegate<TiPresenter<TiView>, TiView> delegate
                = new TiFragmentDelegateBuilder()
                .setDontKeepActivitiesEnabled(false)
                .setHostingActivity(hostingActivity)
                .setSavior(mSavior)
                .setPresenter(presenter)
                .build();

        // When the Fragment is added to the Activity.
        delegate.onCreate_afterSuper(null);
        delegate.onCreateView_beforeSuper(mock(LayoutInflater.class), null, null);
        delegate.onStart_afterSuper();

        // Then the presenter will *not* be stored in the savior
        assertThat(mSavior.presenterCount()).isEqualTo(0);

        // And when the Activity is changing its configuration.
        hostingActivity.setChangingConfiguration(true);
        delegate.onSaveInstanceState_afterSuper(mSavedState);
        delegate.onStop_beforeSuper();
        delegate.onDestroyView_beforeSuper();

        // Then the presenter will be retained but not saved in the savior.
        assertThat(delegate.getPresenter().isDestroyed()).isFalse();
        assertThat(mSavior.presenterCount()).isEqualTo(0);

        // When new Activity instance gets created by the Android Framework.
        hostingActivity.recreateInstance();

        // And the Fragment is retained.
        delegate.onCreateView_beforeSuper(mock(LayoutInflater.class), null, mSavedState);
        delegate.onStart_afterSuper();

        // Then assert that the Presenter is not destroyed and not saved in the savior.
        assertThat(delegate.getPresenter().isDestroyed()).isFalse();
        assertThat(delegate.getPresenter()).isEqualTo(presenter);
        assertThat(mSavior.presenterCount()).isEqualTo(0);
    }

    /**
     * Activity move to background -> move to foreground without savior
     *
     * verified by:
     * - pascal
     */
    @Test
    public void saviorFalse_retainTrue_dontKeepActivitiesFalse_moveToBackground_moveToForeground() {

        final TiFragmentDelegateBuilder.HostingActivity
                hostingActivity = new TiFragmentDelegateBuilder.HostingActivity();

        // Given a Presenter that does not use a static savior but does retain itself.
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(false)
                .setRetainPresenterEnabled(true)
                .build());

        // And given a Fragment.
        final TiFragmentDelegate<TiPresenter<TiView>, TiView> delegate
                = new TiFragmentDelegateBuilder()
                .setDontKeepActivitiesEnabled(false)
                .setHostingActivity(hostingActivity)
                .setSavior(mSavior)
                .setPresenter(presenter)
                .build();

        // When the Fragment is added to the Activity.
        delegate.onCreate_afterSuper(null);
        delegate.onCreateView_beforeSuper(mock(LayoutInflater.class), null, null);
        delegate.onStart_afterSuper();

        // Then the Presenter will *not* be stored in the savior
        assertThat(mSavior.presenterCount()).isEqualTo(0);

        // When the Activity gets moved to background
        delegate.onSaveInstanceState_afterSuper(mSavedState);
        delegate.onStop_beforeSuper();

        // Then the presenter is not destroyed and not saved in the savior
        assertThat(delegate.getPresenter().isDestroyed()).isFalse();
        assertThat(mSavior.presenterCount()).isEqualTo(0);

        // When the Activity moves to foreground again
        delegate.onStart_afterSuper();

        // Then the Presenter stays the same
        assertThat(delegate.getPresenter()).isEqualTo(presenter);
        assertThat(delegate.getPresenter().isDestroyed()).isFalse();
        assertThat(mSavior.presenterCount()).isEqualTo(0);
    }


    /**
     * Activity move to background -> move to foreground without savior (don't keep Activities)
     *
     * verified by:
     * - pascal
     */
    @Test
    public void saviorFalse_retainTrue_dontKeepActivitiesTrue_moveToBackground_moveToForeground() {

        final TiFragmentDelegateBuilder.HostingActivity
                hostingActivity = new TiFragmentDelegateBuilder.HostingActivity();

        // Given a Presenter that does not use a static savior but does retain itself.
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(false)
                .setRetainPresenterEnabled(true)
                .build());

        // And given a Fragment.
        final TiFragmentDelegate<TiPresenter<TiView>, TiView> delegate
                = new TiFragmentDelegateBuilder()
                .setDontKeepActivitiesEnabled(true)
                .setHostingActivity(hostingActivity)
                .setSavior(mSavior)
                .setPresenter(presenter)
                .build();

        // When the Fragment is added to the Activity.
        delegate.onCreate_afterSuper(null);
        delegate.onCreateView_beforeSuper(mock(LayoutInflater.class), null, null);
        delegate.onStart_afterSuper();

        // Then the Presenter will *not* be stored in the savior
        assertThat(mSavior.presenterCount()).isEqualTo(0);

        // When the Activity gets moved to background
        delegate.onSaveInstanceState_afterSuper(mSavedState);
        delegate.onStop_beforeSuper();
        delegate.onDestroyView_beforeSuper();
        delegate.onDestroy_afterSuper();

        // Then the presenter is destroyed
        assertThat(delegate.getPresenter().isDestroyed()).isTrue();
        assertThat(mSavior.presenterCount()).isEqualTo(0);

        // When the Activity moves to foreground again
        // A new Activity gets created by the Android Framework.
        final TiFragmentDelegateBuilder.HostingActivity
                hostingActivity2 = new TiFragmentDelegateBuilder.HostingActivity();

        // And generates a new Fragment instance.
        final TestPresenter presenter2 = new TestPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(false)
                .setRetainPresenterEnabled(true)
                .build());
        final TiFragmentDelegate<TiPresenter<TiView>, TiView> delegate2
                = new TiFragmentDelegateBuilder()
                .setDontKeepActivitiesEnabled(true)
                .setHostingActivity(hostingActivity2)
                .setSavior(mSavior)
                .setPresenter(presenter2)
                .build();

        delegate2.onCreate_afterSuper(mSavedState);
        delegate2.onCreateView_beforeSuper(mock(LayoutInflater.class), null, null);
        delegate2.onStart_afterSuper();

        // Then a new Presenter instance was created
        assertThat(delegate2.getPresenter()).isNotEqualTo(presenter).isEqualTo(presenter2);
        assertThat(delegate2.getPresenter().isDestroyed()).isEqualTo(false);
        assertThat(mSavior.presenterCount()).isEqualTo(0);
    }

    /**
     * Activity changing configuration without savior (don't keep Activities)
     *
     * verified by:
     * - pascal
     */
    @Test
    public void saviorFalse_retainTrue_dontKeepActivitiesTrue_activityChangingConfiguration() {

        final TiFragmentDelegateBuilder.HostingActivity
                hostingActivity = new TiFragmentDelegateBuilder.HostingActivity();

        // Given a Presenter that does not use a static savior but does retain itself.
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(false)
                .setRetainPresenterEnabled(true)
                .build());

        // And given a Fragment.
        final TiFragmentDelegate<TiPresenter<TiView>, TiView> delegate
                = new TiFragmentDelegateBuilder()
                .setDontKeepActivitiesEnabled(true)
                .setHostingActivity(hostingActivity)
                .setSavior(mSavior)
                .setPresenter(presenter)
                .build();

        // When the Fragment is added to the Activity.
        delegate.onCreate_afterSuper(null);
        delegate.onCreateView_beforeSuper(mock(LayoutInflater.class), null, null);
        delegate.onStart_afterSuper();

        // Then the presenter will be stored in the savior
        assertThat(mSavior.presenterCount()).isEqualTo(0);

        // And when the Activity is changing its configuration.
        hostingActivity.setChangingConfiguration(true);
        delegate.onSaveInstanceState_afterSuper(mSavedState);
        delegate.onStop_beforeSuper();
        delegate.onDestroyView_beforeSuper();

        // Then the presenter will be retained but not saved in the savior.
        assertThat(delegate.getPresenter().isDestroyed()).isFalse();
        assertThat(mSavior.presenterCount()).isEqualTo(0);

        // When new Activity instance gets created by the Android Framework.
        hostingActivity.recreateInstance();

        // And the Fragment is retained.
        delegate.onCreateView_beforeSuper(mock(LayoutInflater.class), null, mSavedState);
        delegate.onStart_afterSuper();

        // Then assert that the Presenter is not destroyed and not saved in the savior.
        assertThat(delegate.getPresenter().isDestroyed()).isFalse();
        assertThat(delegate.getPresenter()).isEqualTo(presenter);
        assertThat(mSavior.presenterCount()).isEqualTo(0);
    }

    /**
     * Activity finish without savior
     *
     * verified by:
     * - pascal
     */
    @Test
    public void saviorFalse_retainTrue_dontKeepActivitiesFalse_activityFinishing() {

        final TiFragmentDelegateBuilder.HostingActivity
                hostingActivity = new TiFragmentDelegateBuilder.HostingActivity();

        // Given a Presenter that does not use a static savior but does retain itself.
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(false)
                .setRetainPresenterEnabled(true)
                .build());

        // And given a Fragment.
        final TiFragmentDelegate<TiPresenter<TiView>, TiView> delegate
                = new TiFragmentDelegateBuilder()
                .setDontKeepActivitiesEnabled(false)
                .setHostingActivity(hostingActivity)
                .setSavior(mSavior)
                .setPresenter(presenter)
                .build();

        // When the Fragment is added to the Activity.
        delegate.onCreate_afterSuper(null);
        delegate.onCreateView_beforeSuper(mock(LayoutInflater.class), null, null);
        delegate.onStart_afterSuper();

        // Then the presenter will be stored in the savior
        assertThat(mSavior.presenterCount()).isEqualTo(0);

        // And when the Activity is finishing.
        hostingActivity.setFinishing(true);
        delegate.onStop_beforeSuper();
        delegate.onDestroyView_beforeSuper();
        delegate.onDestroy_afterSuper();

        // Then assert that the Presenter is destroyed and not saved in the savior.
        assertThat(delegate.getPresenter().isDestroyed()).isTrue();
        assertThat(mSavior.presenterCount()).isEqualTo(0);
    }

    /**
     * Activity finish without savior (don't keep Activities)
     *
     * verified by:
     * - pascal
     */
    @Test
    public void saviorFalse_retainTrue_dontKeepActivitiesTrue_activityFinishing() {

        final TiFragmentDelegateBuilder.HostingActivity
                hostingActivity = new TiFragmentDelegateBuilder.HostingActivity();

        // Given a Presenter that does not use a static savior but does retain itself.
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(false)
                .setRetainPresenterEnabled(true)
                .build());

        // And given a Fragment.
        final TiFragmentDelegate<TiPresenter<TiView>, TiView> delegate
                = new TiFragmentDelegateBuilder()
                .setDontKeepActivitiesEnabled(true)
                .setHostingActivity(hostingActivity)
                .setSavior(mSavior)
                .setPresenter(presenter)
                .build();

        // When the Fragment is added to the Activity.
        delegate.onCreate_afterSuper(null);
        delegate.onCreateView_beforeSuper(mock(LayoutInflater.class), null, null);
        delegate.onStart_afterSuper();

        // Then the presenter will be stored in the savior
        assertThat(mSavior.presenterCount()).isEqualTo(0);

        // And when the activity is finishing.
        hostingActivity.setFinishing(true);
        delegate.onStop_beforeSuper();
        delegate.onDestroyView_beforeSuper();
        delegate.onDestroy_afterSuper();

        // Then assert that the presenter is destroyed and not saved in the savior.
        assertThat(delegate.getPresenter().isDestroyed()).isTrue();
        assertThat(mSavior.presenterCount()).isEqualTo(0);
    }

    /**
     * Activity move to background -> move to foreground without retain, the savior should be ignored although enabled
     *
     * verified by:
     * - pascal
     */
    @Test
    public void saviorTrue_retainFalse_dontKeepActivitiesFalse_moveToBackground_moveToForeground() {

        final TiFragmentDelegateBuilder.HostingActivity
                hostingActivity = new TiFragmentDelegateBuilder.HostingActivity();

        // Given a Presenter that does use a static savior but does not retain itself.
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(true)
                .setRetainPresenterEnabled(false)
                .build());

        // And given a Fragment.
        final TiFragmentDelegate<TiPresenter<TiView>, TiView> delegate
                = new TiFragmentDelegateBuilder()
                .setDontKeepActivitiesEnabled(false)
                .setHostingActivity(hostingActivity)
                .setSavior(mSavior)
                .setPresenter(presenter)
                .build();

        // When the Fragment is added to the Activity.
        delegate.onCreate_afterSuper(null);
        delegate.onCreateView_beforeSuper(mock(LayoutInflater.class), null, null);
        delegate.onStart_afterSuper();

        // Then the Presenter will *not* be stored in the savior
        assertThat(mSavior.presenterCount()).isEqualTo(0);

        // And when the Activity is moved to background
        delegate.onSaveInstanceState_afterSuper(mSavedState);
        delegate.onStop_beforeSuper();

        // Then the Presenter is not destroyed
        assertThat(delegate.getPresenter().isDestroyed()).isFalse();

        // And when the Activity moves to foreground again
        delegate.onStart_afterSuper();

        // Then the Presenter is still alive and not saved in savior
        assertThat(delegate.getPresenter().isDestroyed()).isFalse();
        assertThat(mSavior.presenterCount()).isEqualTo(0);

    }

    /**
     * Activity changing configuration without retain, the savior should be ignored although enabled
     *
     * verified by:
     * - pascal
     */
    @Test
    public void saviorTrue_retainFalse_dontKeepActivitiesFalse_activityChangingConfiguration() {

        final TiFragmentDelegateBuilder.HostingActivity
                hostingActivity = new TiFragmentDelegateBuilder.HostingActivity();

        // Given a Presenter that does use a static savior but does not retain itself.
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(true)
                .setRetainPresenterEnabled(false)
                .build());

        // And given a Fragment.
        final TiFragmentDelegate<TiPresenter<TiView>, TiView> delegate
                = new TiFragmentDelegateBuilder()
                .setDontKeepActivitiesEnabled(false)
                .setHostingActivity(hostingActivity)
                .setSavior(mSavior)
                .setPresenter(presenter)
                .build();

        // When the Fragment is added to the Activity.
        delegate.onCreate_afterSuper(null);
        delegate.onCreateView_beforeSuper(mock(LayoutInflater.class), null, null);
        delegate.onStart_afterSuper();

        // Then the presenter will *not* be stored in the savior
        assertThat(mSavior.presenterCount()).isEqualTo(0);

        // And when the Activity is changing its configuration.
        hostingActivity.setChangingConfiguration(true);
        delegate.onSaveInstanceState_afterSuper(mSavedState);
        delegate.onStop_beforeSuper();
        delegate.onDestroyView_beforeSuper();
        delegate.onDestroy_afterSuper();

        // Then assert that the presenter is destroyed and not saved in the savior.
        assertThat(delegate.getPresenter().isDestroyed()).isTrue();
        assertThat(mSavior.presenterCount()).isEqualTo(0);

        // When the Activity is recreated.
        final TiFragmentDelegateBuilder.HostingActivity
                hostingActivity2 = new TiFragmentDelegateBuilder.HostingActivity();

        // And generates a new Fragment instance.
        final TestPresenter presenter2 = new TestPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(true)
                .setRetainPresenterEnabled(false)
                .build());
        final TiFragmentDelegate<TiPresenter<TiView>, TiView> delegate2
                = new TiFragmentDelegateBuilder()
                .setDontKeepActivitiesEnabled(false)
                .setHostingActivity(hostingActivity2)
                .setSavior(mSavior)
                .setPresenter(presenter2)
                .build();

        delegate2.onCreate_afterSuper(mSavedState);
        delegate2.onCreateView_beforeSuper(mock(LayoutInflater.class), null, mSavedState);
        delegate2.onStart_afterSuper();

        // Then a new Presenter instance will be generated and the old presenter isn't used
        assertThat(delegate2.getPresenter()).isNotEqualTo(presenter).isEqualTo(presenter2);
        assertThat(mSavior.presenterCount()).isEqualTo(0);
    }

    /**
     * Activity move to background -> move to foreground without retain, the savior should be ignored although enabled (don't keep Activities)
     *
     * verified by:
     * - pascal
     */
    @Test
    public void saviorTrue_retainFalse_dontKeepActivitiesTrue_moveToBackground_moveToForeground() {

        final TiFragmentDelegateBuilder.HostingActivity
                hostingActivity = new TiFragmentDelegateBuilder.HostingActivity();

        // Given a Presenter that does use a static savior but does not retain itself.
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(true)
                .setRetainPresenterEnabled(false)
                .build());

        // And given a Fragment.
        final TiFragmentDelegate<TiPresenter<TiView>, TiView> delegate
                = new TiFragmentDelegateBuilder()
                .setDontKeepActivitiesEnabled(true)
                .setHostingActivity(hostingActivity)
                .setSavior(mSavior)
                .setPresenter(presenter)
                .build();

        // When the Fragment is added to the Activity.
        delegate.onCreate_afterSuper(null);
        delegate.onCreateView_beforeSuper(mock(LayoutInflater.class), null, null);
        delegate.onStart_afterSuper();

        // Then the Presenter will *not* be stored in the savior
        assertThat(mSavior.presenterCount()).isEqualTo(0);

        // When the Activity is moved to background
        delegate.onSaveInstanceState_afterSuper(mSavedState);
        delegate.onStop_beforeSuper();
        delegate.onDestroyView_beforeSuper();
        delegate.onDestroy_afterSuper();

        // Then the Presenter gets destroyed.
        assertThat(delegate.getPresenter().isDestroyed()).isTrue();
        assertThat(mSavior.presenterCount()).isEqualTo(0);

        // When the Activity is recreated.
        final TiFragmentDelegateBuilder.HostingActivity
                hostingActivity2 = new TiFragmentDelegateBuilder.HostingActivity();

        // And generates a new Fragment instance.
        final TestPresenter presenter2 = new TestPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(true)
                .setRetainPresenterEnabled(false)
                .build());
        final TiFragmentDelegate<TiPresenter<TiView>, TiView> delegate2
                = new TiFragmentDelegateBuilder()
                .setDontKeepActivitiesEnabled(true)
                .setHostingActivity(hostingActivity2)
                .setSavior(mSavior)
                .setPresenter(presenter2)
                .build();

        delegate2.onCreate_afterSuper(mSavedState);
        delegate2.onCreateView_beforeSuper(mock(LayoutInflater.class), null, mSavedState);
        delegate2.onStart_afterSuper();

        // Then the new Presenter does not equals the previous Presenter.
        assertThat(delegate2.getPresenter()).isNotEqualTo(presenter).isEqualTo(presenter2);
        assertThat(mSavior.presenterCount()).isEqualTo(0);
    }

    /**
     * Activity changing configuration without retain, the savior should be ignored although enabled  (don't keep Activities)
     *
     * verified by:
     * - pascal
     */
    @Test
    public void saviorTrue_retainFalse_dontKeepActivitiesTrue_activityChangingConfiguration() {

        final TiFragmentDelegateBuilder.HostingActivity
                hostingActivity = new TiFragmentDelegateBuilder.HostingActivity();

        // Given a Presenter that does use a static savior but does not retain itself.
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(true)
                .setRetainPresenterEnabled(false)
                .build());

        // And given a Fragment.
        final TiFragmentDelegate<TiPresenter<TiView>, TiView> delegate
                = new TiFragmentDelegateBuilder()
                .setDontKeepActivitiesEnabled(true)
                .setHostingActivity(hostingActivity)
                .setSavior(mSavior)
                .setPresenter(presenter)
                .build();

        // When the Fragment is added to the Activity.
        delegate.onCreate_afterSuper(null);
        delegate.onCreateView_beforeSuper(mock(LayoutInflater.class), null, null);
        delegate.onStart_afterSuper();

        // Then the presenter will *not* be stored in the savior
        assertThat(mSavior.presenterCount()).isEqualTo(0);

        // And when the Activity is changing its configuration.
        hostingActivity.setChangingConfiguration(true);
        delegate.onSaveInstanceState_afterSuper(mSavedState);
        delegate.onStop_beforeSuper();
        delegate.onDestroyView_beforeSuper();
        delegate.onDestroy_afterSuper();

        // Then assert that the presenter is destroyed and not saved in the savior.
        assertThat(delegate.getPresenter().isDestroyed()).isTrue();
        assertThat(mSavior.presenterCount()).isEqualTo(0);

        // When the Activity is recreated.
        final TiFragmentDelegateBuilder.HostingActivity
                hostingActivity2 = new TiFragmentDelegateBuilder.HostingActivity();

        // And generates a new Fragment instance.
        final TestPresenter presenter2 = new TestPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(true)
                .setRetainPresenterEnabled(false)
                .build());
        final TiFragmentDelegate<TiPresenter<TiView>, TiView> delegate2
                = new TiFragmentDelegateBuilder()
                .setDontKeepActivitiesEnabled(true)
                .setHostingActivity(hostingActivity2)
                .setSavior(mSavior)
                .setPresenter(presenter2)
                .build();

        delegate2.onCreate_afterSuper(mSavedState);
        delegate2.onCreateView_beforeSuper(mock(LayoutInflater.class), null, mSavedState);
        delegate2.onStart_afterSuper();

        // Then assert that the new Presenter does not equals the previous presenter.
        assertThat(delegate2.getPresenter()).isNotEqualTo(presenter).isEqualTo(presenter2);
        assertThat(mSavior.presenterCount()).isEqualTo(0);
    }

    /**
     * Activity finishing without retain, the savior should be ignored although enabled
     *
     * verified by:
     * - pascal
     */
    @Test
    public void saviorTrue_retainFalse_dontKeepActivitiesFalse_activityFinishing() {

        final TiFragmentDelegateBuilder.HostingActivity
                hostingActivity = new TiFragmentDelegateBuilder.HostingActivity();

        // Given a Presenter that does use a static savior but does not retain itself.
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(true)
                .setRetainPresenterEnabled(false)
                .build());

        // And given a Fragment.
        final TiFragmentDelegate<TiPresenter<TiView>, TiView> delegate
                = new TiFragmentDelegateBuilder()
                .setDontKeepActivitiesEnabled(false)
                .setHostingActivity(hostingActivity)
                .setSavior(mSavior)
                .setPresenter(presenter)
                .build();

        // When the Fragment is added to the Activity.
        delegate.onCreate_afterSuper(null);
        delegate.onCreateView_beforeSuper(mock(LayoutInflater.class), null, null);
        delegate.onStart_afterSuper();

        // Then the presenter will *not* be stored in the savior
        assertThat(mSavior.presenterCount()).isEqualTo(0);

        // And when the Activity is finishing.
        hostingActivity.setFinishing(true);
        delegate.onStop_beforeSuper();
        delegate.onDestroyView_beforeSuper();
        delegate.onDestroy_afterSuper();

        // Then assert that the Presenter is destroyed and not saved in the savior.
        assertThat(delegate.getPresenter().isDestroyed()).isTrue();
        assertThat(mSavior.presenterCount()).isEqualTo(0);
    }

    /**
     * Activity finishing without retain, the savior should be ignored although enabled (don't keep Activities)
     *
     * verified by:
     * - pascal
     */
    @Test
    public void saviorTrue_retainFalse_dontKeepActivitiesTrue_activityFinishing() {

        final TiFragmentDelegateBuilder.HostingActivity
                hostingActivity = new TiFragmentDelegateBuilder.HostingActivity();

        // Given a Presenter that does use a static savior but does not retain itself.
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(true)
                .setRetainPresenterEnabled(false)
                .build());

        // And given a Fragment.
        final TiFragmentDelegate<TiPresenter<TiView>, TiView> delegate
                = new TiFragmentDelegateBuilder()
                .setDontKeepActivitiesEnabled(true)
                .setHostingActivity(hostingActivity)
                .setSavior(mSavior)
                .setPresenter(presenter)
                .build();

        // When the Fragment is added to the Activity.
        delegate.onCreate_afterSuper(null);
        delegate.onCreateView_beforeSuper(mock(LayoutInflater.class), null, null);
        delegate.onStart_afterSuper();

        // Then the presenter will *not* be stored in the savior
        assertThat(mSavior.presenterCount()).isEqualTo(0);

        // And when the Activity is finishing.
        hostingActivity.setFinishing(true);
        delegate.onStop_beforeSuper();
        delegate.onDestroyView_beforeSuper();
        delegate.onDestroy_afterSuper();

        // Then assert that the Presenter is destroyed and not saved in the savior.
        assertThat(delegate.getPresenter().isDestroyed()).isTrue();
        assertThat(mSavior.presenterCount()).isEqualTo(0);
    }

    /**
     * Activity changing configuration Default config
     *
     * verified by:
     * - pascal
     */
    @Test
    public void saviorTrue_retainTrue_dontKeepActivitiesFalse_activityChangingConfiguration() {

        final TiFragmentDelegateBuilder.HostingActivity
                hostingActivity = new TiFragmentDelegateBuilder.HostingActivity();

        // Check that the default config matches this test case
        final TiConfiguration config = new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(true)
                .setRetainPresenterEnabled(true)
                .build();
        assertThat(TiConfiguration.DEFAULT).isEqualTo(config);

        // Given a Presenter that uses a static savior to retain itself (default config).
        final TestPresenter presenter = new TestPresenter(config);

        // And given a Fragment.
        final TiFragmentDelegate<TiPresenter<TiView>, TiView> delegate
                = new TiFragmentDelegateBuilder()
                .setDontKeepActivitiesEnabled(false)
                .setHostingActivity(hostingActivity)
                .setSavior(mSavior)
                .setPresenter(presenter)
                .build();

        // When the Fragment is added to the Activity.
        delegate.onCreate_afterSuper(null);
        delegate.onCreateView_beforeSuper(mock(LayoutInflater.class), null, null);
        delegate.onStart_afterSuper();

        // Then the presenter will be stored in the savior
        assertThat(mSavior.presenterCount()).isEqualTo(1);

        // And when the Activity is changing its configurations.
        hostingActivity.setChangingConfiguration(true);
        delegate.onSaveInstanceState_afterSuper(mSavedState);
        delegate.onStop_beforeSuper();
        delegate.onDestroyView_beforeSuper();

        // Then the presenter will be retained and saved in the savior.
        assertThat(delegate.getPresenter().isDestroyed()).isFalse();
        assertThat(mSavior.presenterCount()).isEqualTo(1);

        // When new Activity instance gets created by the Android Framework.
        hostingActivity.recreateInstance();

        // And the Fragment gets automatically retained.
        delegate.onCreateView_beforeSuper(mock(LayoutInflater.class), null, mSavedState);
        delegate.onStart_afterSuper();

        // Then the Presenter is the same
        assertThat(delegate.getPresenter()).isEqualTo(presenter);
        assertThat(delegate.getPresenter().isDestroyed()).isEqualTo(false);
        assertThat(mSavior.presenterCount()).isEqualTo(1);
    }

    /**
     * Activity move to background -> move to foreground Default config (don't keep Activities)
     *
     * verified by:
     * - pascal
     */
    @Test
    public void saviorTrue_retainTrue_dontKeepActivitiesTrue_moveToBackground_moveToForeground() {

        final TiFragmentDelegateBuilder.HostingActivity
                hostingActivity = new TiFragmentDelegateBuilder.HostingActivity();

        // Check that the default config matches this test case
        final TiConfiguration config = new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(true)
                .setRetainPresenterEnabled(true)
                .build();
        assertThat(TiConfiguration.DEFAULT).isEqualTo(config);

        // Given a Presenter that uses a static savior to retain itself (default config).
        final TestPresenter presenter = new TestPresenter(config);

        // And given a Fragment.
        final TiFragmentDelegate<TiPresenter<TiView>, TiView> delegate
                = new TiFragmentDelegateBuilder()
                .setDontKeepActivitiesEnabled(true)
                .setHostingActivity(hostingActivity)
                .setSavior(mSavior)
                .setPresenter(presenter)
                .build();

        // When the Fragment is added to the Activity.
        delegate.onCreate_afterSuper(null);
        delegate.onCreateView_beforeSuper(mock(LayoutInflater.class), null, null);
        delegate.onStart_afterSuper();

        // Then the presenter will be stored in the savior
        assertThat(mSavior.presenterCount()).isEqualTo(1);

        // When the Activity gets moved to background
        delegate.onSaveInstanceState_afterSuper(mSavedState);
        delegate.onStop_beforeSuper();
        delegate.onDestroyView_beforeSuper();
        delegate.onDestroy_afterSuper();

        // Then the presenter stays alive and is saved in the savior.
        assertThat(delegate.getPresenter().isDestroyed()).isFalse();
        assertThat(mSavior.presenterCount()).isEqualTo(1);

        // When the Activity moves to foreground again
        // A new Activity gets created by the Android Framework.
        final TiFragmentDelegateBuilder.HostingActivity
                hostingActivity2 = new TiFragmentDelegateBuilder.HostingActivity();

        // And generates a new Fragment instance.
        final TestPresenter presenter2 = new TestPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(true)
                .setRetainPresenterEnabled(true)
                .build());
        final TiFragmentDelegate<TiPresenter<TiView>, TiView> delegate2
                = new TiFragmentDelegateBuilder()
                .setDontKeepActivitiesEnabled(true)
                .setHostingActivity(hostingActivity2)
                .setSavior(mSavior)
                .setPresenter(presenter2)
                .build();

        delegate2.onCreate_afterSuper(mSavedState);
        delegate2.onCreateView_beforeSuper(mock(LayoutInflater.class), null, null);
        delegate2.onStart_afterSuper();

        // Then the Presenter is the same as in the previous fragment instance
        assertThat(delegate2.getPresenter()).isNotEqualTo(presenter2).isEqualTo(presenter);
        assertThat(delegate2.getPresenter().isDestroyed()).isEqualTo(false);
        assertThat(mSavior.presenterCount()).isEqualTo(1);
    }

    /**
     * Activity move to background -> move to foreground Default config
     *
     * verified by:
     * - pascal
     */
    @Test
    public void saviorTrue_retainTrue_dontKeepActivitiesFalse_moveToBackground_moveToForeground() {

        final TiFragmentDelegateBuilder.HostingActivity
                hostingActivity = new TiFragmentDelegateBuilder.HostingActivity();

        // Check that the default config matches this test case
        final TiConfiguration config = new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(true)
                .setRetainPresenterEnabled(true)
                .build();
        assertThat(TiConfiguration.DEFAULT).isEqualTo(config);

        // Given a Presenter that uses a static savior to retain itself (default config).
        final TestPresenter presenter = new TestPresenter(config);

        // And given a Fragment.
        final TiFragmentDelegate<TiPresenter<TiView>, TiView> delegate
                = new TiFragmentDelegateBuilder()
                .setDontKeepActivitiesEnabled(false)
                .setHostingActivity(hostingActivity)
                .setSavior(mSavior)
                .setPresenter(presenter)
                .build();

        // When the Fragment is added to the Activity.
        delegate.onCreate_afterSuper(null);
        delegate.onCreateView_beforeSuper(mock(LayoutInflater.class), null, null);
        delegate.onStart_afterSuper();

        // Then the presenter will be stored in the savior
        assertThat(mSavior.presenterCount()).isEqualTo(1);

        // When the Activity gets moved to background
        delegate.onSaveInstanceState_afterSuper(mSavedState);
        delegate.onStop_beforeSuper();

        // Then the presenter stays alive and is saved in the savior.
        assertThat(delegate.getPresenter().isDestroyed()).isFalse();
        assertThat(mSavior.presenterCount()).isEqualTo(1);

        // When the Activity moves to foreground again
        hostingActivity.resetToDefault();

        // And the Fragment gets automatically retained.
        delegate.onStart_afterSuper();

        // Then the Presenter is the same
        assertThat(delegate.getPresenter()).isEqualTo(presenter);
        assertThat(delegate.getPresenter().isDestroyed()).isEqualTo(false);
        assertThat(mSavior.presenterCount()).isEqualTo(1);
    }

    /**
     * Activity changing configuration Default config (don't keep Activities)
     *
     * verified by:
     * - pascal
     */
    @Test
    public void saviorTrue_retainTrue_dontKeepActivitiesTrue_activityChangingConfiguration() {

        final TiFragmentDelegateBuilder.HostingActivity
                hostingActivity = new TiFragmentDelegateBuilder.HostingActivity();

        // Given a Presenter that uses a static savior to retain itself.
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(true)
                .setRetainPresenterEnabled(true)
                .build());

        // And given a Fragment.
        final TiFragmentDelegate<TiPresenter<TiView>, TiView> delegate
                = new TiFragmentDelegateBuilder()
                .setDontKeepActivitiesEnabled(true)
                .setHostingActivity(hostingActivity)
                .setSavior(mSavior)
                .setPresenter(presenter)
                .build();

        // When the fragment is added to the activity.
        delegate.onCreate_afterSuper(null);
        delegate.onCreateView_beforeSuper(mock(LayoutInflater.class), null, null);
        delegate.onStart_afterSuper();

        // Then the presenter will be stored in the savior
        assertThat(mSavior.presenterCount()).isEqualTo(1);

        // And when the Activity is changing its configuration.
        hostingActivity.setChangingConfiguration(true);
        delegate.onSaveInstanceState_afterSuper(mSavedState);
        delegate.onStop_beforeSuper();
        delegate.onDestroyView_beforeSuper();

        // Then the presenter will be retained and saved in the savior.
        assertThat(delegate.getPresenter().isDestroyed()).isFalse();
        assertThat(mSavior.presenterCount()).isEqualTo(1);

        // When new Activity instance gets created by the Android Framework.
        hostingActivity.recreateInstance();

        // And when the Fragment is retained.
        delegate.onCreateView_beforeSuper(mock(LayoutInflater.class), null, mSavedState);
        delegate.onStart_afterSuper();

        // Then assert that the Presenter is not destroyed and saved in the savior.
        assertThat(delegate.getPresenter().isDestroyed()).isFalse();
        assertThat(delegate.getPresenter()).isEqualTo(presenter);
        assertThat(mSavior.presenterCount()).isEqualTo(1);
    }

    /**
     * Activity finish Default config
     *
     * verified by:
     * - pascal
     */
    @Test
    public void saviorTrue_retainTrue_dontKeepActivitiesFalse_activityFinishing() {

        final TiFragmentDelegateBuilder.HostingActivity
                hostingActivity = new TiFragmentDelegateBuilder.HostingActivity();

        // Check that the default config matches this test case
        final TiConfiguration config = new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(true)
                .setRetainPresenterEnabled(true)
                .build();
        assertThat(TiConfiguration.DEFAULT).isEqualTo(config);

        // Given a Presenter that uses a static savior to retain itself (default config).
        final TestPresenter presenter = new TestPresenter(config);

        // And given a Fragment.
        final TiFragmentDelegate<TiPresenter<TiView>, TiView> delegate
                = new TiFragmentDelegateBuilder()
                .setDontKeepActivitiesEnabled(false)
                .setHostingActivity(hostingActivity)
                .setSavior(mSavior)
                .setPresenter(presenter)
                .build();

        // When the fragment is added to the activity.
        delegate.onCreate_afterSuper(null);
        delegate.onCreateView_beforeSuper(mock(LayoutInflater.class), null, null);
        delegate.onStart_afterSuper();

        // Then the presenter will be stored in the savior
        assertThat(mSavior.presenterCount()).isEqualTo(1);

        // When the activity is finishing.
        hostingActivity.setFinishing(true);
        delegate.onStop_beforeSuper();
        delegate.onDestroyView_beforeSuper();
        delegate.onDestroy_afterSuper();

        // Then the presenter is destroyed and not saved in the savior.
        assertThat(delegate.getPresenter().isDestroyed()).isTrue();
        assertThat(mSavior.presenterCount()).isEqualTo(0);
    }

    /**
     * Activity finish Default config  (don't keep Activities)
     *
     * verified by:
     * - pascal
     */
    @Test
    public void saviorTrue_retainTrue_dontKeepActivitiesTrue_activityFinishing() {

        final TiFragmentDelegateBuilder.HostingActivity
                hostingActivity = new TiFragmentDelegateBuilder.HostingActivity();

        // Given a Presenter that uses a static savior to retain itself.
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(true)
                .setRetainPresenterEnabled(true)
                .build());

        // And given a Fragment.
        final TiFragmentDelegate<TiPresenter<TiView>, TiView> delegate
                = new TiFragmentDelegateBuilder()
                .setDontKeepActivitiesEnabled(true)
                .setHostingActivity(hostingActivity)
                .setSavior(mSavior)
                .setPresenter(presenter)
                .build();

        // When the fragment is added to the activity.
        delegate.onCreate_afterSuper(null);
        delegate.onCreateView_beforeSuper(mock(LayoutInflater.class), null, null);
        delegate.onStart_afterSuper();

        // Then the presenter will be stored in the savior
        assertThat(mSavior.presenterCount()).isEqualTo(1);

        // And when the Activity is finishing.
        hostingActivity.setFinishing(true);
        delegate.onStop_beforeSuper();
        delegate.onDestroyView_beforeSuper();
        delegate.onDestroy_afterSuper();

        // Then assert that the presenter is destroyed and not saved in the savior.
        assertThat(delegate.getPresenter().isDestroyed()).isTrue();
        assertThat(mSavior.presenterCount()).isEqualTo(0);
    }
}
