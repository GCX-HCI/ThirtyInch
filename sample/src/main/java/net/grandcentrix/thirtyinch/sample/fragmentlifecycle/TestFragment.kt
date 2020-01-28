package net.grandcentrix.thirtyinch.sample.fragmentlifecycle

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.util.AttributeSet
import android.util.Log
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import net.grandcentrix.thirtyinch.TiConfiguration
import net.grandcentrix.thirtyinch.TiFragment
import net.grandcentrix.thirtyinch.sample.R
import net.grandcentrix.thirtyinch.sample.fragmentlifecycle.FragmentLifecycleActivity.Companion.fragmentLifecycleActivityInstanceCount
import net.grandcentrix.thirtyinch.util.BackstackReader
import rx.subjects.PublishSubject
import java.util.UUID

class TestFragmentA : TestFragment() {

    override val layoutResId: Int = R.layout.fragment_a_test
}

class TestFragmentB : TestFragment() {

    override val layoutResId: Int = R.layout.fragment_b_test
}

abstract class TestFragment : TiFragment<TestPresenter, TestPresenter.TestView>(), TestPresenter.TestView {

    companion object {

        const val RETAIN_PRESENTER = "retain"

        internal var testFragmentInstanceCount = -1
    }

    private val TAG = "${this.javaClass.simpleName}@${Integer.toHexString(this.hashCode())}"

    private var instanceNum = Integer.MIN_VALUE

    private val addedState = PublishSubject.create<Boolean>()

    private val detachedState = PublishSubject.create<Boolean>()

    private val inBackStackState = PublishSubject.create<Boolean>()

    private val isActivityChangingConfigState = PublishSubject.create<Boolean>()

    private val isActivityFinishingState = PublishSubject.create<Boolean>()

    private val removingState = PublishSubject.create<Boolean>()

    private var uuid: String? = null

    @get:LayoutRes
    internal abstract val layoutResId: Int

    init {
        Log.v(TAG, this.toString() + " constructor called")

        testFragmentInstanceCount++
        instanceNum = testFragmentInstanceCount
    }

