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

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import android.os.Bundle;
import android.support.annotation.NonNull;
import java.util.HashMap;
import net.grandcentrix.thirtyinch.TiConfiguration;
import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.TiView;
import org.junit.*;
import org.mockito.invocation.*;
import org.mockito.stubbing.*;

public abstract class AbstractPresenterDestroyTest {

    class TestPresenter extends TiPresenter<TiView> {

        TestPresenter(TiConfiguration config) {
            super(config);
        }
    }

    Bundle mActivitySavedState;

    Bundle mFragmentSavedState;

    TestPresenterSavior mSavior;

    private final HashMap<String, String> activityHostBundle = new HashMap<>();

    private final HashMap<String, String> fakeBundle = new HashMap<>();

    @Before
    public void setUp() throws Exception {
        mSavior = new TestPresenterSavior();
        mFragmentSavedState = mock(Bundle.class);
        doAnswer(saveInMap(fakeBundle)).when(mFragmentSavedState).putString(anyString(), anyString());
        doAnswer(getFromMap(fakeBundle)).when(mFragmentSavedState).getString(anyString());

        mActivitySavedState = mock(Bundle.class);
        doAnswer(saveInMap(activityHostBundle)).when(mActivitySavedState)
                .putString(anyString(), anyString());
        doAnswer(getFromMap(activityHostBundle)).when(mActivitySavedState).getString(anyString());
    }

    @After
    public void tearDown() throws Exception {
        mFragmentSavedState = null;
    }

    @NonNull
    private Answer getFromMap(final HashMap<String, String> store) {
        return new Answer() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                final Object[] args = invocation.getArguments();
                //noinspection RedundantCast
                return store.get((String) args[0]);
            }
        };
    }

    @NonNull
    private Answer saveInMap(final HashMap<String, String> store) {
        return new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                final Object[] args = invocation.getArguments();
                store.put((String) args[0], (String) args[1]);
                return null;
            }
        };
    }
}
