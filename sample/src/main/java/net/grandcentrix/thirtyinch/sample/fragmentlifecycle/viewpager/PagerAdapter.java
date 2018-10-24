package net.grandcentrix.thirtyinch.sample.fragmentlifecycle.viewpager;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import net.grandcentrix.thirtyinch.sample.fragmentlifecycle.TestFragmentA;

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
