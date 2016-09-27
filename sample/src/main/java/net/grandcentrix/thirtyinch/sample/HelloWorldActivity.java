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

package net.grandcentrix.thirtyinch.sample;


import com.jakewharton.rxbinding.view.RxView;

import net.grandcentrix.thirtyinch.TiActivity;
import net.grandcentrix.thirtyinch.TiLog;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.Button;
import android.widget.TextView;

import rx.Observable;

public class HelloWorldActivity extends TiActivity<HelloWorldPresenter, HelloWorldView>
        implements HelloWorldView {

    private Button mButton;

    private TextView mOutput;

    private TextView mUptime;

    public HelloWorldActivity() {
        TiLog.setLogger(TiLog.LOGCAT);
    }

    @Override
    public Observable<Void> onButtonClicked() {
        return RxView.clicks(mButton);
    }

    @NonNull
    @Override
    public HelloWorldPresenter providePresenter() {
        return new HelloWorldPresenter();
    }

    @Override
    public void showPresenterUpTime(final Long uptime) {
        mUptime.setText(String.format("Presenter alive for %ss", uptime));
    }

    @Override
    public void showText(final String text) {
        mOutput.setText(text);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello_world);

        mButton = (Button) findViewById(R.id.button);
        mOutput = (TextView) findViewById(R.id.output);
        mUptime = (TextView) findViewById(R.id.uptime);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new SampleFragment())
                    .commit();
        }
    }
}
