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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.pascalwelsch.compositeandroid.fragment.CompositeFragment;
import net.grandcentrix.thirtyinch.internal.TiPresenterProvider;
import net.grandcentrix.thirtyinch.plugin.TiFragmentPlugin;

public class TestFragment extends CompositeFragment implements TestView {

    private TextView mTextView;

    public TestFragment() {
        addPlugin(new TiFragmentPlugin<>(new TiPresenterProvider<TestPresenter>() {
            @NonNull
            @Override
            public TestPresenter providePresenter() {
                return new TestPresenter();
            }
        }));
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View rootView = inflater.inflate(R.layout.fragment_test, container, false);
        mTextView = (TextView) rootView.findViewById(R.id.fragment_helloworld_text);
        return rootView;
    }

    @Override
    public void showText(final String s) {
        mTextView.setText(s);
    }
}
