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

package net.grandcentrix.thirtyinch.plugin_test;

import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static net.grandcentrix.thirtyinch.plugin_test.TestUtils.rotateOrientation;
import static org.hamcrest.Matchers.*;

import android.app.Instrumentation;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;
import org.junit.*;
import org.junit.runner.*;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class TiPluginTest {

    @Test
    public void startTestActivity() throws Exception {
        final Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();

        // start the activity for the first time
        final TestActivity activity = startTestActivity(instrumentation);

        // make sure the attached presenter filled the UI
        Espresso.onView(withId(R.id.helloworld_text))
                .check(matches(allOf(isDisplayed(), withText("Hello World 1"))));

        Espresso.onView(withId(R.id.fragment_helloworld_text))
                .check(matches(allOf(isDisplayed(), withText("Hello World 1"))));

        activity.finish();
    }

    /**
     * Tests the full Activity lifecycle. Guarantees every lifecycle method gets called
     */
    @Test
    public void testFullLifecycleIncludingConfigurationChange() throws Throwable {
        final Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();

        // start the activity for the first time
        final TestActivity activity = startTestActivity(instrumentation);

        // make sure the attached presenter filled the UI
        Espresso.onView(withId(R.id.helloworld_text))
                .check(matches(allOf(isDisplayed(), withText("Hello World 1"))));
        Espresso.onView(withId(R.id.fragment_helloworld_text))
                .check(matches(allOf(isDisplayed(), withText("Hello World 1"))));

        // restart the activity
        rotateOrientation(activity);

        // assert the activity was bound to the presenter. The presenter should update the UI
        // correctly
        Espresso.onView(withId(R.id.helloworld_text))
                .check(matches(allOf(isDisplayed(), withText("Hello World 2"))));
        Espresso.onView(withId(R.id.fragment_helloworld_text))
                .check(matches(allOf(isDisplayed(), withText("Hello World 2"))));

        activity.finish();
    }

    private TestActivity startTestActivity(final Instrumentation instrumentation) {
        final Intent intent = new Intent(instrumentation.getTargetContext(), TestActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return (TestActivity) instrumentation.startActivitySync(intent);
    }
}
