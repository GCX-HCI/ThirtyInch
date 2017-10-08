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
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import android.os.Bundle;
import android.support.annotation.NonNull;
import java.util.HashMap;
import net.grandcentrix.thirtyinch.TiPresenter;
import org.junit.*;
import org.mockito.invocation.*;
import org.mockito.stubbing.*;

public class PresenterSaviorTest {

    private final HashMap<String, String> fakeBundle = new HashMap<>();

    private Bundle mSavedState;

    @Test
    public void activityAddedWithoutPresenters() throws Exception {

        final TestPresenterSavior savior = new TestPresenterSavior();
        final HostingActivity hostingActivity = new HostingActivity();

        // create lifecycle callbacks with different scope
        final TiPresenter presenter = new TiPresenter() {
        };
        final String id = savior.save(presenter, hostingActivity.getMockActivityInstance());
        assertThat(id).isNotNull();
        assertThat(savior.getPresenterCount()).isEqualTo(1);
        assertThat(savior.mScopes).hasSize(1);

        // some random Activity was created
        final HostingActivity hostingActivity2 = new HostingActivity();
        savior.mActivityInstanceObserver.onActivityCreated(
                hostingActivity2.getMockActivityInstance(), mSavedState);

        // no second scope was created
        assertThat(savior.mScopes).hasSize(1);
    }

    @Test
    public void cleanupAfterActivityFinish() throws Exception {
        final TestPresenterSavior savior = new TestPresenterSavior();

        final HostingActivity hostingActivity = new HostingActivity();

        // Given two presenters saved in savior
        final TiPresenter presenter = new TiPresenter() {
        };
        final TiPresenter presenter2 = new TiPresenter() {
        };

        // When both are saved in savior
        final String id = savior.save(presenter, hostingActivity.getMockActivityInstance());
        assertThat(savior.getPresenterCount()).isEqualTo(1);

        final String id2 = savior.save(presenter2, hostingActivity.getMockActivityInstance());

        // Then the ids are different
        assertThat(id2).isNotEqualTo(id);
        assertThat(savior.getPresenterCount()).isEqualTo(2);

        // and both share the same scope
        assertThat(savior.mScopes).hasSize(1);

        // When the Activity finishes
        hostingActivity.setFinishing(true);

        // fragments are in backstack and can't report Activity finish
        // ActivityLifecycleCallbacks observe activity finish
        savior.mActivityInstanceObserver.onActivityDestroyed(
                hostingActivity.getMockActivityInstance());

        assertThat(savior.getPresenterCount()).isEqualTo(0);
        assertThat(savior.mScopes).isEmpty();
    }

    @Test
    public void clearScopeWhenActivityFinishes() throws Exception {
        final TestPresenterSavior savior = new TestPresenterSavior();

        // Save two presenters in two activities in savior
        final HostingActivity hostingActivity = new HostingActivity();
        final TiPresenter presenter = new TiPresenter() {
        };
        final String id = savior.save(presenter, hostingActivity.getMockActivityInstance());
        assertThat(savior.getPresenterCount()).isEqualTo(1);
        assertThat(savior.mScopes).hasSize(1);

        // save second
        final HostingActivity hostingActivity2 = new HostingActivity();
        final TiPresenter presenter2 = new TiPresenter() {
        };
        final String id2 = savior.save(presenter2, hostingActivity2.getMockActivityInstance());
        assertThat(savior.getPresenterCount()).isEqualTo(2);
        assertThat(savior.mScopes).hasSize(2);

        // When removing the first
        savior.free(id, hostingActivity.getMockActivityInstance());
        assertThat(savior.getPresenterCount()).isEqualTo(1);

        // Then only 1 scope is left, the other got removed
        assertThat(savior.mScopes).hasSize(1);

        // When removing the second
        savior.free(id2, hostingActivity2.getMockActivityInstance());

        // Then no scopes are left
        assertThat(savior.getPresenterCount()).isEqualTo(0);
        assertThat(savior.mScopes).isEmpty();

        // the observer is also cleaned up
        assertThat(savior.mActivityInstanceObserver).isNull();
    }

    @Test
    public void detectFinishingActivity() throws Exception {
        final TestPresenterSavior savior = new TestPresenterSavior();

        final HostingActivity hostingActivity = new HostingActivity();
        final TiPresenter presenter = new TiPresenter() {
        };
        final String id = savior.save(presenter, hostingActivity.getMockActivityInstance());
        assertThat(savior.getPresenterCount()).isEqualTo(1);
        assertThat(id).isNotEmpty().isNotNull();

        hostingActivity.setFinishing(true);
        savior.mActivityInstanceObserver
                .onActivityDestroyed(hostingActivity.getMockActivityInstance());

        assertThat(savior.getPresenterCount()).isEqualTo(0);
        assertThat(savior.mScopes).isEmpty();
    }

