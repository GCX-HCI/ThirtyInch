package net.grandcentrix.thirtyinch.sample.fragmentlifecycle;

import net.grandcentrix.thirtyinch.sample.R;

import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import static android.provider.Settings.Global.ALWAYS_FINISH_ACTIVITIES;
import static net.grandcentrix.thirtyinch.sample.fragmentlifecycle.TestFragment.testFragmentInstanceCount;

public class FragmentLifecycleActivity extends AppCompatActivity {

    static int fragmentLifecycleActivityInstanceCount = -1;

    private final String TAG = this.getClass().getSimpleName()
            + "@" + Integer.toHexString(this.hashCode());

    private SwitchCompat mSwitchAddToBackStack;

    private SwitchCompat mSwitchRetainFragmentInstance;

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

    @Override
    public void finish() {
        super.finish();
        Log.v(TAG, "// When the Activity finishes");
    }

    public void finishActivity(View view) {
        finish();
    }

    @Override
    public void onBackPressed() {
        Log.v(TAG, "// When the back button gets pressed");
        Log.v(TAG, "// When the top most fragment gets popped");
        final FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
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
            getSupportFragmentManager().beginTransaction()
                    .remove(fragment)
                    .commitNow();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentLifecycleActivityInstanceCount++;
        setContentView(R.layout.activity_fragment_lifecycle);
        FragmentManager.enableDebugLogging(true);
        Log.v(TAG, "onCreate of " + this);

        mSwitchAddToBackStack = (SwitchCompat) findViewById(R.id.switch_add_back_stack);
        mSwitchRetainFragmentInstance = (SwitchCompat) findViewById(
                R.id.switch_retain_fragment_instance);
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

        Log.v(TAG, "onDestroy of " + this);

        Log.v(TAG, "hostingActivity" + fragmentLifecycleActivityInstanceCount
                + ".setChangingConfiguration(" + isChangingConfigurations() + ");");
        Log.v(TAG, "hostingActivity" + fragmentLifecycleActivityInstanceCount
                + ".setFinishing(" + isFinishing() + ");");
        Log.v(TAG, "// hostingActivity" + fragmentLifecycleActivityInstanceCount
                + " got destroyed.");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");
        Log.v(TAG, "hostingActivity" + fragmentLifecycleActivityInstanceCount + ""
                + ".setChangingConfiguration(" + isChangingConfigurations() + ");");
        Log.v(TAG, "hostingActivity" + fragmentLifecycleActivityInstanceCount + ""
                + ".setFinishing(" + isFinishing() + ");");
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState(Bundle)");
        Log.v(TAG, "hostingActivity" + fragmentLifecycleActivityInstanceCount + ""
                + ".setChangingConfiguration(" + isChangingConfigurations() + ");");
        Log.v(TAG, "hostingActivity" + fragmentLifecycleActivityInstanceCount + ""
                + ".setFinishing(" + isFinishing() + ");");
    }

    private void addFragment(final Fragment fragment) {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (isRetainFragmentInstance()) {
            Log.v(TAG, "retaining fragment instance");
            fragment.setRetainInstance(true);
        }
        fragmentTransaction.replace(R.id.fragment_placeholder, fragment);
        if (isAddToBackStack()) {
            Log.v(TAG, "adding transaction to the back stack");
            fragmentTransaction.addToBackStack(null);
        }
        final int backStackId = fragmentTransaction.commit();
        Log.v(TAG, "\n// Given a Presenter ...");
        // (testFragmentInstanceCount + 1) because it will be created after executing this code
        Log.v(TAG, "final TestPresenter presenter" + (testFragmentInstanceCount + 1) + " ="
                + " new TestPresenter(new TiConfiguration.Builder()\n"
                + "                .setUseStaticSaviorToRetain(/*TODO set*/)\n"
                + "                .setRetainPresenterEnabled(" + isRetainFragmentInstance() + ")\n"
                + "                .build());");

        Log.v(TAG, "\n// And given a Fragment.");
        Log.v(TAG, "final TiFragmentDelegate<TiPresenter<TiView>, TiView> "
                + "delegate" + (testFragmentInstanceCount + 1) + "\n"
                + "                = new TiFragmentDelegateBuilder()\n"
                + "                .setDontKeepActivitiesEnabled(" + isDontKeepActivities() + ")\n"
                + "                .setHostingActivity(hostingActivity)\n"
                + "                .setSavior(mSavior)\n"
                + "                .setPresenter(presenter" + (testFragmentInstanceCount + 1)
                + ")\n"
                + "                .build();");

        if (backStackId >= 0) {
            Log.v(TAG, "Back stack ID: " + String.valueOf(backStackId));
        }
    }

    private boolean isAddToBackStack() {
        return mSwitchAddToBackStack.isChecked();
    }

    private boolean isDontKeepActivities() {
        // default behaviour
        int dontKeepActivities = 0;
        try {
            dontKeepActivities = Settings.Global
                    .getInt(getContentResolver(), ALWAYS_FINISH_ACTIVITIES);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return dontKeepActivities != 0;
    }

    private boolean isRetainFragmentInstance() {
        return mSwitchRetainFragmentInstance.isChecked();
    }
}
