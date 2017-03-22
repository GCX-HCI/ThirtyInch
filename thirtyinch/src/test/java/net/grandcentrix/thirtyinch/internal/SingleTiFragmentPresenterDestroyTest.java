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

        // Then assert that the new Presenter does not equal the previous presenter.
        assertThat(delegate2.getPresenter()).isNotEqualTo(presenter).isEqualTo(presenter2);
    }

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

        // Then assert that the new Presenter does not equals the previous presenter.
        assertThat(delegate2.getPresenter()).isNotEqualTo(presenter).isEqualTo(presenter2);
    }

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

        // And when the activity is finishing.
        hostingActivity.setFinishing(true);
        delegate.onStop_beforeSuper();
        delegate.onDestroyView_beforeSuper();
        delegate.onDestroy_afterSuper();

        // Then assert that the Presenter is destroyed and not saved in the savior.
        assertThat(delegate.getPresenter().isDestroyed()).isTrue();
        assertThat(mSavior.presenterCount()).isEqualTo(0);
    }

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

        // And when the Activity is finishing.
        hostingActivity.setFinishing(true);
        delegate.onStop_beforeSuper();
        delegate.onDestroyView_beforeSuper();
        delegate.onDestroy_afterSuper();

        // Then assert that the Presenter is destroyed and not saved in the savior.
        assertThat(delegate.getPresenter().isDestroyed()).isTrue();
        assertThat(mSavior.presenterCount()).isEqualTo(0);
    }

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

        // And when the Activity is changing its configuration.
        hostingActivity.setChangingConfiguration(true);
        delegate.onSaveInstanceState_afterSuper(mSavedState);
        delegate.onStop_beforeSuper();
        delegate.onDestroyView_beforeSuper();

        // And the Fragment is retained.
        delegate.onCreateView_beforeSuper(mock(LayoutInflater.class), null, mSavedState);
        delegate.onStart_afterSuper();

        // Then assert that the Presenter is not destroyed and not saved in the savior.
        assertThat(delegate.getPresenter().isDestroyed()).isFalse();
        assertThat(delegate.getPresenter()).isEqualTo(presenter);
        assertThat(mSavior.presenterCount()).isEqualTo(0);
    }

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

        // And when the Activity is changing its configuration.
        hostingActivity.setChangingConfiguration(true);
        delegate.onSaveInstanceState_afterSuper(mSavedState);
        delegate.onStop_beforeSuper();
        delegate.onDestroyView_beforeSuper();

        // And the Fragment is retained.
        delegate.onCreateView_beforeSuper(mock(LayoutInflater.class), null, mSavedState);
        delegate.onStart_afterSuper();

        // Then assert that the Presenter is not destroyed and not saved in the savior.
        assertThat(delegate.getPresenter().isDestroyed()).isFalse();
        assertThat(delegate.getPresenter()).isEqualTo(presenter);
        assertThat(mSavior.presenterCount()).isEqualTo(0);
    }

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

        // And when the Activity is finishing.
        hostingActivity.setFinishing(true);
        delegate.onStop_beforeSuper();
        delegate.onDestroyView_beforeSuper();
        delegate.onDestroy_afterSuper();

        // Then assert that the Presenter is destroyed and not saved in the savior.
        assertThat(delegate.getPresenter().isDestroyed()).isTrue();
        assertThat(mSavior.presenterCount()).isEqualTo(0);
    }

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

        // And when the activity is finishing.
        hostingActivity.setFinishing(true);
        delegate.onStop_beforeSuper();
        delegate.onDestroyView_beforeSuper();
        delegate.onDestroy_afterSuper();

        // Then assert that the presenter is destroyed and not saved in the savior.
        assertThat(delegate.getPresenter().isDestroyed()).isTrue();
        assertThat(mSavior.presenterCount()).isEqualTo(0);
    }

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

        // Then assert that the new Presenter does not equals the previous presenter.
        assertThat(delegate2.getPresenter()).isNotEqualTo(presenter).isEqualTo(presenter2);
        assertThat(mSavior.presenterCount()).isEqualTo(0);
    }

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

        // And when the Activity is changing its configuration.
        hostingActivity.setChangingConfiguration(true);
        delegate.onSaveInstanceState_afterSuper(mSavedState);
        delegate.onStop_beforeSuper();
        delegate.onDestroyView_beforeSuper();
        delegate.onDestroy_afterSuper();

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

        // And when the Activity is finishing.
        hostingActivity.setFinishing(true);
        delegate.onStop_beforeSuper();
        delegate.onDestroyView_beforeSuper();
        delegate.onDestroy_afterSuper();

        // Then assert that the Presenter is destroyed and not saved in the savior.
        assertThat(delegate.getPresenter().isDestroyed()).isTrue();
        assertThat(mSavior.presenterCount()).isEqualTo(0);
    }

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

        // And when the Activity is finishing.
        hostingActivity.setFinishing(true);
        delegate.onStop_beforeSuper();
        delegate.onDestroyView_beforeSuper();
        delegate.onDestroy_afterSuper();

        // Then assert that the Presenter is destroyed and not saved in the savior.
        assertThat(delegate.getPresenter().isDestroyed()).isTrue();
        assertThat(mSavior.presenterCount()).isEqualTo(0);
    }

    @Test
    public void saviorTrue_retainTrue_dontKeepActivitiesFalse_activityChangingConfiguration() {

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
                .setDontKeepActivitiesEnabled(false)
                .setHostingActivity(hostingActivity)
                .setSavior(mSavior)
                .setPresenter(presenter)
                .build();

        // When the Fragment is added to the Activity.
        delegate.onCreate_afterSuper(null);
        delegate.onCreateView_beforeSuper(mock(LayoutInflater.class), null, null);
        delegate.onStart_afterSuper();

        // And when the Activity is changing its configurations.
        hostingActivity.setChangingConfiguration(true);
        delegate.onSaveInstanceState_afterSuper(mSavedState);
        delegate.onStop_beforeSuper();
        delegate.onDestroyView_beforeSuper();

        // And when the Fragment is retained.
        delegate.onCreateView_beforeSuper(mock(LayoutInflater.class), null, mSavedState);
        delegate.onStart_afterSuper();

        // Then assert that the Presenter is not destroyed and saved in the savior.
        assertThat(delegate.getPresenter().isDestroyed()).isFalse();
        assertThat(delegate.getPresenter()).isEqualTo(presenter);
        assertThat(mSavior.presenterCount()).isEqualTo(1);
    }

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

        // And when the Activity is changing its configuration.
        hostingActivity.setChangingConfiguration(true);
        delegate.onSaveInstanceState_afterSuper(mSavedState);
        delegate.onStop_beforeSuper();
        delegate.onDestroyView_beforeSuper();

        // And when the Fragment is retained.
        delegate.onCreateView_beforeSuper(mock(LayoutInflater.class), null, mSavedState);
        delegate.onStart_afterSuper();

        // Then assert that the Presenter is not destroyed and saved in the savior.
        assertThat(delegate.getPresenter().isDestroyed()).isFalse();
        assertThat(delegate.getPresenter()).isEqualTo(presenter);
        assertThat(mSavior.presenterCount()).isEqualTo(1);
    }

    @Test
    public void saviorTrue_retainTrue_dontKeepActivitiesFalse_activityFinishing() {

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
                .setDontKeepActivitiesEnabled(false)
                .setHostingActivity(hostingActivity)
                .setSavior(mSavior)
                .setPresenter(presenter)
                .build();

        // When the fragment is added to the activity.
        delegate.onCreate_afterSuper(null);
        delegate.onCreateView_beforeSuper(mock(LayoutInflater.class), null, null);
        delegate.onStart_afterSuper();

        // And when the activity is finishing.
        hostingActivity.setFinishing(true);
        delegate.onStop_beforeSuper();
        delegate.onDestroyView_beforeSuper();
        delegate.onDestroy_afterSuper();

        // Then assert that the presenter is destroyed and not saved in the savior.
        assertThat(delegate.getPresenter().isDestroyed()).isTrue();
        assertThat(mSavior.presenterCount()).isEqualTo(0);
    }

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
