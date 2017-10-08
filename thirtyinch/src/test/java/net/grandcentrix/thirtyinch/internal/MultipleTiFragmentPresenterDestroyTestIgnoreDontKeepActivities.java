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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import android.view.LayoutInflater;
import net.grandcentrix.thirtyinch.TiConfiguration;
import org.junit.*;

/**
 * Same test cases as {@link MultipleTiFragmentPresenterDestroyTest} but with the "don't keep
 * Activities option enabled". This means that the lifecycle may be different but the final
 * assertions must be identical
 */
public class MultipleTiFragmentPresenterDestroyTestIgnoreDontKeepActivities
        extends AbstractPresenterDestroyTest {

    @Test
    public void activityChangingConfiguration_retainFalse_backstackFalse_dkATrue() {

        final HostingActivity hostingActivity = new HostingActivity();

        // Given a Presenter that does use a static savior but does not retain itself.
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setRetainPresenterEnabled(false)
                .build());

        // And given a Fragment.
        final TestTiFragment fragment = new TestTiFragment.Builder()
                .setHostingActivity(hostingActivity)
                .setSavior(mSavior)
                .setPresenter(presenter)
                .build();

        // When the Fragment is added to the Activity.
        fragment.onCreate(null);
        fragment.onCreateView(mock(LayoutInflater.class), null, null);
        fragment.onStart();

        // And when the Fragment is replaced by another Fragment.
        fragment.onStop();
        fragment.onDestroyView();
        fragment.onDestroy();

        // Then assert that the Presenter is destroyed and not saved in the savior
        // because there is no way to bring the Fragment back.
        assertThat(fragment.getPresenter().isDestroyed()).isTrue();
        assertThat(mSavior.getPresenterCount()).isEqualTo(0);
    }

    @Test
    public void activityChangingConfiguration_retainFalse_backstackTrue_dkATrue() {

        final HostingActivity hostingActivity = new HostingActivity();

        // Given a Presenter that does use a static savior but does not retain itself.
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setRetainPresenterEnabled(false)
                .build());

        // And given a Fragment.
        final TestTiFragment fragment = new TestTiFragment.Builder()
                .setHostingActivity(hostingActivity)
                .setSavior(mSavior)
                .setPresenter(presenter)
                .build();

        // When the Fragment is added to the Activity.
        fragment.setInBackstack(true);
        fragment.onCreate(null);
        fragment.setAdded(true);
        fragment.onCreateView(mock(LayoutInflater.class), null, null);
        fragment.onStart();

        // And when the Fragment is replaced by another Fragment.
        fragment.setAdded(false);
        fragment.setRemoving(true);
        fragment.onStop();
        fragment.onDestroyView();

        // And when the Activity is changing its configuration.
        fragment.onSaveInstanceState(mFragmentSavedState);
        fragment.onDestroy();

        // Then assert that the Presenter is destroyed and not saved in the savior.
        assertThat(fragment.getPresenter().isDestroyed()).isTrue();
        assertThat(mSavior.getPresenterCount()).isEqualTo(0);
    }

    @Test
    public void activityChangingConfiguration_retainTrue_backstackFalse_dkATrue() {

        final HostingActivity hostingActivity = new HostingActivity();

        // Given a Presenter that does use a static savior and does retain itself.
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setRetainPresenterEnabled(true)
                .build());

        // And given a Fragment.
        final TestTiFragment fragment = new TestTiFragment.Builder()
                .setHostingActivity(hostingActivity)
                .setSavior(mSavior)
                .setPresenter(presenter)
                .build();

        // When the Fragment is added to the Activity.
        fragment.onCreate(null);
        fragment.setAdded(true);
        fragment.onCreateView(mock(LayoutInflater.class), null, null);
        fragment.onStart();

        // And when the Fragment is replaced by another Fragment.
        fragment.setAdded(false);
        fragment.setRemoving(true);
        fragment.onStop();
        fragment.onDestroyView();
        fragment.onDestroy();

        // The Presenter is destroyed and not saved in the savior
        assertThat(fragment.getPresenter().isDestroyed()).isTrue();
        assertThat(mSavior.getPresenterCount()).isEqualTo(0);

        // When the Activity is changing its configuration.

        // Then nothing happens with the fragment, not managed anymore
        assertThat(fragment.getPresenter().isDestroyed()).isTrue();
        assertThat(mSavior.getPresenterCount()).isEqualTo(0);
    }

    @Test
    public void activityChangingConfiguration_retainTrue_backstackTrue_dkATrue() {

        final HostingActivity hostingActivity = new HostingActivity();

        // Given a Presenter that uses a static savior to retain itself.
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setRetainPresenterEnabled(true)
                .build());

        // And given a Fragment.
        final TestTiFragment fragment = new TestTiFragment.Builder()
                .setHostingActivity(hostingActivity)
                .setSavior(mSavior)
                .setPresenter(presenter)
                .build();

        // When the Fragment is added to the Activity.
        fragment.setInBackstack(true);
        fragment.onCreate(null);
        fragment.setAdded(true);
        fragment.onCreateView(mock(LayoutInflater.class), null, null);
        fragment.onStart();

        // And when the Fragment is replaced by another Fragment.
        fragment.setAdded(false);
        fragment.setRemoving(true);
        fragment.onStop();
        fragment.onDestroyView();

        // Then the presenter is not destroyed and saved in the savior
        assertThat(fragment.getPresenter().isDestroyed()).isFalse();
        assertThat(mSavior.getPresenterCount()).isEqualTo(1);

        // When the Activity is changing its configuration.
        mSavior.mActivityInstanceObserver.onActivitySaveInstanceState(
                hostingActivity.getMockActivityInstance(), mActivitySavedState);
        fragment.onSaveInstanceState(mFragmentSavedState);
        fragment.onDestroy();

        // Then a new Activity is recreated.
        final HostingActivity hostingActivity2 = new HostingActivity();
        mSavior.mActivityInstanceObserver.onActivityCreated(
                hostingActivity2.getMockActivityInstance(), mActivitySavedState);

        // Then the Presenter is not destroyed and saved in the savior.
        assertThat(fragment.getPresenter().isDestroyed()).isFalse();
        assertThat(mSavior.getPresenterCount()).isEqualTo(1);

        // When the back stack is popped a new Fragment instance is created.
        final TestPresenter presenter2 = new TestPresenter(new TiConfiguration.Builder()
                .setRetainPresenterEnabled(true)
                .build());

        final TestTiFragment fragment2 = new TestTiFragment.Builder()
                .setHostingActivity(hostingActivity2)
                .setSavior(mSavior)
                .setPresenter(presenter2)
                .build();

        // And the instance will be created with the saved instance state
        fragment2.setInBackstack(true);
        fragment2.onCreate(mFragmentSavedState);
        fragment2.setAdded(true);
        fragment2.onCreateView(mock(LayoutInflater.class), null, mFragmentSavedState);
        fragment2.onStart();

        // Then the same presenter gets recovered
        assertThat(fragment2.getPresenter()).isEqualTo(presenter);
        assertThat(fragment2.getPresenter().isDestroyed()).isFalse();
    }

    @Test
    public void activityChangingConfiguration_thenFinish_retainFalse_backstackTrue_dkATrue() {

        final HostingActivity hostingActivity = new HostingActivity();

        // Given a Presenter that uses a static savior to retain itself.
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setRetainPresenterEnabled(false)
                .build());

        // And given a Fragment.
        final TestTiFragment fragment = new TestTiFragment.Builder()
                .setHostingActivity(hostingActivity)
                .setSavior(mSavior)
                .setPresenter(presenter)
                .build();

        // When the Fragment is added to the Activity.
        fragment.setInBackstack(true);
        fragment.onCreate(null);
        fragment.setAdded(true);
        fragment.onCreateView(mock(LayoutInflater.class), null, null);
        fragment.onStart();

        // And when the Fragment is replaced by another Fragment.
        fragment.setAdded(false);
        fragment.setRemoving(true);
        fragment.onStop();
        fragment.onDestroyView();

        // Then the presenter is not destroyed, onDestroy wasn't called
        assertThat(fragment.getPresenter().isDestroyed()).isFalse();

        // Then the presenter is not saved in the savior
        assertThat(mSavior.getPresenterCount()).isEqualTo(0);

        assertThat(mSavior.mActivityInstanceObserver).isNull();
        fragment.onSaveInstanceState(mFragmentSavedState);
        fragment.onDestroy();

        // Then the Presenter is destroyed and not saved in the savior.
        assertThat(fragment.getPresenter().isDestroyed()).isTrue();
        assertThat(mSavior.getPresenterCount()).isEqualTo(0);

        // Then a new Activity is recreated.
        final HostingActivity hostingActivity2 = new HostingActivity();
        assertThat(mSavior.mActivityInstanceObserver).isNull();

        // When the Activity gets finished
        hostingActivity2.setFinishing(true);
        assertThat(mSavior.mActivityInstanceObserver).isNull();

        // Then nothing happens, the presenter is already destroyed
        assertThat(mSavior.getPresenterCount()).isEqualTo(0);
        assertThat(presenter.isDestroyed()).isTrue();
    }

    @Test
    public void activityChangingConfiguration_thenFinish_retainTrue_backstackTrue_dkATrue() {

        final HostingActivity hostingActivity = new HostingActivity();

        // Given a Presenter that uses a static savior to retain itself.
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setRetainPresenterEnabled(true)
                .build());

        // And given a Fragment.
        final TestTiFragment fragment = new TestTiFragment.Builder()
                .setHostingActivity(hostingActivity)
                .setSavior(mSavior)
                .setPresenter(presenter)
                .build();

        // When the Fragment is added to the Activity.
        fragment.setInBackstack(true);
        fragment.onCreate(null);
        fragment.setAdded(true);
        fragment.onCreateView(mock(LayoutInflater.class), null, null);
        fragment.onStart();

        // And when the Fragment is replaced by another Fragment.
        fragment.setAdded(false);
        fragment.setRemoving(true);
        fragment.onStop();
        fragment.onDestroyView();

        // Then the presenter is not destroyed and saved in the savior
        assertThat(fragment.getPresenter().isDestroyed()).isFalse();
        assertThat(mSavior.getPresenterCount()).isEqualTo(1);

        mSavior.mActivityInstanceObserver.onActivitySaveInstanceState(
                hostingActivity.getMockActivityInstance(), mActivitySavedState);
        fragment.onSaveInstanceState(mFragmentSavedState);
        fragment.onDestroy();

        // Then a new Activity is recreated.
        final HostingActivity hostingActivity2 = new HostingActivity();
        mSavior.mActivityInstanceObserver.onActivityCreated(
                hostingActivity2.getMockActivityInstance(), mActivitySavedState);

        // Then the Presenter is not destroyed and saved in the savior.
        assertThat(fragment.getPresenter().isDestroyed()).isFalse();
        assertThat(mSavior.getPresenterCount()).isEqualTo(1);

        // When the Activity gets finished
        hostingActivity2.setFinishing(true);
        mSavior.mActivityInstanceObserver.onActivityDestroyed(
                hostingActivity2.getMockActivityInstance());

        // Then the same presenter is destroyed
        assertThat(mSavior.getPresenterCount()).isEqualTo(0);
        assertThat(presenter.isDestroyed()).isTrue();
    }

    @Test
    public void activityFinishing_retainFalse_backstackFalse_dkATrue() {

        final HostingActivity hostingActivity = new HostingActivity();

        // Given a Presenter that does use a static savior but does not retain itself.
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setRetainPresenterEnabled(false)
                .build());

        // And given a Fragment.
        final TestTiFragment fragment = new TestTiFragment.Builder()
                .setHostingActivity(hostingActivity)
                .setSavior(mSavior)
                .setPresenter(presenter)
                .build();

        // When the Fragment is added to the Activity.
        fragment.onCreate(null);
        fragment.setAdded(true);
        fragment.onCreateView(mock(LayoutInflater.class), null, null);
        fragment.onStart();

        // Then the presenter is not saved in the savior
        assertThat(fragment.getPresenter().isDestroyed()).isFalse();
        assertThat(mSavior.getPresenterCount()).isEqualTo(0);

        // And when the Fragment is replaced by another Fragment.
        fragment.setAdded(false);
        fragment.setRemoving(true);
        fragment.onStop();
        fragment.onDestroyView();
        fragment.onDestroy();

        // Then assert that the Presenter is destroyed and not saved in the savior
        // because there is no way to bring the Fragment back.
        assertThat(fragment.getPresenter().isDestroyed()).isTrue();
        assertThat(mSavior.getPresenterCount()).isEqualTo(0);

        // When the activity finishes
        // nothing happens because the presenter is already destroyed
        hostingActivity.setFinishing(true);
        assertThat(mSavior.mActivityInstanceObserver).isNull();
    }

    @Test
    public void activityFinishing_retainFalse_backstackTrue_dkATrue() {

        final HostingActivity hostingActivity = new HostingActivity();

        // Given a Presenter that does use a static savior but does not retain itself.
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setRetainPresenterEnabled(false)
                .build());

        // And given a Fragment.
        final TestTiFragment fragment = new TestTiFragment.Builder()
                .setHostingActivity(hostingActivity)
                .setSavior(mSavior)
                .setPresenter(presenter)
                .build();

        // When the Fragment is added to the Activity.
        fragment.setInBackstack(true);
        fragment.onCreate(null);
        fragment.setAdded(true);
        fragment.onCreateView(mock(LayoutInflater.class), null, null);
        fragment.onStart();

        // And when the Fragment is replaced by another Fragment.
        fragment.setAdded(false);
        fragment.setRemoving(true);
        fragment.onStop();
        fragment.onDestroyView();

        // And when the Activity is finishing.
        hostingActivity.setFinishing(true);
        assertThat(mSavior.mActivityInstanceObserver).isNull();
        fragment.onDestroy();

        // Then assert that the Presenter is destroyed and not saved in the savior.
        assertThat(fragment.getPresenter().isDestroyed()).isTrue();
        assertThat(mSavior.getPresenterCount()).isEqualTo(0);
    }

    @Test
    public void activityFinishing_retainTrue_backstackFalse_dkATrue() {

        final HostingActivity hostingActivity = new HostingActivity();

        // Given a Presenter that does use a static savior and does retain itself.
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setRetainPresenterEnabled(true)
                .build());

        // And given a Fragment.
        final TestTiFragment fragment = new TestTiFragment.Builder()
                .setHostingActivity(hostingActivity)
                .setSavior(mSavior)
                .setPresenter(presenter)
                .build();

        // When the Fragment is added to the Activity.
        fragment.onCreate(null);
        fragment.setAdded(true);
        fragment.onCreateView(mock(LayoutInflater.class), null, null);
        fragment.onStart();

        // And when the Fragment is replaced by another Fragment.
        fragment.setAdded(true);
        fragment.setRemoving(true);
        fragment.onStop();
        fragment.onDestroyView();
        fragment.onDestroy();

        // Then assert that the Presenter is destroyed and not saved in the savior
        // because there is no way the Fragment can be brought back.
        assertThat(fragment.getPresenter().isDestroyed()).isTrue();
        assertThat(mSavior.getPresenterCount()).isEqualTo(0);

        // When the activity finishes
        // nothing happens because the presenter is already destroyed
        hostingActivity.setFinishing(true);
        assertThat(mSavior.mActivityInstanceObserver).isNull();
    }

    @Test
    public void activityFinishing_retainTrue_backstackTrue_dkATrue() {

        final HostingActivity hostingActivity = new HostingActivity();

        // Given a Presenter that uses a static savior to retain itself.
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setRetainPresenterEnabled(true)
                .build());

        // And given a Fragment.
        final TestTiFragment fragment = new TestTiFragment.Builder()
                .setHostingActivity(hostingActivity)
                .setSavior(mSavior)
                .setPresenter(presenter)
                .build();

        // When the Fragment is added to the Activity.
        fragment.setInBackstack(true);
        fragment.onCreate(null);
        fragment.setAdded(true);
        fragment.onCreateView(mock(LayoutInflater.class), null, null);
        fragment.onStart();

        // And when the Fragment is replaced by another Fragment.
        fragment.setAdded(false);
        fragment.setRemoving(true);
        fragment.onStop();
        fragment.onDestroyView();

        // And when the Activity is finishing.
        hostingActivity.setFinishing(true);
        mSavior.mActivityInstanceObserver.onActivityDestroyed(
                hostingActivity.getMockActivityInstance());
        fragment.onDestroy();

        // Then assert that the Presenter is destroyed and not saved in the savior.
        assertThat(fragment.getPresenter().isDestroyed()).isTrue();
        assertThat(mSavior.getPresenterCount()).isEqualTo(0);
    }

    @Test
    public void popBackstack_retainFalse_backstackTrue_dkATrue() {

        final HostingActivity hostingActivity = new HostingActivity();

        // Given a Presenter that does use a static savior but does not retain itself.
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setRetainPresenterEnabled(false)
                .build());

        // And given a Fragment.
        final TestTiFragment fragment = new TestTiFragment.Builder()
                .setHostingActivity(hostingActivity)
                .setSavior(mSavior)
                .setPresenter(presenter)
                .build();

        // When the Fragment is added to the Activity.
        fragment.setInBackstack(true);
        fragment.onCreate(null);
        fragment.setAdded(true);
        fragment.onCreateView(mock(LayoutInflater.class), null, null);
        fragment.onStart();

        // And when the Fragment is replaced by another Fragment.
        fragment.setAdded(false);
        fragment.setRemoving(true);
        fragment.onStop();
        fragment.onDestroyView();

        // And when the back stack is popped.
        fragment.setAdded(true);
        fragment.setRemoving(false);
        fragment.onCreateView(mock(LayoutInflater.class), null, null);
        fragment.onStart();

        // Then assert that the Presenter is not destroyed and not saved in the savior.
        assertThat(fragment.getPresenter().isDestroyed()).isFalse();
        assertThat(fragment.getPresenter()).isEqualTo(presenter);
        assertThat(mSavior.getPresenterCount()).isEqualTo(0);
    }

    @Test
    public void popBackstack_retainTrue_backstackTrue_dkATrue() {

        final HostingActivity hostingActivity = new HostingActivity();

        // Given a Presenter that uses a static savior to retain itself.
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setRetainPresenterEnabled(true)
                .build());

        // And given a Fragment.
        final TestTiFragment fragment = new TestTiFragment.Builder()
                .setHostingActivity(hostingActivity)
                .setSavior(mSavior)
                .setPresenter(presenter)
                .build();

        // When the Fragment is added to the Activity.
        fragment.setInBackstack(true);
        fragment.onCreate(null);
        fragment.setAdded(true);
        fragment.onCreateView(mock(LayoutInflater.class), null, null);
        fragment.onStart();

        // And when the Fragment is replaced by another Fragment.
        fragment.setAdded(false);
        fragment.setRemoving(true);
        fragment.onStop();
        fragment.onDestroyView();

        // And whent the back stack is popped.
        fragment.setAdded(true);
        fragment.setRemoving(false);
        fragment.onCreateView(mock(LayoutInflater.class), null, null);
        fragment.onStart();

        // Then assert that the Presenter is not destroyed and saved in the savior.
        assertThat(fragment.getPresenter().isDestroyed()).isFalse();
        assertThat(fragment.getPresenter()).isEqualTo(presenter);
        assertThat(mSavior.getPresenterCount()).isEqualTo(1);
    }
}
