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

package net.grandcentrix.thirtyinch.serialize.icepick;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import net.grandcentrix.thirtyinch.TiConfiguration;
import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.TiView;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class IcepickSerializerTest {

    @Test
    public void testSerializeIcepick() throws Exception {
        TestPresenter presenter = new TestPresenter(InstrumentationRegistry.getContext());
        final String id = presenter.getId();

        presenter.mValue = 5;
        presenter.persist();
        ((IcepickPresenterSerializer) presenter.getConfig().getPresenterSerializer()).waitForPendingTasks();

        IcepickPresenterSerializer serializer = new IcepickPresenterSerializer(InstrumentationRegistry.getContext());

        presenter = new TestPresenter(InstrumentationRegistry.getContext());
        assertEquals(0, presenter.mValue);

        presenter = serializer.deserialize(presenter, id);
        assertEquals(5, presenter.mValue);
    }

    public interface TestView extends TiView {
    }

    public static final class TestPresenter extends TiPresenter<TestView> {

        @icepick.State
        int mValue = 0;

        public TestPresenter(Context context) {
            super(new TiConfiguration.Builder().setPresenterSerializer(new IcepickPresenterSerializer(context)).build());
        }
    }
}
