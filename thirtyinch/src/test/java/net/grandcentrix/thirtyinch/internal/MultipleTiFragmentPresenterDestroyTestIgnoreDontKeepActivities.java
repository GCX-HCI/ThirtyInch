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

import org.junit.Test;

import android.view.LayoutInflater;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Same test cases as {@link MultipleTiFragmentPresenterDestroyTest} but with the "don't keep
 * Activities option enabled". This means that the lifecycle may be different but the final
 * assertions must be identical
 */
public class MultipleTiFragmentPresenterDestroyTestIgnoreDontKeepActivities extends TiFragmentPresenterDestroyTest {
    

    @Test
    public void saviorTrue_retainFalse_backstackFalse_activityChangingConfiguration() {

        final HostingActivity hostingActivity = new HostingActivity();

        // Given a Presenter that does use a static savior but does not retain itself.
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(true)
                .setRetainPresenterEnabled(false)
                .build());

        // And given a Fragment.
        final TestTiFragment delegate = new TestTiFragment.Builder()

                .setDontKeepActivitiesEnabled(true)
                .setHostingActivity(hostingActivity)
                .setSavior(mSavior)
                .setPresenter(presenter)
                .build();

        // When the Fragment is added to the Activity.
        delegate.onCreate(null);
        delegate.onCreateView(mock(LayoutInflater.class), null, null);
        delegate.onStart();

        // And when the Fragment is replaced by another Fragment.
        delegate.onStop();
        delegate.onDestroyView();
        delegate.onDestroy();

