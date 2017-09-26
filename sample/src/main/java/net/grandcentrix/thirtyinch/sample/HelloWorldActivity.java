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


import com.jakewharton.rxbinding.view.RxView;

import net.grandcentrix.thirtyinch.TiPresenterBinder;
import net.grandcentrix.thirtyinch.TiPresenterBinders;
import net.grandcentrix.thirtyinch.logginginterceptor.LoggingInterceptor;
import net.grandcentrix.thirtyinch.sample.fragmentlifecycle.FragmentLifecycleActivity;
import net.grandcentrix.thirtyinch.sample.fragmentlifecycle.viewpager.LifecycleViewPagerActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import rx.Observable;

public class HelloWorldActivity extends AppCompatActivity implements HelloWorldView {

    private Button mButton;

    private TextView mOutput;

    private HelloWorldPresenter mPresenter;

    private TextView mUptime;

    @Override
    public Observable<Void> onButtonClicked() {
        return RxView.clicks(mButton);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_hello_world, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.start_fragment_lifecycle_test:
                startActivity(new Intent(this, FragmentLifecycleActivity.class));
                return true;
            case R.id.start_viewpager_test:
                startActivity(new Intent(this, LifecycleViewPagerActivity.class));
                return true;
        }
        return false;
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
        final TiPresenterBinder<HelloWorldPresenter, HelloWorldView> binder = TiPresenterBinders
                .attachPresenter(this, savedInstanceState, () -> new HelloWorldPresenter());
        binder.addBindViewInterceptor(new LoggingInterceptor());
        mPresenter = binder.getPresenter();

        setContentView(R.layout.activity_hello_world);

        mButton = findViewById(R.id.button);
        mOutput = findViewById(R.id.output);
        mUptime = findViewById(R.id.uptime);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new SampleFragment())
                    .commit();
        }

        findViewById(R.id.recreate).setOnClickListener(v -> recreate());
    }

}
