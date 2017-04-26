package net.grandcentrix.thirtyinch.sample.fragmentlifecycle;

import net.grandcentrix.thirtyinch.sample.R;
import net.grandcentrix.thirtyinch.sample.util.AndroidDeveloperOptions;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

import rx.Observable;

public class FragmentLifecycleActivity extends AppCompatActivity {

    static int fragmentLifecycleActivityInstanceCount = -1;

    private final String TAG = this.getClass().getSimpleName()
            + "@" + Integer.toHexString(this.hashCode());

    private SwitchCompat mSwitchAddToBackStack;

    private SwitchCompat mSwitchRetainPresenterInstance;

    public void addFragmentA(View view) {
        final TestFragmentA fragment = new TestFragmentA();
        Log.v(TAG, "adding FragmentA");
        addFragment(fragment);
    }

    public void addFragmentB(View view) {
        final TestFragmentB fragment = new TestFragmentB();
        Log.v(TAG, "adding FragmentB");
        addFragment(fragment);
    }

    public void detachFragmentAndAddAgain(View view) {
        final Fragment fragment = getSupportFragmentManager()
                .findFragmentById(R.id.fragment_placeholder);
        if (fragment != null) {
            //remove fragment
            Log.v(TAG, "// When the Fragment is removed.");
            getSupportFragmentManager().beginTransaction().remove(fragment).commitNow();

            Log.v(TAG, "// When the Fragment get added again to the Activity.");
            //add after delay again. Don't use the same transaction
            Observable.just(null).delay(1, TimeUnit.SECONDS)
                    .subscribe(o -> addFragment(fragment));
        } else {
            Toast.makeText(this, "no fragment found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void finish() {
        super.finish();
        Log.v(TAG, "// When the Activity gets finished");
    }

    public void finishActivity(View view) {
        Log.v(TAG, "finishing Activity");
        finish();
    }

    @Override
    public void onBackPressed() {
        Log.v(TAG, "// When the back button gets pressed");
        final FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            Log.v(TAG, "// When the top most fragment gets popped");
            fragmentManager.popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    public void recreateActivity(View view) {
        Log.v(TAG, "// And when the Activity is changing its configurations.");
        recreate();
    }

    public void removeFragmentA(View view) {
        final Fragment fragment = getSupportFragmentManager()
                .findFragmentById(R.id.fragment_placeholder);
        if (fragment instanceof TestFragmentA) {
            Log.v(TAG, "remove FragmentA");
            getSupportFragmentManager().beginTransaction()
                    .remove(fragment)
                    .commitNow();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            //started for the first time, reset all counters
            fragmentLifecycleActivityInstanceCount = -1;
            TestFragment.testFragmentInstanceCount = -1;
        }

        fragmentLifecycleActivityInstanceCount++;
        setContentView(R.layout.activity_fragment_lifecycle);
        FragmentManager.enableDebugLogging(true);
        Log.v(TAG, "onCreate of " + this);

        mSwitchAddToBackStack = (SwitchCompat) findViewById(R.id.switch_add_back_stack);
        mSwitchRetainPresenterInstance = (SwitchCompat) findViewById(
                R.id.switch_retain_presenter_instance);
        final TextView textDontKeepActivities = (TextView) findViewById(
                R.id.text_dont_keep_activities);
        textDontKeepActivities.setText(
                isDontKeepActivities() ? R.string.dont_keep_activities_enabled
                        : R.string.dont_keep_activities_disabled);

        Log.v(TAG, "// A new Activity gets created by the Android Framework.");
        Log.v(TAG, "final HostingActivity hostingActivity" + fragmentLifecycleActivityInstanceCount
                + " = new HostingActivity();");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.v(TAG, "onDestroy");

        Log.v(TAG, "// hostingActivity" + fragmentLifecycleActivityInstanceCount
                + " got destroyed.");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState(Bundle)");
    }

    private void addFragment(final Fragment fragment) {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        boolean retain = isRetainPresenterInstance();
        if (retain) {
            Log.v(TAG, "retaining presenter");
        }
        final Bundle bundle = new Bundle();
        bundle.putBoolean(TestFragment.RETAIN_PRESENTER, retain);
        fragment.setArguments(bundle);

        fragmentTransaction.replace(R.id.fragment_placeholder, fragment);
        if (isAddToBackStack()) {
            Log.v(TAG, "adding transaction to the back stack");
            fragmentTransaction.addToBackStack(null);
        }
        final int backStackId = fragmentTransaction.commit();
        if (backStackId >= 0) {
            Log.v(TAG, "Back stack ID: " + String.valueOf(backStackId));
        }
    }

    private boolean isAddToBackStack() {
        return mSwitchAddToBackStack.isChecked();
    }

    private boolean isDontKeepActivities() {
        return AndroidDeveloperOptions.isDontKeepActivitiesEnabled(this);
    }

    private boolean isRetainPresenterInstance() {
        return mSwitchRetainPresenterInstance.isChecked();
    }
}
