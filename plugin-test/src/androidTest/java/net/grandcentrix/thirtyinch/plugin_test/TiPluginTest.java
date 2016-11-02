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

package net.grandcentrix.thirtyinch.plugin_test;

import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.Instrumentation;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.assertSame;
import static net.grandcentrix.thirtyinch.plugin_test.TestUtils.rotateOrientation;
import static org.hamcrest.Matchers.allOf;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class TiPluginTest {

    /**
     * Tests the full Activity lifecycle. Guarantees every lifecycle method gets called
     */
    @Test
    public void recreate() throws Throwable {
        final Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();

        // register monitor to track activity startups
        final Instrumentation.ActivityMonitor activityMonitor =
                new Instrumentation.ActivityMonitor(TestActivity.class.getName(), null, false);
        instrumentation.addMonitor(activityMonitor);

        // start the activity for the first time
        final Intent intent = new Intent(instrumentation.getTargetContext(), TestActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        instrumentation.startActivitySync(intent);

        // get activity reference
        final TestActivity first = (TestActivity) activityMonitor.waitForActivityWithTimeout(5000);
        assertNotNull(first);

        // make sure the attached presenter filled the UI
        Espresso.onView(withId(R.id.helloworld_text))
                .check(matches(allOf(isDisplayed(), withText("Hello World 1"))));

        // restart the activity
        rotateOrientation(first);

        // the monitor get's hit when onDestroy gets called for the first time. It's the old
        // activity reference
        final TestActivity destroyedActivity =
                (TestActivity) activityMonitor.waitForActivityWithTimeout(5000);
        // make sure it's the previously started activity, and ignore it
        assertSame(destroyedActivity, first);

        // next hit is the recreated activity
        final TestActivity second = (TestActivity) activityMonitor.waitForActivityWithTimeout(5000);
        assertNotNull(second);
        // is has to be a different Activity object
        assertNotSame(first, second);

        // assert the activity was bound to the presenter. The presenter should update the UI
        // correctly
        Espresso.onView(withId(R.id.helloworld_text))
                .check(matches(allOf(isDisplayed(), withText("Hello World 2"))));
    }
}
