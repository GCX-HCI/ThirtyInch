package net.grandcentrix.thirtyinch.sample.fragmentlifecycle;

import net.grandcentrix.thirtyinch.TiFragment;
import net.grandcentrix.thirtyinch.sample.R;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.BackstackReader;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.TextView;

import java.util.UUID;

import rx.subjects.PublishSubject;

import static net.grandcentrix.thirtyinch.sample.fragmentlifecycle.FragmentLifecycleActivity.fragmentLifecycleActivityInstanceCount;

public abstract class TestFragment
        extends TiFragment<TestPresenter, TestPresenter.TestView>
        implements TestPresenter.TestView {

    static int testFragmentInstanceCount = -1;

    private final String TAG = this.getClass().getSimpleName()
            + "@" + Integer.toHexString(this.hashCode());

    private final Object instanceData = new Object() {
        @Override
        protected void finalize() throws Throwable {
            super.finalize();
            Log.v(getFragmentTag(), "FINALIZE");
        }
    };

    private int instanceNum = Integer.MIN_VALUE;

    private PublishSubject<Boolean> mAddedState = PublishSubject.create();

    private PublishSubject<Boolean> mDetachedState = PublishSubject.create();

    private PublishSubject<Boolean> mInBackstackState = PublishSubject.create();

    private PublishSubject<Boolean> mIsActivityChangingConfigState = PublishSubject.create();

    private PublishSubject<Boolean> mIsActivityFinishingState = PublishSubject.create();

    private PublishSubject<Boolean> mRemovingState = PublishSubject.create();

    private String mUuid;

    public TestFragment() {
        Log.v("FragmentManager", this + " constructor called");

        testFragmentInstanceCount++;
        instanceNum = testFragmentInstanceCount;
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.v(getFragmentTag(), "onActivityCreated");
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v(getFragmentTag(), "onActivityResult");
    }

    @Override
    public void onAttach(final Context context) {
        mAddedState.startWith(false).distinctUntilChanged().skip(1).subscribe(added -> {
            Log.d(getFragmentTag(), "fragment" + instanceNum + ".setAdded(" + added + ")");
        });
        mDetachedState.startWith(false).distinctUntilChanged().skip(1).subscribe(detached -> {
            Log.d(getFragmentTag(), "fragment" + instanceNum + ".setDetached(" + detached + ")");
        });
        mRemovingState.startWith(false).distinctUntilChanged().skip(1).subscribe(removing -> {
            Log.d(getFragmentTag(), "fragment" + instanceNum + ".setRemoving(" + removing + ")");
        });
        mInBackstackState.startWith(false).distinctUntilChanged().skip(1).subscribe(inBackstack -> {
            Log.d(getFragmentTag(),
                    "fragment" + instanceNum + ".setInBackstack(" + inBackstack + ")");
        });

        mIsActivityChangingConfigState.startWith(false).distinctUntilChanged().skip(1)
                .subscribe(changing -> {
                    Log.d(getFragmentTag(),
                            "hostingActivity" + fragmentLifecycleActivityInstanceCount + ""
                                    + ".setChangingConfiguration(" + changing + ");");
                });
        mIsActivityFinishingState.startWith(false).distinctUntilChanged().skip(1)
                .subscribe(finishing -> {
                    Log.d(getFragmentTag(),
                            "hostingActivity" + fragmentLifecycleActivityInstanceCount + ""
                                    + ".setFinishing(" + finishing + ");");
                });

        printState();
        super.onAttach(context);
        Log.v(getFragmentTag(), "onAttach(" + context + ")");
        printState();
    }

    @Override
    public void onAttachFragment(final Fragment childFragment) {
        super.onAttachFragment(childFragment);
        printState();
        Log.v(getFragmentTag(), "onAttachFragment");
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.v(getFragmentTag(), "onConfigurationChanged");
    }

    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        Log.v(getFragmentTag(), "onContextItemSelected");
        return super.onContextItemSelected(item);
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        printState();
        super.onCreate(savedInstanceState);
        Log.v(getFragmentTag(), "onCreate(" + savedInstanceState + ")");
        printState();
        if (savedInstanceState != null) {
            mUuid = savedInstanceState.getString("uuid");
            Log.v("FragmentManager", "RESTORED " + mUuid);
        }
        if (mUuid == null) {
            mUuid = UUID.randomUUID().toString();
            Log.v("FragmentManager", "CREATED " + mUuid);
        }

        Log.v(getFragmentTag(), "// TODO generate a new TiFragmentDelegate instance");
        Log.v(TAG, "final TiFragmentDelegate<TiPresenter<TiView>, TiView> "
                + "fragment" + instanceNum + "\n"
                + "                = new TiFragmentDelegateBuilder()...\n");

        if (savedInstanceState == null) {
            Log.d(getFragmentTag(), "fragment" + instanceNum + ".onCreate(null);");
        } else {
            Log.d(getFragmentTag(), "fragment" + instanceNum
                    + ".onCreate(savedInstanceState);");
        }

        Log.v(getFragmentTag(), "instance Data: " + instanceData);
    }

    @Override
    public Animation onCreateAnimation(final int transit, final boolean enter, final int nextAnim) {
        Log.v(getFragmentTag(), "onCreateAnimation");
        return super.onCreateAnimation(transit, enter, nextAnim);
    }

    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View v,
            final ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        Log.v(getFragmentTag(), "onCreateContextMenu");
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        Log.v(getFragmentTag(), "onCreateOptionsMenu");
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        printState();
        Log.v(getFragmentTag(),
                "onCreateView() called with: inflater = [" + inflater + "], container = ["
                        + container + "], savedInstanceState = [" + savedInstanceState + "]");
        if (savedInstanceState == null) {
            Log.d(getFragmentTag(), "fragment" + instanceNum
                    + ".onCreateView(inflater, null, null);");
        } else {
            Log.d(getFragmentTag(), "fragment" + instanceNum
                    + ".onCreateView(inflater, null, savedInstanceState);");
        }

        Log.v(getFragmentTag(), "instance Data: " + instanceData);

        return inflater.inflate(getLayoutResId(), container, false);
    }

    @Override
    public void onDestroy() {
        printState();
        super.onDestroy();
        Log.v(getFragmentTag(), "onDestroy");
        Log.d(getFragmentTag(), "fragment" + instanceNum + ".onDestroy();");
        printState();
        Log.v(getFragmentTag(), "instance Data: " + instanceData);
        Log.v("FragmentManager", "DESTROYED " + mUuid);
    }

    @Override
    public void onDestroyOptionsMenu() {
        super.onDestroyOptionsMenu();
        Log.v(getFragmentTag(), "onDestroyOptionsMenu");
    }

    @Override
    public void onDestroyView() {
        printState();
        super.onDestroyView();
        Log.v(getFragmentTag(), "onDestroyView");
        printState();

        Log.d(getFragmentTag(), "fragment" + instanceNum + ".onDestroyView();");

        Log.v(getFragmentTag(), "instance Data: " + instanceData);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        printState();
        Log.v(getFragmentTag(), "onDetach");
        printState();

        mAddedState.onCompleted();
        mDetachedState.onCompleted();
        mRemovingState.onCompleted();
        mIsActivityChangingConfigState.onCompleted();
        mIsActivityFinishingState.onCompleted();
    }

    @Override
    public void onHiddenChanged(final boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.v(getFragmentTag(), "onHiddenChanged");
    }

    @Override
    public void onInflate(final Context context, final AttributeSet attrs,
            final Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);
        Log.v(getFragmentTag(), "onInflate");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.v(getFragmentTag(), "onLowMemory");
        printState();
    }

    @Override
    public void onMultiWindowModeChanged(final boolean isInMultiWindowMode) {
        super.onMultiWindowModeChanged(isInMultiWindowMode);
        Log.v(getFragmentTag(), "onMultiWindowModeChanged");
        printState();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        Log.v(getFragmentTag(), "onOptionsItemSelected");
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onOptionsMenuClosed(final Menu menu) {
        super.onOptionsMenuClosed(menu);
        Log.v(getFragmentTag(), "onOptionsMenuClosed");
    }

    @Override
    public void onPause() {
        printState();
        super.onPause();
        Log.v(getFragmentTag(), "onPause()");
        printState();
    }

    @Override
    public void onPictureInPictureModeChanged(final boolean isInPictureInPictureMode) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode);
        Log.v(getFragmentTag(), "onPictureInPictureModeChanged");
        printState();
    }

    @Override
    public void onPrepareOptionsMenu(final Menu menu) {
        super.onPrepareOptionsMenu(menu);
        Log.v(getFragmentTag(), "onPrepareOptionsMenu");
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode,
            @NonNull final String[] permissions,
            @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.v(getFragmentTag(), "onRequestPermissionsResult");
        printState();
    }

    @Override
    public void onResume() {
        printState();
        super.onResume();
        Log.v(getFragmentTag(), "onResume");
        printState();
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        printState();
        Log.v(getFragmentTag(), "onSaveInstanceState");

        outState.putString("uuid", mUuid);

        Log.d(getFragmentTag(), "fragment" + instanceNum + ".onSaveInstanceState(outState);");
        printState();
    }

    @Override
    public void onStart() {
        printState();
        super.onStart();
        Log.v(getFragmentTag(), "onStart");
        Log.d(getFragmentTag(), "fragment" + instanceNum + ".onStart();");
        printState();
    }

    @Override
    public void onStop() {
        printState();
        super.onStop();
        Log.v(getFragmentTag(), "onStop");
        Log.d(getFragmentTag(), "fragment" + instanceNum + ".onStop();");
        printState();
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        printState();
        super.onViewCreated(view, savedInstanceState);
        Log.v(getFragmentTag(), "onViewCreated");
        printState();

        final TextView fragmentTag = (TextView) view.findViewById(R.id.sample_text);
        fragmentTag.setText(TAG);
    }

    @Override
    public void onViewStateRestored(@Nullable final Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Log.v(getFragmentTag(), "onViewStateRestored");
    }

    @NonNull
    @Override
    public TestPresenter providePresenter() {
        return new TestPresenter(getClass().getSimpleName());
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        Log.i("FragmentManager", "GCed " + this + ", uuid: " + this.mUuid);
    }

    @LayoutRes
    abstract int getLayoutResId();

    private String getFragmentTag() {
        return TAG;
    }

    private void printState() {
        mAddedState.onNext(isAdded());
        mDetachedState.onNext(isDetached());
        mRemovingState.onNext(isRemoving());
        mInBackstackState.onNext(BackstackReader.isInBackStack(this));

        final FragmentActivity activity = getActivity();
        if (activity != null) {
            mIsActivityFinishingState.onNext(activity.isFinishing());
            mIsActivityChangingConfigState.onNext(activity.isChangingConfigurations());
        }
    }
}