    @Test
    public void freePresenter() throws Exception {

        final TestPresenterSavior savior = new TestPresenterSavior();

        final HostingActivity hostingActivity = new HostingActivity();
        final TiPresenter presenter = new TiPresenter() {
        };
        final String id = savior.save(presenter, hostingActivity.getMockActivityInstance());
        assertThat(savior.getPresenterCount()).isEqualTo(1);

        savior.free(id, hostingActivity.getMockActivityInstance());
        assertThat(savior.getPresenterCount()).isEqualTo(0);
    }

    @Test
    public void freePresenterSaveAgain() throws Exception {

        final TestPresenterSavior savior = new TestPresenterSavior();

        final HostingActivity hostingActivity = new HostingActivity();
        final TiPresenter presenter = new TiPresenter() {
        };
        final String id = savior.save(presenter, hostingActivity.getMockActivityInstance());
        assertThat(savior.getPresenterCount()).isEqualTo(1);

        savior.free(id, hostingActivity.getMockActivityInstance());
        assertThat(savior.getPresenterCount()).isEqualTo(0);

        final String id2 = savior.save(presenter, hostingActivity.getMockActivityInstance());
        assertThat(id2).isNotEqualTo(id);

        assertThat(savior.getPresenterCount()).isEqualTo(1);
    }

    @Test
    public void freePresenterTwice() throws Exception {

        final TestPresenterSavior savior = new TestPresenterSavior();

        final HostingActivity hostingActivity = new HostingActivity();
        final TiPresenter presenter = new TiPresenter() {
        };
        final String id = savior.save(presenter, hostingActivity.getMockActivityInstance());
        assertThat(savior.getPresenterCount()).isEqualTo(1);

        savior.free(id, hostingActivity.getMockActivityInstance());
        assertThat(savior.getPresenterCount()).isEqualTo(0);
        assertThat(savior.mScopes).isEmpty();

        // free again should do nothing
        savior.free(id, hostingActivity.getMockActivityInstance());
        assertThat(savior.getPresenterCount()).isEqualTo(0);
        assertThat(savior.mScopes).isEmpty();
    }

    @Test
    public void freePresenterWithDifferentActivity() throws Exception {

        final TestPresenterSavior savior = new TestPresenterSavior();

        final HostingActivity hostingActivity = new HostingActivity();
        final TiPresenter presenter = new TiPresenter() {
        };
        final String id = savior.save(presenter, hostingActivity.getMockActivityInstance());
        assertThat(savior.getPresenterCount()).isEqualTo(1);

        final HostingActivity hostingActivity2 = new HostingActivity();
        savior.free(id, hostingActivity2.getMockActivityInstance());
        assertThat(savior.getPresenterCount()).isEqualTo(1);
    }

    @Test
    public void ignoreWhenActivityChangesConfiguration() throws Exception {
        final TestPresenterSavior savior = new TestPresenterSavior();

        final HostingActivity hostingActivity = new HostingActivity();
        final TiPresenter presenter = new TiPresenter() {
        };
        final String id = savior.save(presenter, hostingActivity.getMockActivityInstance());
        assertThat(savior.getPresenterCount()).isEqualTo(1);
        assertThat(id).isNotEmpty().isNotNull();

        savior.mActivityInstanceObserver
                .onActivityDestroyed(hostingActivity.getMockActivityInstance());

        assertThat(savior.getPresenterCount()).isEqualTo(1);
    }

    @Test
    public void ignoreWhenActivityRecreates() throws Exception {
        final TestPresenterSavior savior = new TestPresenterSavior();

        final HostingActivity hostingActivity = new HostingActivity();
        final TiPresenter presenter = new TiPresenter() {
        };
        final String id = savior.save(presenter, hostingActivity.getMockActivityInstance());
        assertThat(savior.getPresenterCount()).isEqualTo(1);
        assertThat(id).isNotEmpty().isNotNull();

        savior.mActivityInstanceObserver
                .onActivityDestroyed(hostingActivity.getMockActivityInstance());

        assertThat(savior.getPresenterCount()).isEqualTo(1);
    }

