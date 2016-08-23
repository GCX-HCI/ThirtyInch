package net.grandcentrix.thirtyinch.sample;

import net.grandcentrix.thirtyinch.TiFragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class SampleFragment extends TiFragment<SamplePresenter, SampleView> implements SampleView {

    private TextView mSampleText;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        final ViewGroup view = (ViewGroup) inflater
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
