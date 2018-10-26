package net.grandcentrix.thirtyinch.sample.fragmentlifecycle.viewpager

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import net.grandcentrix.thirtyinch.sample.fragmentlifecycle.TestFragmentA

internal class PagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getCount(): Int {
        return 4
    }

    override fun getItem(position: Int): Fragment {
        return TestFragmentA()
    }
}
