/*
 * Copyright (C) 2017 grandcentrix GmbH
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.grandcentrix.thirtyinch.sample

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.jakewharton.rxbinding.view.RxView
import net.grandcentrix.thirtyinch.TiActivity
import net.grandcentrix.thirtyinch.logginginterceptor.LoggingInterceptor
import net.grandcentrix.thirtyinch.sample.fragmentlifecycle.FragmentLifecycleActivity
import net.grandcentrix.thirtyinch.sample.fragmentlifecycle.viewpager.LifecycleViewPagerActivity

class HelloWorldActivity : TiActivity<HelloWorldPresenter, HelloWorldView>(), HelloWorldView {

    private val button by lazy { findViewById<Button>(R.id.button) }
    private val output by lazy { findViewById<TextView>(R.id.output) }
    private val uptime by lazy { findViewById<TextView>(R.id.uptime) }

    init {
        addBindViewInterceptor(LoggingInterceptor())
    }

    override fun providePresenter() = HelloWorldPresenter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hello_world)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, SampleFragment())
                    .commit()
        }

        findViewById<View>(R.id.recreate).setOnClickListener { recreate() }
    }

    override fun onButtonClicked() = RxView.clicks(button)

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_hello_world, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.start_fragment_lifecycle_test -> {
                startActivity(Intent(this, FragmentLifecycleActivity::class.java))
                return true
            }
            R.id.start_viewpager_test -> {
                startActivity(Intent(this, LifecycleViewPagerActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun showPresenterUpTime(uptime: Long) {
        this.uptime.text = String.format("Presenter alive for %ss", uptime)
    }

    override fun showText(text: String) {
        output.text = text
    }
}
