package net.grandcentrix.thirtyinch.sample.fragmentlifecycle;

import static net.grandcentrix.thirtyinch.sample.fragmentlifecycle.FragmentLifecycleActivity.fragmentLifecycleActivityInstanceCount;

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
import net.grandcentrix.thirtyinch.TiConfiguration;
import net.grandcentrix.thirtyinch.TiFragment;
import net.grandcentrix.thirtyinch.sample.R;
import rx.subjects.PublishSubject;

public abstract class TestFragment
        extends TiFragment<TestPresenter, TestPresenter.TestView>
        implements TestPresenter.TestView {

    public static final String RETAIN_PRESENTER = "retain";

    static int testFragmentInstanceCount = -1;

    private final String TAG = this.getClass().getSimpleName()
            + "@" + Integer.toHexString(this.hashCode());

    private int instanceNum = Integer.MIN_VALUE;

    private PublishSubject<Boolean> mAddedState = PublishSubject.create();

    private PublishSubject<Boolean> mDetachedState = PublishSubject.create();

    private PublishSubject<Boolean> mInBackstackState = PublishSubject.create();

    private PublishSubject<Boolean> mIsActivityChangingConfigState = PublishSubject.create();

    private PublishSubject<Boolean> mIsActivityFinishingState = PublishSubject.create();

    private PublishSubject<Boolean> mRemovingState = PublishSubject.create();

    private String mUuid;

    public TestFragment() {
        Log.v(TAG, this + " constructor called");

        testFragmentInstanceCount++;
        instanceNum = testFragmentInstanceCount;
    }

    @Override
    public void onAttach(final Context context) {
        mAddedState.startWith(false).distinctUntilChanged().skip(1).subscribe(added -> {
            Log.d(TAG, "fragment" + instanceNum + ".setAdded(" + added + ")");
        });
        mDetachedState.startWith(false).distinctUntilChanged().skip(1).subscribe(detached -> {
            Log.d(TAG, "fragment" + instanceNum + ".setDetached(" + detached + ")");
        });
        mRemovingState.startWith(false).distinctUntilChanged().skip(1).subscribe(removing -> {
            Log.d(TAG, "fragment" + instanceNum + ".setRemoving(" + removing + ")");
        });
        mInBackstackState.startWith(false).distinctUntilChanged().skip(1).subscribe(inBackstack -> {
            Log.d(TAG,
                    "fragment" + instanceNum + ".setInBackstack(" + inBackstack + ")");
        });

        mIsActivityChangingConfigState.startWith(false).distinctUntilChanged().skip(1)
                .subscribe(changing -> {
                    Log.d(TAG,
                            "hostingActivity" + fragmentLifecycleActivityInstanceCount + ""
                                    + ".setChangingConfiguration(" + changing + ");");
                });
        mIsActivityFinishingState.startWith(false).distinctUntilChanged().skip(1)
                .subscribe(finishing -> {
                    Log.d(TAG,
                            "hostingActivity" + fragmentLifecycleActivityInstanceCount + ""
                                    + ".setFinishing(" + finishing + ");");
                });

        printState();
        super.onAttach(context);
        Log.v(TAG, "onAttach(" + context + ")");
        printState();
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        printState();
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate(" + savedInstanceState + ")");
        printState();
        if (savedInstanceState != null) {
            mUuid = savedInstanceState.getString("uuid");
            Log.v(TAG, "RESTORED " + mUuid);
        }
        if (mUuid == null) {
            mUuid = UUID.randomUUID().toString();
            Log.v(TAG, "CREATED " + mUuid);
        }

        if (savedInstanceState == null) {
            Log.d(TAG, "fragment" + instanceNum + ".onCreate(null);");
        } else {
            Log.d(TAG, "fragment" + instanceNum
                    + ".onCreate(savedInstanceState);");
        }
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        printState();
        Log.v(TAG,
                "onCreateView() called with: inflater = [" + inflater + "], container = ["
                        + container + "], savedInstanceState = [" + savedInstanceState + "]");
        if (savedInstanceState == null) {
            Log.d(TAG, "fragment" + instanceNum
                    + ".onCreateView(inflater, null, null);");
        } else {
            Log.d(TAG, "fragment" + instanceNum
                    + ".onCreateView(inflater, null, savedInstanceState);");
        }

        return inflater.inflate(getLayoutResId(), container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        printState();
        super.onViewCreated(view, savedInstanceState);
        Log.v(TAG, "onViewCreated");
        printState();

        final TextView fragmentTag = (TextView) view.findViewById(R.id.sample_text);
        fragmentTag.setText(TAG);
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.v(TAG, "onActivityCreated");
    }

    @Override
    public void onViewStateRestored(@Nullable final Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Log.v(TAG, "onViewStateRestored");
    }

    @Override
    public void onStart() {
        printState();
        super.onStart();
        Log.d(TAG, "fragment" + instanceNum + ".onStart();");
        printState();
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v(TAG, "onActivityResult");
    }

    @Override
    public void onResume() {
        printState();
        super.onResume();
        Log.v(TAG, "onResume");
        printState();
    }

    @Override
    public void onPause() {
        printState();
        super.onPause();
        Log.v(TAG, "onPause()");
        printState();
    }

    @Override
    public void onStop() {
        printState();
        super.onStop();
        Log.d(TAG, "fragment" + instanceNum + ".onStop();");
        printState();
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        printState();

        outState.putString("uuid", mUuid);

        Log.d(TAG, "fragment" + instanceNum + ".onSaveInstanceState(outState);");
        printState();
    }

    @Override
    public void onDestroyView() {
        printState();
        super.onDestroyView();
        Log.v(TAG, "onDestroyView");
        printState();

        Log.d(TAG, "fragment" + instanceNum + ".onDestroyView();");
    }

    @Override
    public void onDestroy() {
        printState();
        super.onDestroy();
        Log.v(TAG, "onDestroy");
        Log.d(TAG, "fragment" + instanceNum + ".onDestroy();");
        printState();
        Log.v("FragmentManager", "DESTROYED " + mUuid);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        printState();
        Log.v(TAG, "onDetach");
        printState();

        mAddedState.onCompleted();
        mDetachedState.onCompleted();
        mRemovingState.onCompleted();
        mIsActivityChangingConfigState.onCompleted();
        mIsActivityFinishingState.onCompleted();
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode,
            @NonNull final String[] permissions,
            @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.v(TAG, "onRequestPermissionsResult");
        printState();
    }

    @Override
    public void onAttachFragment(final Fragment childFragment) {
        super.onAttachFragment(childFragment);
        printState();
        Log.v(TAG, "onAttachFragment");
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.v(TAG, "onConfigurationChanged");
    }

    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        Log.v(TAG, "onContextItemSelected");
        return super.onContextItemSelected(item);
    }

    @Override
    public Animation onCreateAnimation(final int transit, final boolean enter, final int nextAnim) {
        Log.v(TAG, "onCreateAnimation");
        return super.onCreateAnimation(transit, enter, nextAnim);
    }

    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View v,
            final ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        Log.v(TAG, "onCreateContextMenu");
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        Log.v(TAG, "onCreateOptionsMenu");
    }

    @Override
    public void onDestroyOptionsMenu() {
        super.onDestroyOptionsMenu();
        Log.v(TAG, "onDestroyOptionsMenu");
    }

    @Override
    public void onHiddenChanged(final boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.v(TAG, "onHiddenChanged");
    }

    @Override
    public void onInflate(final Context context, final AttributeSet attrs,
            final Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);
        Log.v(TAG, "onInflate");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.v(TAG, "onLowMemory");
        printState();
    }

    @Override
    public void onMultiWindowModeChanged(final boolean isInMultiWindowMode) {
        super.onMultiWindowModeChanged(isInMultiWindowMode);
        Log.v(TAG, "onMultiWindowModeChanged");
        printState();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        Log.v(TAG, "onOptionsItemSelected");
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onOptionsMenuClosed(final Menu menu) {
        super.onOptionsMenuClosed(menu);
        Log.v(TAG, "onOptionsMenuClosed");
    }

    @Override
    public void onPictureInPictureModeChanged(final boolean isInPictureInPictureMode) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode);
        Log.v(TAG, "onPictureInPictureModeChanged");
        printState();
    }

    @Override
    public void onPrepareOptionsMenu(final Menu menu) {
        super.onPrepareOptionsMenu(menu);
        Log.v(TAG, "onPrepareOptionsMenu");
    }

    @NonNull
    @Override
    public TestPresenter providePresenter() {
        boolean retain = false;
        if (getArguments() != null) {
            retain = getArguments().getBoolean(RETAIN_PRESENTER, false);
        }

        final TiConfiguration config = new TiConfiguration.Builder()
                .setRetainPresenterEnabled(retain)
                .build();

        final TestPresenter presenter = new TestPresenter(config, getClass().getSimpleName());
        Log.d(TAG, "created " + presenter);
        Log.v(TAG, "retain presenter " + retain + ", " + presenter);

        return presenter;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        Log.v(TAG, "GCed " + this + ", uuid: " + this.mUuid);
    }

    @LayoutRes
    abstract int getLayoutResId();

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