    override fun onAttach(context: Context) {
        addedState.startWith(false).distinctUntilChanged().skip(1)
                .subscribe { added -> Log.d(TAG, "fragment$instanceNum.setAdded($added)") }
        detachedState.startWith(false).distinctUntilChanged().skip(1)
                .subscribe { detached -> Log.d(TAG, "fragment$instanceNum.setDetached($detached)") }
        removingState.startWith(false).distinctUntilChanged().skip(1)
                .subscribe { removing -> Log.d(TAG, "fragment$instanceNum.setRemoving($removing)") }
        inBackStackState.startWith(false).distinctUntilChanged().skip(1)
                .subscribe { inBackstack -> Log.d(TAG, "fragment$instanceNum.setInBackstack($inBackstack)") }

        isActivityChangingConfigState.startWith(false).distinctUntilChanged().skip(1)
                .subscribe { changing ->
                    Log.d(TAG,
                            "hostingActivity$fragmentLifecycleActivityInstanceCount.setChangingConfiguration($changing);")
                }
        isActivityFinishingState.startWith(false).distinctUntilChanged().skip(1)
                .subscribe { finishing ->
                    Log.d(TAG, "hostingActivity$fragmentLifecycleActivityInstanceCount.setFinishing($finishing);")
                }

        printState()
        super.onAttach(context)
        Log.v(TAG, "onAttach($context)")
        printState()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        printState()
        super.onCreate(savedInstanceState)
        Log.v(TAG, "onCreate($savedInstanceState)")
        printState()
        if (savedInstanceState != null) {
            uuid = savedInstanceState.getString("uuid")
            Log.v(TAG, "RESTORED $uuid")
        }
        if (uuid == null) {
            uuid = UUID.randomUUID().toString()
            Log.v(TAG, "CREATED $uuid")
        }

        if (savedInstanceState == null) {
            Log.d(TAG, "fragment$instanceNum.onCreate(null);")
        } else {
            Log.d(TAG, "fragment$instanceNum.onCreate(savedInstanceState);")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        printState()
        Log.v(TAG,
                "onCreateView() called with: inflater = [$inflater], container = [$container], savedInstanceState = [$savedInstanceState]")
        if (savedInstanceState == null) {
            Log.d(TAG, "fragment$instanceNum.onCreateView(inflater, null, null);")
        } else {
            Log.d(TAG, "fragment$instanceNum.onCreateView(inflater, null, savedInstanceState);")
        }

        return inflater.inflate(layoutResId, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        printState()
        super.onViewCreated(view, savedInstanceState)
        Log.v(TAG, "onViewCreated")
        printState()

        val fragmentTag = view.findViewById<TextView>(R.id.sample_text)
        fragmentTag.text = TAG
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.v(TAG, "onActivityCreated")
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        Log.v(TAG, "onViewStateRestored")
    }

    override fun onStart() {
        printState()
        super.onStart()
        Log.d(TAG, "fragment$instanceNum.onStart();")
        printState()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.v(TAG, "onActivityResult")
    }

    override fun onResume() {
        printState()
        super.onResume()
        Log.v(TAG, "onResume")
        printState()
    }

    override fun onPause() {
        printState()
        super.onPause()
        Log.v(TAG, "onPause()")
        printState()
    }

    override fun onStop() {
        printState()
        super.onStop()
        Log.d(TAG, "fragment$instanceNum.onStop();")
        printState()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        printState()

        outState.putString("uuid", uuid)

        Log.d(TAG, "fragment$instanceNum.onSaveInstanceState(outState);")
        printState()
    }

    override fun onDestroyView() {
        printState()
        super.onDestroyView()
        Log.v(TAG, "onDestroyView")
        printState()

        Log.d(TAG, "fragment$instanceNum.onDestroyView();")
    }

    override fun onDestroy() {
        printState()
        super.onDestroy()
        Log.v(TAG, "onDestroy")
        Log.d(TAG, "fragment$instanceNum.onDestroy();")
        printState()
        Log.v("FragmentManager", "DESTROYED $uuid")
    }

    override fun onDetach() {
        super.onDetach()
        printState()
        Log.v(TAG, "onDetach")
        printState()

        addedState.onCompleted()
        detachedState.onCompleted()
        removingState.onCompleted()
        isActivityChangingConfigState.onCompleted()
        isActivityFinishingState.onCompleted()
    }

    override fun onRequestPermissionsResult(requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.v(TAG, "onRequestPermissionsResult")
        printState()
    }

    override fun onAttachFragment(childFragment: Fragment) {
        super.onAttachFragment(childFragment)
        printState()
        Log.v(TAG, "onAttachFragment")
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.v(TAG, "onConfigurationChanged")
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        Log.v(TAG, "onContextItemSelected")
        return super.onContextItemSelected(item)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        Log.v(TAG, "onCreateContextMenu")
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        Log.v(TAG, "onCreateOptionsMenu")
    }

    override fun onDestroyOptionsMenu() {
        super.onDestroyOptionsMenu()
        Log.v(TAG, "onDestroyOptionsMenu")
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        Log.v(TAG, "onHiddenChanged")
    }

    override fun onInflate(context: Context, attrs: AttributeSet, savedInstanceState: Bundle?) {
        super.onInflate(context, attrs, savedInstanceState)
        Log.v(TAG, "onInflate")
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Log.v(TAG, "onLowMemory")
        printState()
    }

    override fun onMultiWindowModeChanged(isInMultiWindowMode: Boolean) {
        super.onMultiWindowModeChanged(isInMultiWindowMode)
        Log.v(TAG, "onMultiWindowModeChanged")
        printState()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.v(TAG, "onOptionsItemSelected")
        return super.onOptionsItemSelected(item)
    }

    override fun onOptionsMenuClosed(menu: Menu) {
        super.onOptionsMenuClosed(menu)
        Log.v(TAG, "onOptionsMenuClosed")
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode)
        Log.v(TAG, "onPictureInPictureModeChanged")
        printState()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        Log.v(TAG, "onPrepareOptionsMenu")
    }

    override fun providePresenter(): TestPresenter {
        val retain = arguments?.getBoolean(RETAIN_PRESENTER, false) ?: false

        val config = TiConfiguration.Builder()
                .setRetainPresenterEnabled(retain)
                .build()

        val presenter = TestPresenter(config, javaClass.simpleName)
        Log.d(TAG, "created $presenter")
        Log.v(TAG, "retain presenter $retain, $presenter")

        return presenter
    }

    // Override finalize in kotlin
    // https://kotlinlang.org/docs/reference/java-interop.html#finalize
    @Suppress("unused")
    protected fun finalize() {
        Log.v(TAG, "GCed " + this + ", uuid: " + this.uuid)
    }

    private fun printState() {
        addedState.onNext(isAdded)
        detachedState.onNext(isDetached)
        removingState.onNext(isRemoving)
        inBackStackState.onNext(BackstackReader.isInBackStack(this))

        val activity = activity ?: return
        isActivityFinishingState.onNext(activity.isFinishing)
        isActivityChangingConfigState.onNext(activity.isChangingConfigurations)
    }
}
