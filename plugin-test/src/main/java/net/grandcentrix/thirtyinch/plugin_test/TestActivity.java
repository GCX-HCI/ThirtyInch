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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;
import com.pascalwelsch.compositeandroid.activity.CompositeActivity;
import net.grandcentrix.thirtyinch.TiLog;
import net.grandcentrix.thirtyinch.internal.TiPresenterProvider;
import net.grandcentrix.thirtyinch.plugin.TiActivityPlugin;

public class TestActivity extends CompositeActivity implements TestView {

    private TextView mText;

    public TestActivity() {
        addPlugin(new TiActivityPlugin<>(new TiPresenterProvider<TestPresenter>() {
            @NonNull
            @Override
            public TestPresenter providePresenter() {
                return new TestPresenter();
            }
        }));
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_test);
        mText = (TextView) findViewById(R.id.helloworld_text);

        if (savedInstanceState == null) {

            final TestFragment testFragment = new TestFragment();

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, testFragment)
                    .commit();
        }
    }

    @Override
    public void showText(final String s) {
        mText.setText(s);
    }

    static {
        TiLog.setLogger(new TiLog.Logger() {
            @Override
            public void log(int level, String tag, String msg) {
                Log.println(level, tag, msg);
            }
        });
    }
}
