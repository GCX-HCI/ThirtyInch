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

    @Test
    public void saviorTrue_retainTrue_dontKeepActivitiesTrue_fragmentAttached_activityFinish() {

        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(true)
                .setRetainPresenterEnabled(true)
                .build());

        final TiFragmentDelegate<TiPresenter<TiView>, TiView> delegate
                = new TiFragmentDelegateBuilder()
                .setDontKeepActivitiesEnabled(true)
                .setIsAdded(true)
                .setIsFinishing(true)
                .setIsChangingConfigurations(false)
                .setPresenter(presenter)
                .build();

        final Bundle savedState = mock(Bundle.class);
        final TiFragmentPresenterDestroyTest.PutInMapAnswer putInMap = putInMap();
        doAnswer(putInMap).when(savedState).putString(anyString(), anyString());

        delegate.onCreate_afterSuper(null);

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
        assertThat(PresenterSaviorTestHelper.presenterCount()).isEqualTo(0);
    }

    @Before
    public void setUp() throws Exception {
        PresenterSaviorTestHelper.clear();
    }

    @NonNull
    private PutInMapAnswer putInMap() {
        return new PutInMapAnswer();
    }
}
