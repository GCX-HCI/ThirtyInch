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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;

import java.util.HashMap;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public class TiFragmentPresenterDestroyTest {

    private class PutInMapAnswer implements Answer<Void> {

        final HashMap<String, String> map = new HashMap<>();

        @Override
        public Void answer(InvocationOnMock invocation) throws Throwable {
            final Object[] args = invocation.getArguments();
            map.put((String) args[0], (String) args[1]);
            return null;
        }
    }

    private class TestPresenter extends TiPresenter<TiView> {

        TestPresenter(TiConfiguration config) {
            super(config);
        }
    }

    private MockSavior mSavior;

    @Test
    public void saviorFalse_retainFalse_dontKeepActivitiesFalse_activityChangingConfiguration() {

        // Given a Presenter that does not use a static savior and does not retain itself.
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(false)
                .setRetainPresenterEnabled(false)
                .build());

        // And given a Fragment.
        final TiFragmentDelegate<TiPresenter<TiView>, TiView> delegate
                = new TiFragmentDelegateBuilder()
                .setDontKeepActivitiesEnabled(false)
                .setIsAdded(true)
                .setIsHostingActivityFinishing(false)
                .setIsHostingActivityChangingConfiguration(true)
                .setSavior(mSavior)
                .setPresenter(presenter)
                .build();

        final Bundle savedState = mock(Bundle.class);
        final TiFragmentPresenterDestroyTest.PutInMapAnswer putInMap = putInMap();
        doAnswer(putInMap).when(savedState).putString(anyString(), anyString());

        // TODO: rberghegger 16.03.17 call onAttach()?

        // When the fragment is added to the activity.
        delegate.onCreate_afterSuper(savedState);

        delegate.onCreateView_beforeSuper(mock(LayoutInflater.class), null, null);

        delegate.onStart_afterSuper();

        // And when the activity is changing configurations.
        delegate.onSaveInstanceState_afterSuper(savedState);

        delegate.onStop_beforeSuper();

        delegate.onDestroyView_beforeSuper();

        delegate.onDestroy_afterSuper();

        // TODO: rberghegger 16.03.17 call on detach?

        // TODO: rberghegger 16.03.17 call onAttach()?

        delegate.onCreate_afterSuper(savedState);

        delegate.onCreateView_beforeSuper(mock(LayoutInflater.class), null, null);

        delegate.onStart_afterSuper();

        // Then assert that the presenter is destroyed and not saved in the savior.
        assertThat(delegate.getPresenter().isDestroyed()).isTrue();
        assertThat(mSavior.presenterCount()).isEqualTo(0);
    }

    @Test
    public void saviorFalse_retainFalse_dontKeepActivitiesFalse_activityFinishing() {

        // Given a Presenter that does not use a static savior and does not retain itself.
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(false)
                .setRetainPresenterEnabled(false)
                .build());

        // And given a Fragment.
        final TiFragmentDelegate<TiPresenter<TiView>, TiView> delegate
                = new TiFragmentDelegateBuilder()
                .setDontKeepActivitiesEnabled(false)
                .setIsAdded(true)
                .setIsHostingActivityFinishing(true)
                .setIsHostingActivityChangingConfiguration(false)
                .setSavior(mSavior)
                .setPresenter(presenter)
                .build();

        final Bundle savedState = mock(Bundle.class);
        final TiFragmentPresenterDestroyTest.PutInMapAnswer putInMap = putInMap();
        doAnswer(putInMap).when(savedState).putString(anyString(), anyString());

        // TODO: rberghegger 16.03.17 call onAttach()?

        // When the fragment is added to the activity.
        delegate.onCreate_afterSuper(savedState);

        delegate.onCreateView_beforeSuper(mock(LayoutInflater.class), null, null);

        delegate.onStart_afterSuper();

        // And when the activity is finishing.
        delegate.onStop_beforeSuper();

        delegate.onDestroyView_beforeSuper();

        delegate.onDestroy_afterSuper();

        // TODO: rberghegger 16.03.17 call onDetach()?

        // Then assert that the presenter is destroyed and not saved in the savior.
        assertThat(delegate.getPresenter().isDestroyed()).isTrue();
        assertThat(mSavior.presenterCount()).isEqualTo(0);
    }

    @Test
    public void saviorFalse_retainFalse_dontKeepActivitiesFalse_backStack() {

        // Given a Presenter that does not use a static savior and does not retain itself.
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(false)
                .setRetainPresenterEnabled(false)
                .build());

        // And given a Fragment.
        final TiFragmentDelegate<TiPresenter<TiView>, TiView> delegate
                = new TiFragmentDelegateBuilder()
                .setDontKeepActivitiesEnabled(false)
                .setIsAdded(true)
                .setIsHostingActivityFinishing(true)
                .setIsHostingActivityChangingConfiguration(false)
                .setSavior(mSavior)
                .setPresenter(presenter)
                .build();

        final Bundle savedState = mock(Bundle.class);
        final TiFragmentPresenterDestroyTest.PutInMapAnswer putInMap = putInMap();
        doAnswer(putInMap).when(savedState).putString(anyString(), anyString());

        // TODO: rberghegger 16.03.17 call onAttach()?

        // When the fragment is added to the activity.
        delegate.onCreate_afterSuper(savedState);

        delegate.onCreateView_beforeSuper(mock(LayoutInflater.class), null, null);

        delegate.onStart_afterSuper();

        // And when it is replaced by another Fragment.
        delegate.onStop_beforeSuper();

        delegate.onDestroyView_beforeSuper();

        // And when the back stack is popped.
        delegate.onCreateView_beforeSuper(mock(LayoutInflater.class), null, null);

        delegate.onStart_afterSuper();

        // Then assert that the presenter is destroyed and not saved in the savior.
        assertThat(delegate.getPresenter().isDestroyed()).isFalse();
        assertThat(mSavior.presenterCount()).isEqualTo(0);
    }

    @Test
    public void saviorTrue_retainTrue_dontKeepActivitiesTrue_fragmentAttached_activityFinishing() {

        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(true)
                .setRetainPresenterEnabled(true)
                .build());

        final TiFragmentDelegate<TiPresenter<TiView>, TiView> delegate
                = new TiFragmentDelegateBuilder()
                .setDontKeepActivitiesEnabled(true)
                .setIsAdded(true)
                .setIsHostingActivityFinishing(true)
                .setIsHostingActivityChangingConfiguration(false)
                .setSavior(mSavior)
                .setPresenter(presenter)
                .build();

        final Bundle savedState = mock(Bundle.class);
        final TiFragmentPresenterDestroyTest.PutInMapAnswer putInMap = putInMap();
        doAnswer(putInMap).when(savedState).putString(anyString(), anyString());

        delegate.onCreate_afterSuper(null);

        assertThat(mSavior.presenterCount()).isEqualTo(1);

        assertThat(delegate.getPresenter().isInitialized()).isTrue();

        delegate.onCreateView_beforeSuper(mock(LayoutInflater.class), null, null);

        delegate.onStart_afterSuper();

        delegate.onSaveInstanceState_afterSuper(savedState);

        assertThat(putInMap.map).containsKey(TiFragmentDelegate.SAVED_STATE_PRESENTER_ID);
        assertThat(putInMap.map.get(TiFragmentDelegate.SAVED_STATE_PRESENTER_ID)).isNotNull();

        delegate.onStop_beforeSuper();

        delegate.onDestroyView_beforeSuper();

        delegate.onDestroy_afterSuper();

        assertThat(delegate.getPresenter().isDestroyed()).isTrue();
        assertThat(mSavior.presenterCount()).isEqualTo(0);
    }

    @Before
    public void setUp() throws Exception {
        mSavior = new MockSavior();
    }

    @After
    public void tearDown() throws Exception {
        mSavior.clear();
    }

    @NonNull
    private PutInMapAnswer putInMap() {
        return new PutInMapAnswer();
    }
}
