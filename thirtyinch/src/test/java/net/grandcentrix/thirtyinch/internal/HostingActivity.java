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


/**
 * mutable object mocking the hosting activity and their state
 */
public class HostingActivity {

    private boolean mIsChangingConfiguration;

    public boolean isChangingConfiguration() {
        return mIsChangingConfiguration;
    }

    public boolean isFinishing() {
        return mIsFinishing;
    }

    private boolean mIsFinishing;

    public HostingActivity(final boolean isFinishing, final boolean isChangingConfiguration) {
        mIsFinishing = isFinishing;
        mIsChangingConfiguration = isChangingConfiguration;
    }

    public HostingActivity() {
        resetToDefault();
    }

    /**
     * like when the Activity gets recreated by the Android Framework.
     * Resets to default values
     */
    public void recreateInstance() {
        resetToDefault();
    }

    public void resetToDefault() {
        mIsChangingConfiguration = false;
        mIsFinishing = false;
    }

    public void setChangingConfiguration(final boolean changingConfiguration) {
        mIsChangingConfiguration = changingConfiguration;
    }

    public void setFinishing(final boolean finishing) {
        mIsFinishing = finishing;
    }
}
