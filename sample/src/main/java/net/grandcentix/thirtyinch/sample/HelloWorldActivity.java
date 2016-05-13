package net.grandcentix.thirtyinch.sample;

import com.jakewharton.rxbinding.view.RxView;

import net.grandcentrix.thirtyinch.Presenter;
import net.grandcentrix.thirtyinch.android.ThirtyInchActivity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.Button;
import android.widget.TextView;

import rx.Observable;

public class HelloWorldActivity extends ThirtyInchActivity<HelloWorldView>
        implements HelloWorldView {

    private Button mButton;

    private TextView mOutput;

    private TextView mUptime;

    @Override
    public Observable<Void> onButtonClicked() {
        return RxView.clicks(mButton);
    }

    @Override
    public void showPresenterUpTime(final Long uptime) {
        mUptime.setText(String.format("Presenter alive fore %ss", uptime));
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
    }

    @NonNull
    @Override
    protected Presenter<HelloWorldView> providePresenter(
            @NonNull final Bundle activityIntentBundle) {
        return new HelloWorldPresenter();
    }

    @NonNull
    @Override
    protected HelloWorldView provideView() {
        return this;
    }
}
