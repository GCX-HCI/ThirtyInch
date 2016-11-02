/*
 * Copyright (C) 2016 grandcentrix GmbH
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
import android.support.annotation.Nullable;

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

    private class Delegate extends TiActivityDelegate<TestPresenter, TiView> {

        public Delegate(
                final TestPresenter presenter,
                final boolean isFinishing,
                final boolean isChangingConfigurations,
                final boolean dontdontKeepActivitiesEnabled,
                final TiPresenterProvider<TestPresenter> retainedInstance) {

            super(new DelegatedTiActivity<TestPresenter>() {
                @Nullable
                @Override
                public TestPresenter getRetainedPresenter() {
                    return retainedInstance.providePresenter();
                }

                @Override
                public boolean isActivityChangingConfigurations() {
                    return isChangingConfigurations;
                }

                @Override
                public boolean isActivityFinishing() {
                    return isFinishing;
                }

                @Override
                public boolean isDontKeepActivitiesEnabled() {
                    return dontdontKeepActivitiesEnabled;
                }

                @Override
                public boolean postToMessageQueue(Runnable runnable) {
                    runnable.run();
                    return true;
                }
            }, new TiViewProvider<TiView>() {
                @NonNull
                @Override
                public TiView provideView() {
                    return mock(TiView.class);
                }
            }, new TiPresenterProvider<TestPresenter>() {
                @NonNull
                @Override
                public TestPresenter providePresenter() {
                    return presenter;
                }
            }, new TiLoggingTagProvider() {
                @Override
                public String getLoggingTag() {
                    return "";
                }
            });
        }
    }

    private class TestPresenter extends TiPresenter<TiView> {

        public TestPresenter(TiConfiguration config) {
            super(config);
        }
    }

    @Test
    public void saviorFalse_dontKeepActivitiesFalse_configurationChange() throws Exception {
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(false)
                .build());

        final TiActivityDelegate<TestPresenter, TiView> delegate = new Delegate(presenter, false,
                true, false, new TiPresenterProvider<TestPresenter>() {
            @NonNull
            @Override
            public TestPresenter providePresenter() {
                return null;
            }
        });

        final Bundle savedState = mock(Bundle.class);
        final PutInMapAnswer putInMap = putInMap();
        doAnswer(putInMap).when(savedState).putString(anyString(), anyString());

        doFullLifecycleAndDestroy(delegate, savedState);

        assertThat(putInMap.map).containsKey(TiActivityDelegate.SAVED_STATE_PRESENTER_ID);
        assertThat(putInMap.map.get(TiActivityDelegate.SAVED_STATE_PRESENTER_ID)).isNull();

        assertThat(presenter.isDestroyed()).isFalse();
        assertThat(PresenterSaviorTestHelper.presenterCount()).isEqualTo(0);

        delegate.onCreate_afterSuper(savedState);
    }

    @Test
    public void saviorFalse_dontKeepActivitiesFalse_finish() throws Exception {
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(false)
                .build());

        final TiActivityDelegate<TestPresenter, TiView> delegate = new Delegate(presenter, true,
                false, false, new TiPresenterProvider<TestPresenter>() {
            @NonNull
            @Override
            public TestPresenter providePresenter() {
                return null;
            }
        });

        final Bundle savedState = mock(Bundle.class);
        doFullLifecycleAndDestroy(delegate, savedState);

        assertThat(presenter.isDestroyed()).isTrue();
        assertThat(PresenterSaviorTestHelper.presenterCount()).isEqualTo(0);

        try {
            // presenter is destroyed and cannot be recreated
            delegate.onCreate_afterSuper(savedState);
            fail("did not throw");
        } catch (Exception e) {
            assertThat(e).hasMessageContaining("destroyed");
        }
    }

    @Test
    public void saviorFalse_dontKeepActivitiesFalse_moveToBackground_moveToForeground()
            throws Exception {
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(false)
                .build());

        final TiActivityDelegate<TestPresenter, TiView> delegate = new Delegate(presenter, false,
                false, false, new TiPresenterProvider<TestPresenter>() {
            @NonNull
            @Override
            public TestPresenter providePresenter() {
                return null;
            }
        });

        final Bundle savedState = mock(Bundle.class);
        delegate.onCreate_afterSuper(null);
        // savior is disabled
        assertThat(PresenterSaviorTestHelper.presenterCount()).isEqualTo(0);

        assertThat(delegate.getPresenter().isInitialized()).isTrue();

        delegate.onStart_afterSuper();

        assertThat(delegate.getPresenter().isDestroyed()).isFalse();

        delegate.onStop_beforeSuper();
        delegate.onStop_afterSuper();
        delegate.onSaveInstanceState_afterSuper(savedState);

        delegate.onStart_afterSuper();

        assertThat(presenter.isDestroyed()).isFalse();
        assertThat(PresenterSaviorTestHelper.presenterCount()).isEqualTo(0);
    }

    @Test
    public void saviorFalse_dontKeepActivitiesTrue_configurationChange() throws Exception {
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(false)
                .build());

        final TiActivityDelegate<TestPresenter, TiView> delegate = new Delegate(presenter, false,
                true, true, new TiPresenterProvider<TestPresenter>() {
            @NonNull
            @Override
            public TestPresenter providePresenter() {
                return null;
            }
        });

        final Bundle savedState = mock(Bundle.class);
        final PutInMapAnswer putInMap = putInMap();
        doAnswer(putInMap).when(savedState).putString(anyString(), anyString());

        doFullLifecycleAndDestroy(delegate, savedState);

        assertThat(putInMap.map).containsKey(TiActivityDelegate.SAVED_STATE_PRESENTER_ID);
        assertThat(putInMap.map.get(TiActivityDelegate.SAVED_STATE_PRESENTER_ID)).isNull();

        assertThat(presenter.isDestroyed()).isFalse();
        assertThat(PresenterSaviorTestHelper.presenterCount()).isEqualTo(0);

        delegate.onCreate_afterSuper(null);
    }

    @Test
    public void saviorFalse_dontKeepActivitiesTrue_finish() throws Exception {
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(false)
                .build());

        final TiActivityDelegate<TestPresenter, TiView> delegate = new Delegate(presenter, true,
                false, true, new TiPresenterProvider<TestPresenter>() {
            @NonNull
            @Override
            public TestPresenter providePresenter() {
                return null;
            }
        });

        final Bundle savedState = mock(Bundle.class);
        doFullLifecycleAndDestroy(delegate, savedState);

        assertThat(presenter.isDestroyed()).isTrue();
        assertThat(PresenterSaviorTestHelper.presenterCount()).isEqualTo(0);

        try {
            // presenter is destroyed and cannot be recreated
            delegate.onCreate_afterSuper(null);
            fail("did not throw");
        } catch (Exception e) {
            assertThat(e).hasMessageContaining("destroyed");
        }
    }

    @Test
    public void saviorFalse_dontKeepActivitiesTrue_moveToBackground_moveToForeground()
            throws Exception {
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(false)
                .build());

        final TiActivityDelegate<TestPresenter, TiView> delegate = new Delegate(presenter, false,
                false, true, new TiPresenterProvider<TestPresenter>() {
            @NonNull
            @Override
            public TestPresenter providePresenter() {
                return null;
            }
        });

        final Bundle savedState = mock(Bundle.class);
        delegate.onCreate_afterSuper(null);
        // savior is disabled
        assertThat(PresenterSaviorTestHelper.presenterCount()).isEqualTo(0);

        assertThat(delegate.getPresenter().isInitialized()).isTrue();

        delegate.onStart_afterSuper();

        assertThat(delegate.getPresenter().isDestroyed()).isFalse();

        delegate.onStop_beforeSuper();
        delegate.onStop_afterSuper();
        delegate.onSaveInstanceState_afterSuper(savedState);

        delegate.onStart_afterSuper();

        assertThat(presenter.isDestroyed()).isFalse();
        assertThat(PresenterSaviorTestHelper.presenterCount()).isEqualTo(0);

        delegate.onCreate_afterSuper(null);
    }

    @Test
    public void saviorTrue_dontKeepActivitiesFalse_configurationChange() throws Exception {
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(true)
                .build());

        final TestPresenter[] retainedPresenter = new TestPresenter[]{null};
        final TiActivityDelegate<TestPresenter, TiView> delegate = new Delegate(presenter, false,
                true, false, new TiPresenterProvider<TestPresenter>() {
            @NonNull
            @Override
            public TestPresenter providePresenter() {
                return retainedPresenter[0];
            }
        });

        final Bundle savedState = mock(Bundle.class);
        final PutInMapAnswer putInMap = putInMap();
        doAnswer(putInMap).when(savedState).putString(anyString(), anyString());

        doFullLifecycleAndDestroy(delegate, savedState);

        assertThat(putInMap.map).containsKey(TiActivityDelegate.SAVED_STATE_PRESENTER_ID);
        assertThat(putInMap.map.get(TiActivityDelegate.SAVED_STATE_PRESENTER_ID))
                .contains("TestPresenter");

        assertThat(presenter.isDestroyed()).isFalse();
        assertThat(PresenterSaviorTestHelper.presenterCount()).isEqualTo(1);

        retainedPresenter[0] = presenter;
        delegate.onCreate_afterSuper(savedState);
    }

    @Test
    public void saviorTrue_dontKeepActivitiesFalse_finish() throws Exception {
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(true)
                .build());

        final TiActivityDelegate<TestPresenter, TiView> delegate = new Delegate(presenter, true,
                false, false, new TiPresenterProvider<TestPresenter>() {
            @NonNull
            @Override
            public TestPresenter providePresenter() {
                return null;
            }
        });

        final Bundle savedState = mock(Bundle.class);
        doFullLifecycleAndDestroy(delegate, savedState);

        assertThat(presenter.isDestroyed()).isTrue();
        assertThat(PresenterSaviorTestHelper.presenterCount()).isEqualTo(0);

        try {
            // presenter is destroyed and cannot be recreated
            delegate.onCreate_afterSuper(savedState);
            fail("did not throw");
        } catch (Exception e) {
            assertThat(e).hasMessageContaining("destroyed");
        }
    }

    @Test
    public void saviorTrue_dontKeepActivitiesFalse_moveToBackground_moveToForeground()
            throws Exception {
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(true)
                .build());

        final TestPresenter[] retainedPresenter = new TestPresenter[]{null};
        final TiActivityDelegate<TestPresenter, TiView> delegate = new Delegate(presenter, false,
                false, false, new TiPresenterProvider<TestPresenter>() {
            @NonNull
            @Override
            public TestPresenter providePresenter() {
                return retainedPresenter[0];
            }
        });

        final Bundle savedState = mock(Bundle.class);
        delegate.onCreate_afterSuper(null);
        assertThat(PresenterSaviorTestHelper.presenterCount()).isEqualTo(1);

        assertThat(delegate.getPresenter().isInitialized()).isTrue();

        delegate.onStart_afterSuper();

        assertThat(delegate.getPresenter().isDestroyed()).isFalse();

        delegate.onStop_beforeSuper();
        delegate.onStop_afterSuper();
        delegate.onSaveInstanceState_afterSuper(savedState);

        delegate.onStart_afterSuper();

        assertThat(presenter.isDestroyed()).isFalse();
        assertThat(PresenterSaviorTestHelper.presenterCount()).isEqualTo(1);

        retainedPresenter[0] = presenter;
        delegate.onCreate_afterSuper(savedState);
    }

    @Test
    public void saviorTrue_dontKeepActivitiesTrue_configurationChange() throws Exception {
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(true)
                .build());

        final TiActivityDelegate<TestPresenter, TiView> delegate = new Delegate(presenter, false,
                true, true, new TiPresenterProvider<TestPresenter>() {
            @NonNull
            @Override
            public TestPresenter providePresenter() {
                return null;
            }
        });

        final Bundle savedState = mock(Bundle.class);
        final PutInMapAnswer putInMap = putInMap();
        doAnswer(putInMap).when(savedState).putString(anyString(), anyString());

        doFullLifecycleAndDestroy(delegate, savedState);

        assertThat(putInMap.map).containsKey(TiActivityDelegate.SAVED_STATE_PRESENTER_ID);
        assertThat(putInMap.map.get(TiActivityDelegate.SAVED_STATE_PRESENTER_ID))
                .contains("TestPresenter");

        assertThat(presenter.isDestroyed()).isFalse();
        assertThat(PresenterSaviorTestHelper.presenterCount()).isEqualTo(1);
    }

    @Test
    public void saviorTrue_dontKeepActivitiesTrue_finish() throws Exception {
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(true)
                .build());

        final TiActivityDelegate<TestPresenter, TiView> delegate = new Delegate(presenter, true,
                false, true, new TiPresenterProvider<TestPresenter>() {
            @NonNull
            @Override
            public TestPresenter providePresenter() {
                return null;
            }
        });

        final Bundle savedState = mock(Bundle.class);
        doFullLifecycleAndDestroy(delegate, savedState);

        assertThat(presenter.isDestroyed()).isTrue();
        assertThat(PresenterSaviorTestHelper.presenterCount()).isEqualTo(0);

        try {
            // presenter is destroyed and cannot be recreated
            delegate.onCreate_afterSuper(savedState);
            fail("did not throw");
        } catch (Exception e) {
            assertThat(e).hasMessageContaining("destroyed");
        }
    }

    @Test
    public void saviorTrue_dontKeepActivitiesTrue_moveToBackground_moveToForeground()
            throws Exception {
        final TestPresenter presenter = new TestPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(true)
                .build());

        final TiActivityDelegate<TestPresenter, TiView> delegate = new Delegate(presenter, false,
                false, true, new TiPresenterProvider<TestPresenter>() {
            @NonNull
            @Override
            public TestPresenter providePresenter() {
                return null;
            }
        });

        final Bundle savedState = mock(Bundle.class);
        doFullLifecycleAndDestroy(delegate, savedState);

        assertThat(presenter.isDestroyed()).isFalse();
        assertThat(PresenterSaviorTestHelper.presenterCount()).isEqualTo(1);
    }

    @Before
    public void setUp() throws Exception {
        PresenterSaviorTestHelper.clear();
    }

    private void doFullLifecycleAndDestroy(final TiActivityDelegate<TestPresenter, TiView> delegate,
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
