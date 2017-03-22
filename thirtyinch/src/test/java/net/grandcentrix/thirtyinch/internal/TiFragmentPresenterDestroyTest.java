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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.HashMap;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public abstract class TiFragmentPresenterDestroyTest {

    class TestPresenter extends TiPresenter<TiView> {

        TestPresenter(TiConfiguration config) {
            super(config);
        }
    }

    Bundle mSavedState;

    MockSavior mSavior;

    private final HashMap<String, String> fakeBundle = new HashMap<>();

    @Before
    public void setUp() throws Exception {
        mSavior = new MockSavior();
        mSavedState = mock(Bundle.class);
        doAnswer(saveInMap()).when(mSavedState).putString(anyString(), anyString());
        doAnswer(getFromMap()).when(mSavedState).getString(anyString());
    }

    @After
    public void tearDown() throws Exception {
        mSavior.clear();
        mSavedState = null;
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
