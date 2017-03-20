package net.grandcentrix.thirtyinch.sample.fragmentlifecycle;

import net.grandcentrix.thirtyinch.sample.R;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

public abstract class TestFragment extends Fragment {

    private final String TAG = this.getClass().getSimpleName()
            + "@" + Integer.toHexString(this.hashCode());

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(getFragmentTag(), "onViewCreated");
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(getFragmentTag(), "onActivityResult");
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        Log.d(getFragmentTag(), "onAttach");
    }

    @Override
    public void onAttachFragment(final Fragment childFragment) {
        super.onAttachFragment(childFragment);
        Log.d(getFragmentTag(), "onAttachFragment");
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(getFragmentTag(), "onConfigurationChanged");
    }

    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        Log.d(getFragmentTag(), "onContextItemSelected");
        return super.onContextItemSelected(item);
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(getFragmentTag(), "onCreate");

        // TODO: rberghegger 17.03.17 find a smarter way to log what would be called on the delegate
        if (savedInstanceState == null) {
            Log.v(getFragmentTag(), "mDelegate.onCreate_afterSuper(null);");
        } else {
            Log.v(getFragmentTag(), "mDelegate.onCreate_afterSuper(savedInstanceState);");
        }
    }

    @Override
    public Animation onCreateAnimation(final int transit, final boolean enter, final int nextAnim) {
        Log.d(getFragmentTag(), "onCreateAnimation");
        return super.onCreateAnimation(transit, enter, nextAnim);
    }

    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View v,
            final ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        Log.d(getFragmentTag(), "onCreateContextMenu");
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        Log.d(getFragmentTag(), "onCreateOptionsMenu");
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState) {
        Log.d(getFragmentTag(), "onCreateView");
        // TODO: rberghegger 17.03.17 find a smarter way to log what would be called on the delegate
        Log.v(getFragmentTag(), "mDelegate.onCreateView_beforeSuper(null, null, null);");
        return inflater.inflate(getLayoutResId(), container, false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(getFragmentTag(), "onDestroy");
        // TODO: rberghegger 17.03.17 find a smarter way to log what would be called on the delegate
        Log.v(getFragmentTag(), "mDelegate.onDestroy_afterSuper();");
    }

    @Override
    public void onDestroyOptionsMenu() {
        super.onDestroyOptionsMenu();
        Log.d(getFragmentTag(), "onDestroyOptionsMenu");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(getFragmentTag(), "onDestroyView");
        // TODO: rberghegger 17.03.17 find a smarter way to log what would be called on the delegate
        Log.v(getFragmentTag(), "mDelegate.onDestroyView_beforeSuper();");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(getFragmentTag(), "onDetach");
    }

    @Override
    public void onHiddenChanged(final boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.d(getFragmentTag(), "onHiddenChanged");
    }

    @Override
    public void onInflate(final Context context, final AttributeSet attrs,
            final Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);
        Log.d(getFragmentTag(), "onInflate");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.d(getFragmentTag(), "onLowMemory");
    }

    @Override
    public void onMultiWindowModeChanged(final boolean isInMultiWindowMode) {
        super.onMultiWindowModeChanged(isInMultiWindowMode);
        Log.d(getFragmentTag(), "onMultiWindowModeChanged");
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        Log.d(getFragmentTag(), "onOptionsItemSelected");
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onOptionsMenuClosed(final Menu menu) {
        super.onOptionsMenuClosed(menu);
        Log.d(getFragmentTag(), "onOptionsMenuClosed");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(getFragmentTag(), "onPause");
    }

    @Override
    public void onPictureInPictureModeChanged(final boolean isInPictureInPictureMode) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode);
        Log.d(getFragmentTag(), "onPictureInPictureModeChanged");
    }

    @Override
    public void onPrepareOptionsMenu(final Menu menu) {
        super.onPrepareOptionsMenu(menu);
        Log.d(getFragmentTag(), "onPrepareOptionsMenu");
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode,
            @NonNull final String[] permissions,
            @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(getFragmentTag(), "onRequestPermissionsResult");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(getFragmentTag(), "onResume");
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(getFragmentTag(), "onSaveInstanceState");
        // TODO: rberghegger 17.03.17 find a smarter way to log what would be called on the delegate
        Log.v(getFragmentTag(), "mDelegate.onSaveInstanceState_afterSuper(null);");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(getFragmentTag(), "onStart");
        // TODO: rberghegger 17.03.17 find a smarter way to log what would be called on the delegate
        Log.v(getFragmentTag(), "mDelegate.onStart_afterSuper();");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(getFragmentTag(), "onStop");
        // TODO: rberghegger 17.03.17 find a smarter way to log what would be called on the delegate
        Log.v(getFragmentTag(), "mDelegate.onStop_beforeSuper();");
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(getFragmentTag(), "onViewCreated");

        final TextView fragmentTag = (TextView) view.findViewById(R.id.sample_text);
        fragmentTag.setText(TAG);
    }

    @Override
    public void onViewStateRestored(@Nullable final Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Log.d(getFragmentTag(), "onViewStateRestored");
    }

    @LayoutRes
    abstract int getLayoutResId();

    private String getFragmentTag() {
        return TAG;
    }
}
