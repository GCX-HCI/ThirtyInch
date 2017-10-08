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

import net.grandcentrix.thirtyinch.TiConfiguration;
import org.junit.*;

public class TiActivityPresenterDestroyTest extends AbstractPresenterDestroyTest {

    @Test
    public void configurationChange_retainFalse_dkAFalse() throws Exception {
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setRetainPresenterEnabled(false)
                .build());

        final TestTiActivity activity = new TestTiActivity.Builder()
                .setPresenter(presenter)
                .setSavior(mSavior)
                .build();

        // When the Activity gets created
        activity.onCreate(null);
        assertThat(activity.getPresenter().isInitialized()).isTrue();
        activity.onStart();
        assertThat(activity.getPresenter().isDestroyed()).isFalse();

        // Then the presenter is saved in savior
        assertThat(presenter.isDestroyed()).isFalse();
        assertThat(mSavior.getPresenterCount()).isEqualTo(0);

        // When the Activity changes configurations
        activity.setFinishing(false);
        activity.onStop();
        assertThat(mSavior.mActivityInstanceObserver).isNull();
        activity.onSaveInstanceState(mActivitySavedState);
        activity.onDestroy();

        // Then the presenter is destroyed
        assertThat(presenter.isDestroyed()).isTrue();
        assertThat(mSavior.getPresenterCount()).isEqualTo(0);

        final TestPresenter presenter2 = new TestPresenter(new TiConfiguration.Builder()
                .setRetainPresenterEnabled(false)
                .build());
        // And recovered in the new instance
        final TestTiActivity activity2 = new TestTiActivity.Builder()
                .setSavior(mSavior)
                .setPresenter(presenter2)
                .build();

        // When the new Activity instance gets created
        assertThat(mSavior.mActivityInstanceObserver).isNull();
        activity2.onCreate(mActivitySavedState);
        activity2.onStart();

