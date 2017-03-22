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

import java.util.HashMap;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public abstract class TiFragmentPresenterDestroyTest {

    private class PutInMapAnswer implements Answer<Void> {

        final HashMap<String, String> map = new HashMap<>();

        @Override
        public Void answer(InvocationOnMock invocation) throws Throwable {
            final Object[] args = invocation.getArguments();
            map.put((String) args[0], (String) args[1]);
            return null;
        }
    }

    class TestPresenter extends TiPresenter<TiView> {

        TestPresenter(TiConfiguration config) {
            super(config);
        }
    }

    Bundle mSavedState;

    MockSavior mSavior;

    @Before
    public void setUp() throws Exception {
        mSavior = new MockSavior();
        mSavedState = mock(Bundle.class);
        final PutInMapAnswer putInMap = new PutInMapAnswer();
        doAnswer(putInMap).when(mSavedState).putString(anyString(), anyString());
    }

    @After
    public void tearDown() throws Exception {
        mSavior.clear();
        mSavedState = null;
    }
}
