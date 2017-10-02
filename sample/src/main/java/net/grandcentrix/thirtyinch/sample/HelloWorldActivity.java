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


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.jakewharton.rxbinding.view.RxView;
import net.grandcentrix.thirtyinch.TiActivity;
import net.grandcentrix.thirtyinch.logginginterceptor.LoggingInterceptor;
import net.grandcentrix.thirtyinch.sample.fragmentlifecycle.FragmentLifecycleActivity;
import net.grandcentrix.thirtyinch.sample.fragmentlifecycle.viewpager.LifecycleViewPagerActivity;
import rx.Observable;

public class HelloWorldActivity extends TiActivity<HelloWorldPresenter, HelloWorldView>
        implements HelloWorldView {

    private Button mButton;

    private TextView mOutput;

    private TextView mUptime;

    public HelloWorldActivity() {
        addBindViewInterceptor(new LoggingInterceptor());
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

        findViewById(R.id.recreate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                recreate();
            }
        });
    }

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

}
