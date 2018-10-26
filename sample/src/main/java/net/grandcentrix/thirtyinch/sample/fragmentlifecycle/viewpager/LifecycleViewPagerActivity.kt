package net.grandcentrix.thirtyinch.sample.fragmentlifecycle.viewpager

import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.View

class LifecycleViewPagerActivity : AppCompatActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewPager = ViewPager(this)
        viewPager.id = View.generateViewId()
        setContentView(viewPager)

        viewPager.adapter = PagerAdapter(supportFragmentManager)
    }
}
