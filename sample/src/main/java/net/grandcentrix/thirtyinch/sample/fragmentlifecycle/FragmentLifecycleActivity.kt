package net.grandcentrix.thirtyinch.sample.fragmentlifecycle

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import net.grandcentrix.thirtyinch.sample.R
import net.grandcentrix.thirtyinch.sample.util.isDontKeepActivitiesEnabled
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

class FragmentLifecycleActivity : AppCompatActivity() {

    private val TAG = "${this.javaClass.simpleName}@${Integer.toHexString(this.hashCode())}"

    private var switchAddToBackStack: SwitchCompat? = null

    private var switchRetainPresenterInstance: SwitchCompat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            //started for the first time, reset all counters
            fragmentLifecycleActivityInstanceCount = -1
            TestFragment.testFragmentInstanceCount = -1
        }

        fragmentLifecycleActivityInstanceCount++
        setContentView(R.layout.activity_fragment_lifecycle)
        FragmentManager.enableDebugLogging(true)
        Log.v(TAG, "onCreate of " + this)

        switchAddToBackStack = findViewById(R.id.switch_add_back_stack)
        switchRetainPresenterInstance = findViewById(R.id.switch_retain_presenter_instance)
        val textDontKeepActivities = findViewById<TextView>(R.id.text_dont_keep_activities)
        val keepActivitiesText = if (isDontKeepActivitiesEnabled()) {
            R.string.dont_keep_activities_enabled
        } else {
            R.string.dont_keep_activities_disabled
        }
        textDontKeepActivities.setText(keepActivitiesText)

        Log.v(TAG, "// A new Activity gets created by the Android Framework.")
        Log.v(TAG, "final HostingActivity hostingActivity" + fragmentLifecycleActivityInstanceCount
                + " = new HostingActivity();")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause()")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d(TAG, "onSaveInstanceState(Bundle)")
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.v(TAG, "onDestroy")

        Log.v(TAG, "// hostingActivity" + fragmentLifecycleActivityInstanceCount
                + " got destroyed.")
    }

    fun addFragmentA(view: View) {
        val fragment = TestFragmentA()
        Log.v(TAG, "adding FragmentA")
        addFragment(fragment)
    }

    fun addFragmentB(view: View) {
        val fragment = TestFragmentB()
        Log.v(TAG, "adding FragmentB")
        addFragment(fragment)
    }

    fun detachFragmentAndAddAgain(view: View) {
        val fragment = supportFragmentManager
                .findFragmentById(R.id.fragment_placeholder)
        if (fragment != null) {
            //remove fragment
            Log.v(TAG, "// When the Fragment is removed.")
            supportFragmentManager.beginTransaction().remove(fragment).commitNow()

            Log.v(TAG, "// When the Fragment get added again to the Activity.")
            //add after delay again. Don't use the same transaction
            Observable.just<Any>(null).delay(1, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { _ -> addFragment(fragment) }
        } else {
            Toast.makeText(this, "no fragment found", Toast.LENGTH_SHORT).show()
        }
    }

    override fun finish() {
        super.finish()
        Log.v(TAG, "// When the Activity gets finished")
    }

    fun finishActivity(view: View) {
        Log.v(TAG, "finishing Activity")
        finish()
    }

    override fun onBackPressed() {
        Log.v(TAG, "// When the back button gets pressed")
        val fragmentManager = supportFragmentManager
        if (fragmentManager.backStackEntryCount > 0) {
            Log.v(TAG, "// When the top most fragment gets popped")
            fragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }

    fun recreateActivity(view: View) {
        Log.v(TAG, "// And when the Activity is changing its configurations.")
        recreate()
    }

    fun removeFragmentA(view: View) {
        val fragment = supportFragmentManager
                .findFragmentById(R.id.fragment_placeholder)
        if (fragment is TestFragmentA) {
            Log.v(TAG, "remove FragmentA")
            supportFragmentManager.beginTransaction()
                    .remove(fragment)
                    .commitNow()
        }
    }

    private fun addFragment(fragment: Fragment?) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        val retain = switchRetainPresenterInstance?.isChecked == true
        if (retain) {
            Log.v(TAG, "retaining presenter")
        }
        if (fragment!!.arguments == null) {
            val bundle = Bundle()
            bundle.putBoolean(TestFragment.RETAIN_PRESENTER, retain)
            fragment.arguments = bundle
        } else {
            Log.v(TAG, "reusing fragment, not setting new arguments")
        }

        fragmentTransaction.replace(R.id.fragment_placeholder, fragment)
        if (switchAddToBackStack?.isChecked == true) {
            Log.v(TAG, "adding transaction to the back stack")
            fragmentTransaction.addToBackStack(null)
        }
        val backStackId = fragmentTransaction.commit()
        if (backStackId >= 0) {
            Log.v(TAG, "Back stack ID: " + backStackId.toString())
        }
    }

    companion object {

        internal var fragmentLifecycleActivityInstanceCount = -1
    }
}
