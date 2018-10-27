package net.grandcentrix.thirtyinch.sample.fragmentlifecycle.viewpager

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.View
import net.grandcentrix.thirtyinch.sample.fragmentlifecycle.TestFragmentA

class LifecycleViewPagerActivity : AppCompatActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewPager = ViewPager(this)
        viewPager.id = View.generateViewId()
        setContentView(viewPager)

        viewPager.adapter = PagerAdapter(supportFragmentManager)
    }
}

private class PagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getCount(): Int = 4

    override fun getItem(position: Int): Fragment = TestFragmentA()
}