    @Test
    public void recoverKeyWithNewActivity() throws Exception {
        final TestPresenterSavior savior = new TestPresenterSavior();

        final HostingActivity hostingActivity = new HostingActivity();
        final TiPresenter presenter = new TiPresenter() {
        };
        final String id = savior.save(presenter, hostingActivity.getMockActivityInstance());

        // Activity changes configuration
        savior.mActivityInstanceObserver.onActivitySaveInstanceState(
                hostingActivity.getMockActivityInstance(), mSavedState);
        final String scopeId = fakeBundle.get(ActivityInstanceObserver.TI_ACTIVITY_ID_KEY);
        assertThat(scopeId).isNotNull();
        savior.mActivityInstanceObserver
                .onActivityDestroyed(hostingActivity.getMockActivityInstance());
        assertThat(savior.getPresenterCount()).isEqualTo(1);

        // new Activity instance gets created
        final HostingActivity hostingActivity2 = new HostingActivity();
        savior.mActivityInstanceObserver.onActivityCreated(
                hostingActivity2.getMockActivityInstance(), mSavedState);

        // recover with new Activity
        final TiPresenter recover = savior.recover(id, hostingActivity2.getMockActivityInstance());
        assertThat(recover).isEqualTo(presenter);
    }

    @Test
    public void recoverUnsupportedHost() throws Exception {
        final TestPresenterSavior savior = new TestPresenterSavior();
        try {
            savior.recover("someRandomId", "not supported host");
            fail("did not throw");
        } catch (Throwable e) {
            assertThat(e).isInstanceOf(PresenterSavior.IllegalHostException.class)
                    .hasMessageContaining("String");
        }
    }

    @Test
    public void restoreFailWithDifferentActivity() throws Exception {

        final TestPresenterSavior savior = new TestPresenterSavior();

        final HostingActivity hostingActivity = new HostingActivity();
        final TiPresenter presenter = new TiPresenter() {
        };
        final String id = savior.save(presenter, hostingActivity.getMockActivityInstance());
        assertThat(savior.getPresenterCount()).isEqualTo(1);

        final HostingActivity hostingActivity2 = new HostingActivity();
        final TiPresenter recovered = savior
                .recover(id, hostingActivity2.getMockActivityInstance());
        assertThat(recovered).isNull();
        assertThat(savior.getPresenterCount()).isEqualTo(1);
    }

    @Test
    public void restoreFromSavior() throws Exception {

        final TestPresenterSavior savior = new TestPresenterSavior();

        final HostingActivity hostingActivity = new HostingActivity();
        final TiPresenter presenter = new TiPresenter() {
        };
        final String id = savior.save(presenter, hostingActivity.getMockActivityInstance());
        assertThat(savior.getPresenterCount()).isEqualTo(1);

        final TiPresenter recovered = savior.recover(id, hostingActivity.getMockActivityInstance());
        assertThat(recovered).isEqualTo(presenter);
        assertThat(savior.getPresenterCount()).isEqualTo(1);
    }

    @Test
    public void saveToSavior() throws Exception {

        final TestPresenterSavior savior = new TestPresenterSavior();

        final HostingActivity hostingActivity = new HostingActivity();
        final TiPresenter presenter = new TiPresenter() {
        };
        final String id = savior.save(presenter, hostingActivity.getMockActivityInstance());
        assertThat(savior.getPresenterCount()).isEqualTo(1);
        assertThat(id).isNotEmpty().isNotNull();
    }

    @Test
    public void saveUnsupportedHost() throws Exception {
        final TestPresenterSavior savior = new TestPresenterSavior();
        final TiPresenter presenter = new TiPresenter() {
        };
        try {
            savior.save(presenter, "not supported host");
            fail("did not throw");
        } catch (Throwable e) {
            assertThat(e).isInstanceOf(PresenterSavior.IllegalHostException.class)
                    .hasMessageContaining("String");
        }
    }

    @Before
    public void setUp() throws Exception {
        mSavedState = mock(Bundle.class);
        doAnswer(saveInMap()).when(mSavedState).putString(anyString(), anyString());
        doAnswer(getFromMap()).when(mSavedState).getString(anyString());
    }

    @NonNull
    private Answer getFromMap() {
        return new Answer() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                final Object[] args = invocation.getArguments();
                //noinspection RedundantCast
                return fakeBundle.get((String) args[0]);
            }
        };
    }

    @NonNull
    private Answer saveInMap() {
        return new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                final Object[] args = invocation.getArguments();
                fakeBundle.put((String) args[0], (String) args[1]);
                return null;
            }
        };
    }
}