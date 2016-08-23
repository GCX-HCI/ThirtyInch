/*
 * Copyright (C) 2015 grandcentrix GmbH
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

import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.TiConfiguration;
import net.grandcentrix.thirtyinch.TiView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class TiActivityDelegateTest {

    private TiActivityDelegate mDelegate;

    private boolean mIsActivityFinishing = false;

    private boolean mIsDontKeepActivitiesEnabled = false;

    private TiPresenter<TiView> mPresenter;

    private TiPresenter<TiView> mRetainedPresenter;

    @Before
    public void setUp() throws Exception {
        mDelegate = newDelegate();

    }

    @Test
    public void testDontKeepActivities_DontRecoverWithSavior() throws Exception {
        mIsDontKeepActivitiesEnabled = true;

        final TiPresenter firstPresenter = new TiPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(false)
                .build()) {
        };
        final TiPresenter secondPresenter = new TiPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(false)
                .build()) {
        };
        mPresenter = firstPresenter;

        mDelegate.onCreate_afterSuper(null);

        assertEquals(TiPresenter.State.CREATED_WITH_DETACHED_VIEW, mPresenter.getState());

        mDelegate.onDestroy_afterSuper();
        assertEquals(TiPresenter.State.DESTROYED, mPresenter.getState());

        // don't reuse the old presenter
        mPresenter = secondPresenter;
        mDelegate = newDelegate();
        mDelegate.onCreate_afterSuper(new Bundle());
        assertEquals(secondPresenter, mDelegate.getPresenter());
    }

    @Test
    public void testDontKeepActivities_RecoverWithSavior() throws Exception {
        mIsDontKeepActivitiesEnabled = true;

        final TiPresenter firstPresenter = new TiPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(true)
                .build()) {
        };
        final TiPresenter secondPresenter = new TiPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(true)
                .build()) {
        };
        mPresenter = firstPresenter;
        mDelegate.onCreate_afterSuper(null);

        assertEquals(TiPresenter.State.CREATED_WITH_DETACHED_VIEW, mPresenter.getState());

        final Bundle bundle = new Bundle();
        mDelegate.onSaveInstanceState_afterSuper(bundle);

        mDelegate.onDestroy_afterSuper();
        assertEquals(TiPresenter.State.CREATED_WITH_DETACHED_VIEW, mPresenter.getState());

        mPresenter = secondPresenter;
        mDelegate = newDelegate();

        // check reuse of old presenter
        mDelegate.onCreate_afterSuper(bundle);
        assertEquals(firstPresenter, mDelegate.getPresenter());
    }

    @Test
    public void testRestorePresenter_withNonConfigurationInstance() throws Exception {
        final TiPresenter firstPresenter = new TiPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(false)
                .build()) {
        };
        final TiPresenter secondPresenter = new TiPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(false)
                .build()) {
        };
        mPresenter = firstPresenter;

        mDelegate.onCreate_afterSuper(null);

        assertEquals(TiPresenter.State.CREATED_WITH_DETACHED_VIEW, mPresenter.getState());

        final Bundle bundle = new Bundle();
        mDelegate.onSaveInstanceState_afterSuper(bundle);

        mDelegate.onDestroy_afterSuper();
        assertEquals(TiPresenter.State.CREATED_WITH_DETACHED_VIEW, mPresenter.getState());

        mPresenter = secondPresenter;
        mDelegate = newDelegate();

        // set presenter to retain
        mRetainedPresenter = firstPresenter;

        // check reuse of old presenter
        mDelegate.onCreate_afterSuper(bundle);
        assertEquals(firstPresenter, mDelegate.getPresenter());
    }

    @Test
    public void testRestorePresenter_withSavior_whichIsDisabled() throws Exception {
        final TiPresenter firstPresenter = new TiPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(false)
                .build()) {
        };
        final TiPresenter secondPresenter = new TiPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(false)
                .build()) {
        };
        mPresenter = firstPresenter;

        mDelegate.onCreate_afterSuper(null);

        assertEquals(TiPresenter.State.CREATED_WITH_DETACHED_VIEW, mPresenter.getState());

        final Bundle bundle = new Bundle();
        mDelegate.onSaveInstanceState_afterSuper(bundle);

        mDelegate.onDestroy_afterSuper();
        assertEquals(TiPresenter.State.CREATED_WITH_DETACHED_VIEW, mPresenter.getState());

        mPresenter = secondPresenter;

        // check reuse of old presenter
        mDelegate.onCreate_afterSuper(bundle);
        assertEquals(secondPresenter, mDelegate.getPresenter());

        // new one got created
        assertEquals(TiPresenter.State.CREATED_WITH_DETACHED_VIEW, mPresenter.getState());
    }

    @Test
    public void testRestorePresenter_withSavior() throws Exception {
        final TiPresenter firstPresenter = new TiPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(true)
                .build()) {
        };
        final TiPresenter secondPresenter = new TiPresenter(new TiConfiguration.Builder()
                .setUseStaticSaviorToRetain(true)
                .build()) {
        };
        mPresenter = firstPresenter;

        mDelegate.onCreate_afterSuper(null);

        assertEquals(TiPresenter.State.CREATED_WITH_DETACHED_VIEW, mPresenter.getState());

        final Bundle bundle = new Bundle();
        mDelegate.onSaveInstanceState_afterSuper(bundle);

        mDelegate.onDestroy_afterSuper();
        assertEquals(TiPresenter.State.CREATED_WITH_DETACHED_VIEW, mPresenter.getState());

        mPresenter = secondPresenter;

        // check reuse of old presenter
        mDelegate.onCreate_afterSuper(bundle);
        assertEquals(firstPresenter, mDelegate.getPresenter());

        // new one got NOT created
        assertEquals(TiPresenter.State.INITIALIZED, mPresenter.getState());
    }

    @NonNull
    private TiActivityDelegate newDelegate() {
        return new TiActivityDelegate<>(
                new DelegatedTiActivity<TiPresenter<TiView>>() {

                    @Nullable
                    @Override
                    public TiPresenter<TiView> getRetainedPresenter() {
                        return mRetainedPresenter;
                    }

                    @Override
                    public boolean isActivityFinishing() {
                        return mIsActivityFinishing;
                    }

                    @Override
                    public boolean isDontKeepActivitiesEnabled() {
                        return mIsDontKeepActivitiesEnabled;
                    }

                    @Override
                    public boolean postToMessageQueue(final Runnable action) {
                        action.run();
                        return true;
                    }
                },
                new TiViewProvider<TiView>() {
                    @NonNull
                    @Override
                    public TiView provideView() {
                        return new TiView() {
                        };
                    }
                },
                new TiPresenterProvider<TiPresenter<TiView>>() {
                    @NonNull
                    @Override
                    public TiPresenter<TiView> providePresenter() {
                        return mPresenter;
                    }
                },
                new TiPresenterLogger() {
                    @Override
                    public void logTiMessages(final String msg) {
                        System.out.println(msg);
                    }
                });
    }
}
