package net.grandcentrix.thirtyinch.sample.fragmentlifecycle.viewpager;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
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
