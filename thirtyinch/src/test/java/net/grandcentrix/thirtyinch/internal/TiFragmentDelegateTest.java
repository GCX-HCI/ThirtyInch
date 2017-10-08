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

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import java.util.concurrent.atomic.AtomicInteger;
import net.grandcentrix.thirtyinch.TiConfiguration;
import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.TiView;
import org.junit.*;

public class TiFragmentDelegateTest extends AbstractPresenterDestroyTest {

    @Test
    public void provideDestroyedPresenter() throws Exception {
        final TestPresenter destroyedPresenter = new TestPresenter(TiConfiguration.DEFAULT);
        // make presenter destroyed
        destroyedPresenter.create();
        destroyedPresenter.destroy();

        final TestTiFragment fragment = new TestTiFragment.Builder()
                .setPresenter(destroyedPresenter)
                .build();
        try {
            fragment.onCreate(null);
            fail("did not throw");
        } catch (IllegalStateException e) {
            assertThat(e).hasMessageContaining("Presenter not in initialized state")
                    .hasMessageContaining("providePresenter");
        }
    }

    @Test
    public void provideReusedPresenter() throws Exception {

        final TestPresenter reusedPresenter = new TestPresenter(TiConfiguration.DEFAULT);
        // create the instance once, the presenter is now not in INITIALIZED state
        reusedPresenter.create();

        final TestTiFragment fragment = new TestTiFragment.Builder()
                .setPresenter(reusedPresenter)
                .build();
        try {
            fragment.onCreate(null);
            fail("did not throw");
        } catch (IllegalStateException e) {
            assertThat(e).hasMessageContaining("Presenter not in initialized state")
                    .hasMessageContaining("providePresenter");
        }
    }

    @Test
    public void reuseSameFragmentCreateANewPresenterInstance() throws Exception {

        final AtomicInteger providePresenterCalls = new AtomicInteger(0);

        // Given a fragment which always return a new presenter instance in providePresenter()
        final TestTiFragment fragment = new TestTiFragment.Builder()
                .setPresenterProvider(new TiPresenterProvider<TiPresenter<TiView>>() {
                    @NonNull
                    @Override
                    public TiPresenter<TiView> providePresenter() {
                        providePresenterCalls.incrementAndGet();
                        return new TestPresenter(TiConfiguration.DEFAULT);
                    }
                })
                .setSavior(mSavior)
                .build();

        // When the Fragment is added to the Activity.
        fragment.onCreate(null);
        fragment.setAdded(true);
        fragment.onCreateView(mock(LayoutInflater.class), null, null);
        fragment.onStart();

        final TiPresenter<TiView> presenter1 = fragment.getPresenter();
        // Then the presenter is saved in the savior
        assertThat(fragment.getPresenter().isDestroyed()).isFalse();
        assertThat(mSavior.getPresenterCount()).isEqualTo(1);

        // And when the Fragment is removed from the Activity
        fragment.setAdded(false);
        fragment.setRemoving(true);
        fragment.onStop();
        fragment.onDestroyView();
        fragment.onDestroy();

        // Then providePresenter was called once
        assertThat(providePresenterCalls.intValue()).isEqualTo(1);

        // Then assert that the Presenter is destroyed and not saved in the savior
        assertThat(fragment.getPresenter().isDestroyed()).isTrue();
        assertThat(mSavior.getPresenterCount()).isEqualTo(0);

        // When the Fragment is added again to the Activity, again
        fragment.onCreate(null);
        fragment.setAdded(true);
        fragment.onCreateView(mock(LayoutInflater.class), null, null);
        fragment.onStart();

        // Then providePresenter was called again
        assertThat(providePresenterCalls.intValue()).isEqualTo(2);

        // Then the presenter is different
        final TiPresenter<TiView> presenter2 = fragment.getPresenter();
        assertThat(presenter2).isNotSameAs(presenter1);

    }
}