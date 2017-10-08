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

public class TiActivityDelegateTest extends AbstractPresenterDestroyTest {

    @Test
    public void provideDestroyedPresenter() throws Exception {
        final TestPresenter destroyedPresenter = new TestPresenter(TiConfiguration.DEFAULT);
        // make presenter destroyed
        destroyedPresenter.create();
        destroyedPresenter.destroy();

        final TestTiActivity activity = new TestTiActivity.Builder()
                .setPresenter(destroyedPresenter)
                .build();
        try {
            activity.onCreate(null);
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

        final TestTiActivity activity = new TestTiActivity.Builder()
                .setPresenter(reusedPresenter)
                .build();
        try {
            activity.onCreate(null);
            fail("did not throw");
        } catch (IllegalStateException e) {
            assertThat(e).hasMessageContaining("Presenter not in initialized state")
                    .hasMessageContaining("providePresenter");
        }
    }
}