        // Then a new presenter gets created
        assertThat(activity2.getPresenter()).isNotEqualTo(presenter);
    }

    @Test
    public void configurationChange_retainFalse_dkATrue() throws Exception {
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setRetainPresenterEnabled(false)
                .build());

        final TestTiActivity activity = new TestTiActivity.Builder()
                .setPresenter(presenter)
                .setSavior(mSavior)
                .build();

        // When the Activity gets created
        activity.onCreate(null);
        assertThat(activity.getPresenter().isInitialized()).isTrue();
        activity.onStart();
        assertThat(activity.getPresenter().isDestroyed()).isFalse();

        // Then the presenter is saved in savior
        assertThat(presenter.isDestroyed()).isFalse();
        assertThat(mSavior.getPresenterCount()).isEqualTo(0);

        // When the Activity changes configurations
        activity.setFinishing(false);
        activity.onStop();
        assertThat(mSavior.mActivityInstanceObserver).isNull();
        activity.onSaveInstanceState(mActivitySavedState);
        activity.onDestroy();

        // Then the presenter is destroyed
        assertThat(presenter.isDestroyed()).isTrue();
        assertThat(mSavior.getPresenterCount()).isEqualTo(0);

        final TestPresenter presenter2 = new TestPresenter(new TiConfiguration.Builder()
                .setRetainPresenterEnabled(false)
                .build());
        // And recovered in the new instance
        final TestTiActivity activity2 = new TestTiActivity.Builder()
                .setSavior(mSavior)
                .setPresenter(presenter2)
                .build();

        // When the new Activity instance gets created
        assertThat(mSavior.mActivityInstanceObserver).isNull();
        activity2.onCreate(mActivitySavedState);
        activity2.onStart();

        // Then a new presenter gets created
        assertThat(activity2.getPresenter()).isNotEqualTo(presenter);
    }

    @Test
    public void configurationChange_retainTrue_dkAFalse() throws Exception {
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setRetainPresenterEnabled(true)
                .build());

        final TestTiActivity activity = new TestTiActivity.Builder()
                .setPresenter(presenter)
                .setSavior(mSavior)
                .build();

        // When the Activity gets created
        activity.onCreate(null);
        assertThat(activity.getPresenter().isInitialized()).isTrue();
        activity.onStart();
        assertThat(activity.getPresenter().isDestroyed()).isFalse();

        // Then the presenter is saved in savior
        assertThat(presenter.isDestroyed()).isFalse();
        assertThat(mSavior.getPresenterCount()).isEqualTo(1);

        // When the Activity changes configurations
        activity.setFinishing(false);
        activity.onStop();
        mSavior.mActivityInstanceObserver.onActivitySaveInstanceState(
                activity.getMockActivityInstance(), mActivitySavedState);
        activity.onSaveInstanceState(mActivitySavedState);
        activity.onDestroy();

        // Then the presenter is not destroyed
        assertThat(presenter.isDestroyed()).isFalse();
        assertThat(mSavior.getPresenterCount()).isEqualTo(1);

        // And recovered in the new instance
        final TestTiActivity activity2 = new TestTiActivity.Builder()
                .setSavior(mSavior)
                .build();

        // When the new Activity instance gets created
        mSavior.mActivityInstanceObserver.onActivityCreated(
                activity2.getMockActivityInstance(), mActivitySavedState);
        activity2.onCreate(mActivitySavedState);
        activity2.onStart();

        // Then the same presenter gets recovered
        assertThat(activity2.getPresenter()).isEqualTo(presenter);
    }

    @Test
    public void configurationChange_retainTrue_dkATrue() throws Exception {
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setRetainPresenterEnabled(true)
                .build());

        final TestTiActivity activity = new TestTiActivity.Builder()
                .setPresenter(presenter)
                .setSavior(mSavior)
                .build();

        // When the Activity gets created
        activity.onCreate(null);
        assertThat(activity.getPresenter().isInitialized()).isTrue();
        activity.onStart();
        assertThat(activity.getPresenter().isDestroyed()).isFalse();

        // Then the presenter is saved in savior
        assertThat(presenter.isDestroyed()).isFalse();
        assertThat(mSavior.getPresenterCount()).isEqualTo(1);

        // When the Activity changes configurations
        activity.setFinishing(false);
        activity.onStop();
        mSavior.mActivityInstanceObserver.onActivitySaveInstanceState(
                activity.getMockActivityInstance(), mActivitySavedState);
        activity.onSaveInstanceState(mActivitySavedState);
        activity.onDestroy();

        // Then the presenter is not destroyed
        assertThat(presenter.isDestroyed()).isFalse();
        assertThat(mSavior.getPresenterCount()).isEqualTo(1);

        // And recovered in the new instance
        final TestTiActivity activity2 = new TestTiActivity.Builder()
                .setSavior(mSavior)
                .build();

        // When the new Activity instance gets created
        mSavior.mActivityInstanceObserver.onActivityCreated(
                activity2.getMockActivityInstance(), mActivitySavedState);
        activity2.onCreate(mActivitySavedState);
        activity2.onStart();

        // Then the same presenter gets recovered
        assertThat(activity2.getPresenter()).isEqualTo(presenter);
    }

    @Test
    public void finish_retainFalse_dkAFalse() throws Exception {
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setRetainPresenterEnabled(false)
                .build());

        final TestTiActivity activity = new TestTiActivity.Builder()
                .setPresenter(presenter)
                .setSavior(mSavior)
                .build();

        // When the Activity gets created
        activity.onCreate(null);
        assertThat(activity.getPresenter().isInitialized()).isTrue();
        activity.onStart();
        assertThat(activity.getPresenter().isDestroyed()).isFalse();

        // Then the presenter is saved in savior
        assertThat(presenter.isDestroyed()).isFalse();
        assertThat(mSavior.getPresenterCount()).isEqualTo(0);

        // When the Activity gets finished
        activity.setFinishing(true);
        activity.onStop();
        assertThat(mSavior.mActivityInstanceObserver).isNull();
        activity.onSaveInstanceState(mActivitySavedState);
        activity.onDestroy();

        // Then the presenter is destroyed
        assertThat(presenter.isDestroyed()).isTrue();
        assertThat(mSavior.getPresenterCount()).isEqualTo(0);
    }

    @Test
    public void finish_retainFalse_dkATrue() throws Exception {
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setRetainPresenterEnabled(false)
                .build());

        final TestTiActivity activity = new TestTiActivity.Builder()
                .setPresenter(presenter)
                .setSavior(mSavior)
                .build();

        // When the Activity gets created
        activity.onCreate(null);
        assertThat(activity.getPresenter().isInitialized()).isTrue();
        activity.onStart();
        assertThat(activity.getPresenter().isDestroyed()).isFalse();

        // Then the presenter is saved in savior
        assertThat(presenter.isDestroyed()).isFalse();
        assertThat(mSavior.getPresenterCount()).isEqualTo(0);

        // When the Activity gets finished
        activity.setFinishing(true);
        activity.onStop();
        assertThat(mSavior.mActivityInstanceObserver).isNull();
        activity.onSaveInstanceState(mActivitySavedState);
        activity.onDestroy();

        // savior cleaned up
        assertThat(mSavior.mActivityInstanceObserver).isNull();

        // Then the presenter is destroyed
        assertThat(presenter.isDestroyed()).isTrue();
        assertThat(mSavior.getPresenterCount()).isEqualTo(0);
    }

    @Test
    public void finish_retainTrue_dkAFalse() throws Exception {
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setRetainPresenterEnabled(true)
                .build());

        final TestTiActivity activity = new TestTiActivity.Builder()
                .setPresenter(presenter)
                .setSavior(mSavior)
                .build();

        // When the Activity gets created
        activity.onCreate(null);
        assertThat(activity.getPresenter().isInitialized()).isTrue();
        activity.onStart();
        assertThat(activity.getPresenter().isDestroyed()).isFalse();

        // Then the presenter is saved in savior
        assertThat(presenter.isDestroyed()).isFalse();
        assertThat(mSavior.getPresenterCount()).isEqualTo(1);

        // When the Activity gets finished
        activity.setFinishing(true);
        activity.onStop();
        mSavior.mActivityInstanceObserver.onActivitySaveInstanceState(
                activity.getMockActivityInstance(), mActivitySavedState);
        activity.onSaveInstanceState(mActivitySavedState);
        activity.onDestroy();

        // Then the presenter is destroyed
        assertThat(presenter.isDestroyed()).isTrue();
        assertThat(mSavior.getPresenterCount()).isEqualTo(0);
    }

    @Test
    public void finish_retainTrue_dkATrue() throws Exception {
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setRetainPresenterEnabled(true)
                .build());

        final TestTiActivity activity = new TestTiActivity.Builder()
                .setPresenter(presenter)
                .setSavior(mSavior)
                .build();

        // When the Activity gets created
        activity.onCreate(null);
        assertThat(activity.getPresenter().isInitialized()).isTrue();
        activity.onStart();
        assertThat(activity.getPresenter().isDestroyed()).isFalse();

        // Then the presenter is saved in savior
        assertThat(presenter.isDestroyed()).isFalse();
        assertThat(mSavior.getPresenterCount()).isEqualTo(1);

        // When the Activity gets finished
        activity.setFinishing(true);
        activity.onStop();
        mSavior.mActivityInstanceObserver.onActivitySaveInstanceState(
                activity.getMockActivityInstance(), mActivitySavedState);
        activity.onSaveInstanceState(mActivitySavedState);
        activity.onDestroy();

        // savior cleaned up
        assertThat(mSavior.mActivityInstanceObserver).isNull();

        // Then the presenter is destroyed
        assertThat(presenter.isDestroyed()).isTrue();
        assertThat(mSavior.getPresenterCount()).isEqualTo(0);
    }

    @Test
    public void moveToBackground_moveToForeground_retainFalse_dkAFalse() throws Exception {

        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setRetainPresenterEnabled(false)
                .build());

        final TestTiActivity activity = new TestTiActivity.Builder()
                .setPresenter(presenter)
                .setSavior(mSavior)
                .build();

        // When the Activity gets created
        activity.onCreate(null);
        assertThat(activity.getPresenter().isInitialized()).isTrue();
        activity.onStart();
        assertThat(activity.getPresenter().isDestroyed()).isFalse();

        // When the Activity moves to background
        activity.setFinishing(false);
        activity.onStop();
        assertThat(mSavior.mActivityInstanceObserver).isNull();

        // Then the presenter is not destroyed
        assertThat(presenter.isDestroyed()).isFalse();
        assertThat(mSavior.getPresenterCount()).isEqualTo(0);

        // When the Activity moves to foreground
        activity.onStart();

        // Then presenter not destroyed and the same
        assertThat(activity.getPresenter().isDestroyed()).isFalse();
        assertThat(activity.getPresenter()).isEqualTo(presenter);
    }

    @Test
    public void moveToBackground_moveToForeground_retainFalse_dkATrue() throws Exception {
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setRetainPresenterEnabled(false)
                .build());

        final TestTiActivity activity = new TestTiActivity.Builder()
                .setPresenter(presenter)
                .setSavior(mSavior)
                .build();

        // When the Activity gets created
        activity.onCreate(null);
        assertThat(activity.getPresenter().isInitialized()).isTrue();
        activity.onStart();
        assertThat(activity.getPresenter().isDestroyed()).isFalse();

        // When the Activity moves to background
        activity.setFinishing(false);
        activity.onStop();
        assertThat(mSavior.mActivityInstanceObserver).isNull();
        activity.onSaveInstanceState(mActivitySavedState);
        activity.onDestroy();

        // Then the presenter is not destroyed
        assertThat(presenter.isDestroyed()).isTrue();
        assertThat(mSavior.getPresenterCount()).isEqualTo(0);

        final TestPresenter presenter2 = new TestPresenter(new TiConfiguration.Builder()
                .setRetainPresenterEnabled(false)
                .build());

        // When the Activity moves to foreground
        final TestTiActivity activity2 = new TestTiActivity.Builder()
                .setSavior(mSavior)
                .setPresenter(presenter2)
                .build();

        // When the new Activity instance gets created
        assertThat(mSavior.mActivityInstanceObserver).isNull();
        activity2.onCreate(mActivitySavedState);
        assertThat(activity2.getPresenter().isInitialized()).isTrue();
        activity2.onStart();
        assertThat(activity2.getPresenter().isDestroyed()).isFalse();

        // Then a new presenter was created
        assertThat(activity2.getPresenter()).isNotEqualTo(presenter);
        assertThat(presenter2.isDestroyed()).isFalse();
    }

    @Test
    public void moveToBackground_moveToForeground_retainTrue_dkAFalse() throws Exception {

        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setRetainPresenterEnabled(true)
                .build());

        final TestTiActivity activity = new TestTiActivity.Builder()
                .setPresenter(presenter)
                .setSavior(mSavior)
                .build();

        // When the Activity gets created
        activity.onCreate(null);
        assertThat(activity.getPresenter().isInitialized()).isTrue();
        activity.onStart();
        assertThat(activity.getPresenter().isDestroyed()).isFalse();

        // When the Activity moves to background
        activity.setFinishing(false);
        activity.onStop();
        mSavior.mActivityInstanceObserver.onActivitySaveInstanceState(
                activity.getMockActivityInstance(), mActivitySavedState);

        // Then the presenter is not destroyed
        assertThat(presenter.isDestroyed()).isFalse();
        assertThat(mSavior.getPresenterCount()).isEqualTo(1);

        // When the Activity moves to foreground
        activity.onStart();

        // Then presenter not destroyed and the same
        assertThat(activity.getPresenter().isDestroyed()).isFalse();
        assertThat(activity.getPresenter()).isEqualTo(presenter);
    }

    @Test
    public void moveToBackground_moveToForeground_retainTrue_dkATrue() throws Exception {
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setRetainPresenterEnabled(true)
                .build());

        final TestTiActivity activity = new TestTiActivity.Builder()
                .setPresenter(presenter)
                .setSavior(mSavior)
                .build();

        // When the Activity gets created
        activity.onCreate(null);
        assertThat(activity.getPresenter().isInitialized()).isTrue();
        activity.onStart();
        assertThat(activity.getPresenter().isDestroyed()).isFalse();

        // When the Activity moves to background
        activity.setFinishing(false);
        activity.onStop();
        mSavior.mActivityInstanceObserver.onActivitySaveInstanceState(
                activity.getMockActivityInstance(), mActivitySavedState);
        activity.onSaveInstanceState(mActivitySavedState);
        activity.onDestroy();

        // Then the presenter is not destroyed
        assertThat(presenter.isDestroyed()).isFalse();
        assertThat(mSavior.getPresenterCount()).isEqualTo(1);

        // When the Activity moves to foreground
        final TestTiActivity activity2 = new TestTiActivity.Builder()
                .setSavior(mSavior)
                .build();

        // When the new Activity instance gets created
        mSavior.mActivityInstanceObserver.onActivityCreated(
                activity2.getMockActivityInstance(), mActivitySavedState);
        activity2.onCreate(mActivitySavedState);
        assertThat(activity2.getPresenter().isInitialized()).isTrue();
        activity2.onStart();
        assertThat(activity2.getPresenter().isDestroyed()).isFalse();

        // Then the same presenter gets recovered
        assertThat(activity2.getPresenter()).isEqualTo(presenter);
    }

}
