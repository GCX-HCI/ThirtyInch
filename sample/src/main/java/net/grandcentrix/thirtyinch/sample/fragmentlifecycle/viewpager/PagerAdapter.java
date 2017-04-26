package net.grandcentrix.thirtyinch.sample.fragmentlifecycle.viewpager;

import net.grandcentrix.thirtyinch.sample.fragmentlifecycle.TestFragmentA;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

class PagerAdapter extends FragmentPagerAdapter {

    public PagerAdapter(final FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public Fragment getItem(final int position) {
        return new TestFragmentA();
    }
}
