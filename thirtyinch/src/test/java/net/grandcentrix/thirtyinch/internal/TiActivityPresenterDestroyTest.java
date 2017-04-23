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

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.HashMap;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public class TiActivityPresenterDestroyTest {

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

        public TestPresenter(TiConfiguration config) {
            super(config);
        }
    }

    private TestPresenterSavior mSavior;

    @Test
    public void dontKeepActivitiesFalse_configurationChange() throws Exception {
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .build());

        final TestPresenter[] retainedPresenter = new TestPresenter[]{null};
        final TiActivityDelegate<TiPresenter<TiView>, TiView> delegate = new TiActivityDelegateBuilder()
                .setPresenter(presenter)
                .setIsFinishing(false)
                .setIsChangingConfigurations(true)
                .setDontKeepActivitiesEnabled(false)
                .setSavior(mSavior)
                .setRetainedPresenterProvider(new TiPresenterProvider<TiPresenter<TiView>>() {
                    @NonNull
                    @Override
                    public TiPresenter<TiView> providePresenter() {
                        return retainedPresenter[0];
                    }
                })
                .build();

        final Bundle savedState = mock(Bundle.class);
        final PutInMapAnswer putInMap = putInMap();
        doAnswer(putInMap).when(savedState).putString(anyString(), anyString());

        doFullLifecycleAndDestroy(delegate, savedState);

        assertThat(putInMap.map).containsKey(TiActivityDelegate.SAVED_STATE_PRESENTER_ID);
        assertThat(putInMap.map.get(TiActivityDelegate.SAVED_STATE_PRESENTER_ID))
                .contains("TestPresenter");

        assertThat(presenter.isDestroyed()).isFalse();
        assertThat(mSavior.getPresenterCount()).isEqualTo(1);

        retainedPresenter[0] = presenter;
        delegate.onCreate_afterSuper(savedState);
    }

    @Test
    public void dontKeepActivitiesFalse_finish() throws Exception {
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .build());

        final TiActivityDelegate<TiPresenter<TiView>, TiView> delegate = new TiActivityDelegateBuilder()
                .setPresenter(presenter)
                .setIsFinishing(true)
                .setIsChangingConfigurations(false)
                .setDontKeepActivitiesEnabled(false)
                .setSavior(mSavior)
                .build();

        final Bundle savedState = mock(Bundle.class);
        doFullLifecycleAndDestroy(delegate, savedState);

        assertThat(presenter.isDestroyed()).isTrue();
        assertThat(mSavior.getPresenterCount()).isEqualTo(0);

        try {
            // presenter is destroyed and cannot be recreated
            delegate.onCreate_afterSuper(savedState);
            fail("did not throw");
        } catch (Exception e) {
            assertThat(e).hasMessageContaining("DESTROYED");
        }
    }

    @Test
    public void dontKeepActivitiesFalse_moveToBackground_moveToForeground()
            throws Exception {
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .build());

        final TestPresenter[] retainedPresenter = new TestPresenter[]{null};
        final TiActivityDelegate<TiPresenter<TiView>, TiView> delegate = new TiActivityDelegateBuilder()
                .setPresenter(presenter)
                .setIsFinishing(false)
                .setIsChangingConfigurations(false)
                .setDontKeepActivitiesEnabled(false)
                .setSavior(mSavior)
                .setRetainedPresenterProvider(new TiPresenterProvider<TiPresenter<TiView>>() {
                    @NonNull
                    @Override
                    public TiPresenter<TiView> providePresenter() {
                        return retainedPresenter[0];
                    }
                })
                .build();

        final Bundle savedState = mock(Bundle.class);
        delegate.onCreate_afterSuper(null);
        assertThat(mSavior.getPresenterCount()).isEqualTo(1);

        assertThat(delegate.getPresenter().isInitialized()).isTrue();

        delegate.onStart_afterSuper();

        assertThat(delegate.getPresenter().isDestroyed()).isFalse();

        delegate.onStop_beforeSuper();
        delegate.onStop_afterSuper();
        delegate.onSaveInstanceState_afterSuper(savedState);

        delegate.onStart_afterSuper();

        assertThat(presenter.isDestroyed()).isFalse();
        assertThat(mSavior.getPresenterCount()).isEqualTo(1);

        retainedPresenter[0] = presenter;
        delegate.onCreate_afterSuper(savedState);
    }

    @Test
    public void dontKeepActivitiesTrue_configurationChange() throws Exception {
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .build());

        final TiActivityDelegate<TiPresenter<TiView>, TiView> delegate = new TiActivityDelegateBuilder()
                .setPresenter(presenter)
                .setIsFinishing(false)
                .setIsChangingConfigurations(true)
                .setDontKeepActivitiesEnabled(true)
                .setSavior(mSavior)
                .build();

        final Bundle savedState = mock(Bundle.class);
        final PutInMapAnswer putInMap = putInMap();
        doAnswer(putInMap).when(savedState).putString(anyString(), anyString());

        doFullLifecycleAndDestroy(delegate, savedState);

        assertThat(putInMap.map).containsKey(TiActivityDelegate.SAVED_STATE_PRESENTER_ID);
        assertThat(putInMap.map.get(TiActivityDelegate.SAVED_STATE_PRESENTER_ID))
                .contains("TestPresenter");

        assertThat(presenter.isDestroyed()).isFalse();
        assertThat(mSavior.getPresenterCount()).isEqualTo(1);
    }

    @Test
    public void dontKeepActivitiesTrue_finish() throws Exception {
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .build());

        final TiActivityDelegate<TiPresenter<TiView>, TiView> delegate = new TiActivityDelegateBuilder()
                .setPresenter(presenter)
                .setIsFinishing(true)
                .setIsChangingConfigurations(false)
                .setDontKeepActivitiesEnabled(true)
                .setSavior(mSavior)
                .build();

        final Bundle savedState = mock(Bundle.class);
        doFullLifecycleAndDestroy(delegate, savedState);

        assertThat(presenter.isDestroyed()).isTrue();
        assertThat(mSavior.getPresenterCount()).isEqualTo(0);

        try {
            // presenter is destroyed and cannot be recreated
            delegate.onCreate_afterSuper(savedState);
            fail("did not throw");
        } catch (Exception e) {
            assertThat(e).hasMessageContaining("DESTROYED");
        }
    }

    @Test
    public void dontKeepActivitiesTrue_moveToBackground_moveToForeground()
            throws Exception {
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .build());

        final TiActivityDelegate<TiPresenter<TiView>, TiView> delegate = new TiActivityDelegateBuilder()
                .setPresenter(presenter)
                .setIsFinishing(false)
                .setIsChangingConfigurations(false)
                .setDontKeepActivitiesEnabled(true)
                .setSavior(mSavior)
                .build();

        final Bundle savedState = mock(Bundle.class);
        doFullLifecycleAndDestroy(delegate, savedState);

        assertThat(presenter.isDestroyed()).isFalse();
        assertThat(mSavior.getPresenterCount()).isEqualTo(1);
    }

    @Before
    public void setUp() throws Exception {
        mSavior = new TestPresenterSavior();
    }

    private void doFullLifecycleAndDestroy(final TiActivityDelegate<TiPresenter<TiView>, TiView> delegate,
            final Bundle savedState) {
        delegate.onCreate_afterSuper(null);

        assertThat(delegate.getPresenter().isInitialized()).isTrue();

        delegate.onStart_afterSuper();

        assertThat(delegate.getPresenter().isDestroyed()).isFalse();

        delegate.onStop_beforeSuper();
        delegate.onStop_afterSuper();
        delegate.onSaveInstanceState_afterSuper(savedState);
        delegate.onDestroy_afterSuper();
    }

    @NonNull
    private PutInMapAnswer putInMap() {
        return new PutInMapAnswer();
    }
}
