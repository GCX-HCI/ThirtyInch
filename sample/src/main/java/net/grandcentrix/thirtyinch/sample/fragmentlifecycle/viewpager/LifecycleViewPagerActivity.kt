package net.grandcentrix.thirtyinch.sample.fragmentlifecycle.viewpager

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
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

