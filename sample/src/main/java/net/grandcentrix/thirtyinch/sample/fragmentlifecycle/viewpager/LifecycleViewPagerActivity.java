package net.grandcentrix.thirtyinch.sample.fragmentlifecycle.viewpager;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class LifecycleViewPagerActivity extends AppCompatActivity {

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ViewPager viewPager = new ViewPager(this);
        viewPager.setId(View.generateViewId());
        setContentView(viewPager);

        viewPager.setAdapter(new PagerAdapter(getSupportFragmentManager()));
    }
}
