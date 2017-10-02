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


import static org.mockito.Mockito.*;

import android.app.Activity;
import android.app.Application;

/**
 * mutable object mocking the hosting activity and their state
 */
public class HostingActivity {

    private static Application mApplication = mock(Application.class);

    private final Activity mActivityMock;

    private boolean mIsFinishing;

    public HostingActivity() {
        mActivityMock = mock(Activity.class);
        when(mActivityMock.getApplication()).thenReturn(mApplication);
    }

    public Activity getMockActivityInstance() {
        // always update with latest data
        when(mActivityMock.isFinishing()).thenReturn(mIsFinishing);
        return mActivityMock;
    }

    public boolean isFinishing() {
        return mIsFinishing;
    }

    public void setFinishing(final boolean finishing) {
        mIsFinishing = finishing;
    }
}
