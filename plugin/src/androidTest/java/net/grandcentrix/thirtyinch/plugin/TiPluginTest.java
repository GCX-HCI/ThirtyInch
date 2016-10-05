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

package net.grandcentrix.thirtyinch.plugin;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertNotEquals;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class TiPluginTest {

    @Rule
    public ActivityTestRule<TestActivity> mActivityRule =
            new ActivityTestRule<>(TestActivity.class);

    @Test
    public void recreate() throws Throwable {
        final TestActivity first = mActivityRule.getActivity();

        Espresso.onView(withId(R.id.helloworld_text))
                .check(matches(isDisplayed()));

        final Instrumentation.ActivityMonitor activityMonitor = new Instrumentation.ActivityMonitor(
                TestActivity.class.getName(), null, false);
        InstrumentationRegistry.getInstrumentation().addMonitor(activityMonitor);

        assertNull(activityMonitor.getLastActivity());

        first.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                first.recreate();
            }
        });
        /*Context context = InstrumentationRegistry.getTargetContext();
        int orientation = context.getResources().getConfiguration().orientation;

        first.setRequestedOrientation((orientation == Configuration.ORIENTATION_PORTRAIT)
                ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);*/

        assertNotNull(activityMonitor.getLastActivity());
        final TestActivity second = (TestActivity) activityMonitor.waitForActivityWithTimeout(5000);
        assertNotNull(second);

        //final TestActivity second = mActivityRule.getActivity();

        assertEquals(2, activityMonitor.getHits());
        assertNotEquals(first, second);

        Espresso.onView(withId(R.id.helloworld_text))
                .check(matches(isDisplayed()));
    }
}
