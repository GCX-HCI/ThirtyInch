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

package net.grandcentrix.thirtyinch.sample;

import net.grandcentrix.thirtyinch.TiActivity;
import net.grandcentrix.thirtyinch.screen.TiScreen;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class SampleScreen extends TiScreen<SamplePresenter, SampleView> implements SampleView {

    private TextView mSampleText;

    //TODO remove actiivty here, will leak!
    public SampleScreen(final TiActivity tiActivity) {
        super(tiActivity);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final Context context, @NonNull final ViewGroup container) {
        final ViewGroup view = (ViewGroup) LayoutInflater.from(context)
                .inflate(R.layout.fragment_sample, container, false);

        mSampleText = (TextView) view.findViewById(R.id.sample_text);

        return view;
    }

    @NonNull
    @Override
    public SamplePresenter providePresenter() {
        return new SamplePresenter();
    }

    @Override
    public void showText(final String s) {
        mSampleText.setText(s);
    }
}