        // Then assert that the Presenter is destroyed and not saved in the savior
        // because there is no way to bring the Fragment back.
        assertThat(delegate.getPresenter().isDestroyed()).isTrue();
        assertThat(mSavior.presenterCount()).isEqualTo(0);
    }

    @Test
    public void saviorTrue_retainFalse_backstackFalse_activityFinishing() {

        final HostingActivity hostingActivity = new HostingActivity();

        // Given a Presenter that does use a static savior but does not retain itself.
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(true)
                .setRetainPresenterEnabled(false)
                .build());

        // And given a Fragment.
        final TestTiFragment delegate = new TestTiFragment.Builder()

                .setDontKeepActivitiesEnabled(true)
                .setHostingActivity(hostingActivity)
                .setSavior(mSavior)
                .setPresenter(presenter)
                .build();

        // When the Fragment is added to the Activity.
        delegate.onCreate(null);
        delegate.onCreateView(mock(LayoutInflater.class), null, null);
        delegate.onStart();

        // And when the Fragment is replaced by another Fragment.
        delegate.onStop();
        delegate.onDestroyView();
        delegate.onDestroy();

        // Then assert that the Presenter is destroyed and not saved in the savior
        // because there is no way to bring the Fragment back.
        assertThat(delegate.getPresenter().isDestroyed()).isTrue();
        assertThat(mSavior.presenterCount()).isEqualTo(0);
    }


    @Test
    public void saviorTrue_retainFalse_backstackTrue_activityChangingConfiguration() {

        final HostingActivity hostingActivity = new HostingActivity();

        // Given a Presenter that does use a static savior but does not retain itself.
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(true)
                .setRetainPresenterEnabled(false)
                .build());

        // And given a Fragment.
        final TestTiFragment delegate = new TestTiFragment.Builder()

                .setDontKeepActivitiesEnabled(true)
                .setHostingActivity(hostingActivity)
                .setSavior(mSavior)
                .setPresenter(presenter)
                .build();

        // When the Fragment is added to the Activity.
        delegate.onCreate(null);
        delegate.onCreateView(mock(LayoutInflater.class), null, null);
        delegate.onStart();

        // And when the Fragment is replaced by another Fragment.
        delegate.onStop();
        delegate.onDestroyView();

        // And when the Activity is changing its configuration.
        hostingActivity.setChangingConfiguration(true);
        delegate.onSaveInstanceState(mSavedState);
        delegate.onDestroy();

        // Then assert that the Presenter is destroyed and not saved in the savior.
        assertThat(delegate.getPresenter().isDestroyed()).isTrue();
        assertThat(mSavior.presenterCount()).isEqualTo(0);
    }

    @Test
    public void saviorTrue_retainFalse_backstackTrue_activityFinishing() {

        final HostingActivity hostingActivity = new HostingActivity();

        // Given a Presenter that does use a static savior but does not retain itself.
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(true)
                .setRetainPresenterEnabled(false)
                .build());

        // And given a Fragment.
        final TestTiFragment delegate = new TestTiFragment.Builder()

                .setDontKeepActivitiesEnabled(true)
                .setHostingActivity(hostingActivity)
                .setSavior(mSavior)
                .setPresenter(presenter)
                .build();

        // When the Fragment is added to the Activity.
        delegate.onCreate(null);
        delegate.onCreateView(mock(LayoutInflater.class), null, null);
        delegate.onStart();

        // And when the Fragment is replaced by another Fragment.
        delegate.onStop();
        delegate.onDestroyView();

        // And when the Activity is finishing.
        hostingActivity.setFinishing(true);
        delegate.onDestroy();

        // Then assert that the Presenter is destroyed and not saved in the savior.
        assertThat(delegate.getPresenter().isDestroyed()).isTrue();
        assertThat(mSavior.presenterCount()).isEqualTo(0);
    }

    @Test
    public void saviorTrue_retainFalse_backstackTrue_popBackstack() {

        final HostingActivity hostingActivity = new HostingActivity();

        // Given a Presenter that does use a static savior but does not retain itself.
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(true)
                .setRetainPresenterEnabled(false)
                .build());

        // And given a Fragment.
        final TestTiFragment delegate = new TestTiFragment.Builder()

                .setDontKeepActivitiesEnabled(true)
                .setHostingActivity(hostingActivity)
                .setSavior(mSavior)
                .setPresenter(presenter)
                .build();

        // When the Fragment is added to the Activity.
        delegate.onCreate(null);
        delegate.onCreateView(mock(LayoutInflater.class), null, null);
        delegate.onStart();

        // And when the Fragment is replaced by another Fragment.
        delegate.onStop();
        delegate.onDestroyView();

        // And when the back stack is popped.
        delegate.onCreateView(mock(LayoutInflater.class), null, null);
        delegate.onStart();

        // Then assert that the Presenter is not destroyed and not saved in the savior.
        assertThat(delegate.getPresenter().isDestroyed()).isFalse();
        assertThat(delegate.getPresenter()).isEqualTo(presenter);
        assertThat(mSavior.presenterCount()).isEqualTo(0);
    }


    @Test
    public void saviorTrue_retainTrue_backstackFalse_activityChangingConfiguration() {

        final HostingActivity hostingActivity = new HostingActivity();

        // Given a Presenter that does use a static savior and does retain itself.
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(true)
                .setRetainPresenterEnabled(true)
                .build());

        // And given a Fragment.
        final TestTiFragment delegate = new TestTiFragment.Builder()

                .setDontKeepActivitiesEnabled(true)
                .setHostingActivity(hostingActivity)
                .setSavior(mSavior)
                .setPresenter(presenter)
                .build();

        // When the Fragment is added to the Activity.
        delegate.onCreate(null);
        delegate.onCreateView(mock(LayoutInflater.class), null, null);
        delegate.onStart();

        // And when the Fragment is replaced by another Fragment.
        delegate.onStop();
        delegate.onDestroyView();
        delegate.onDestroy();

        // Then assert that the Presenter is destroyed and not saved in the savior
        // because there is no way the Fragment can be brought back.
        assertThat(delegate.getPresenter().isDestroyed()).isTrue();
        assertThat(mSavior.presenterCount()).isEqualTo(0);
    }

    @Test
    public void saviorTrue_retainTrue_backstackFalse_activityFinishing() {

        final HostingActivity hostingActivity = new HostingActivity();

        // Given a Presenter that does use a static savior and does retain itself.
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(true)
                .setRetainPresenterEnabled(true)
                .build());

        // And given a Fragment.
        final TestTiFragment delegate = new TestTiFragment.Builder()

                .setDontKeepActivitiesEnabled(true)
                .setHostingActivity(hostingActivity)
                .setSavior(mSavior)
                .setPresenter(presenter)
                .build();

        // When the Fragment is added to the Activity.
        delegate.onCreate(null);
        delegate.onCreateView(mock(LayoutInflater.class), null, null);
        delegate.onStart();

        // And when the Fragment is replaced by another Fragment.
        delegate.onStop();
        delegate.onDestroyView();
        delegate.onDestroy();

        // Then assert that the Presenter is destroyed and not saved in the savior
        // because there is no way the Fragment can be brought back.
        assertThat(delegate.getPresenter().isDestroyed()).isTrue();
        assertThat(mSavior.presenterCount()).isEqualTo(0);
    }

    @Test
    public void saviorTrue_retainTrue_backstackTrue_activityChangingConfiguration() {

        final HostingActivity hostingActivity = new HostingActivity();

        // Given a Presenter that uses a static savior to retain itself.
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(true)
                .setRetainPresenterEnabled(true)
                .build());

        // And given a Fragment.
        final TestTiFragment delegate = new TestTiFragment.Builder()

                .setDontKeepActivitiesEnabled(true)
                .setHostingActivity(hostingActivity)
                .setSavior(mSavior)
                .setPresenter(presenter)
                .build();

        // When the Fragment is added to the Activity.
        delegate.onCreate(null);
        delegate.onCreateView(mock(LayoutInflater.class), null, null);
        delegate.onStart();

        // And when the Fragment is replaced by another Fragment.
        delegate.onStop();
        delegate.onDestroyView();

        // And when the Activity is changing its configuration.
        hostingActivity.setChangingConfiguration(true);
        delegate.onSaveInstanceState(mSavedState);

        // Then assert that the Presenter is not destroyed and saved in the savior.
        assertThat(delegate.getPresenter().isDestroyed()).isFalse();
        assertThat(mSavior.presenterCount()).isEqualTo(1);

        // TODO: rberghegger 22.03.17  We should test that the original presenter is recovered but this is not possible because the presenter can't be recovered in TiFragmentDelegate onCreate() :-(
        // Then the Activity is recreated.
        /*final HostingActivity hostingActivity2 = new HostingActivity();

        // And when the back stack is popped a new Fragment instance is created.
        final TestPresenter presenter2 = new TestPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(true)
                .setRetainPresenterEnabled(true)
                .build());
        final TestTiFragment delegate = new TestTiFragment.Builder()2
                = new TiFragmentDelegateBuilder()
                .setDontKeepActivitiesEnabled(false)
                .setHostingActivity(hostingActivity2)
                .setSavior(mSavior)
                .setPresenter(presenter2)
                .build();

        delegate2.onCreate(mSavedState);
        delegate2.onCreateView(mock(LayoutInflater.class), null, mSavedState);
        delegate2.onStart();

        // Then assert that the new Presenter does equal the previous presenter.
        assertThat(delegate2.getPresenter()).isEqualTo(presenter);*/
    }

    @Test
    public void saviorTrue_retainTrue_backstackTrue_activityFinishing() {

        final HostingActivity hostingActivity = new HostingActivity();

        // Given a Presenter that uses a static savior to retain itself.
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(true)
                .setRetainPresenterEnabled(true)
                .build());

        // And given a Fragment.
        final TestTiFragment delegate = new TestTiFragment.Builder()

                .setDontKeepActivitiesEnabled(true)
                .setHostingActivity(hostingActivity)
                .setSavior(mSavior)
                .setPresenter(presenter)
                .build();

        // When the Fragment is added to the Activity.
        delegate.onCreate(null);
        delegate.onCreateView(mock(LayoutInflater.class), null, null);
        delegate.onStart();

        // And when the Fragment is replaced by another Fragment.
        delegate.onStop();
        delegate.onDestroyView();

        // And when the Activity is finishing.
        hostingActivity.setFinishing(true);
        delegate.onDestroy();

        // Then assert that the Presenter is destroyed and not saved in the savior.
        assertThat(delegate.getPresenter().isDestroyed()).isTrue();
        assertThat(mSavior.presenterCount()).isEqualTo(0);
    }

    @Test
    public void saviorTrue_retainTrue_backstackTrue_popBackstack() {

        final HostingActivity hostingActivity = new HostingActivity();

        // Given a Presenter that uses a static savior to retain itself.
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(true)
                .setRetainPresenterEnabled(true)
                .build());

        // And given a Fragment.
        final TestTiFragment delegate = new TestTiFragment.Builder()

                .setDontKeepActivitiesEnabled(true)
                .setHostingActivity(hostingActivity)
                .setSavior(mSavior)
                .setPresenter(presenter)
                .build();

        // When the Fragment is added to the Activity.
        delegate.onCreate(null);
        delegate.onCreateView(mock(LayoutInflater.class), null, null);
        delegate.onStart();

        // And when the Fragment is replaced by another Fragment.
        delegate.onStop();
        delegate.onDestroyView();

        // And whent the back stack is popped.
        delegate.onCreateView(mock(LayoutInflater.class), null, null);
        delegate.onStart();

        // Then assert that the Presenter is not destroyed and saved in the savior.
        assertThat(delegate.getPresenter().isDestroyed()).isFalse();
        assertThat(delegate.getPresenter()).isEqualTo(presenter);
        assertThat(mSavior.presenterCount()).isEqualTo(1);
    }
}
