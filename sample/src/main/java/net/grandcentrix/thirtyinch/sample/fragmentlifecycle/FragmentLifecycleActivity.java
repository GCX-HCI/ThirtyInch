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

public class FragmentLifecycleActivity extends AppCompatActivity {

    private static final String TAG = FragmentLifecycleActivity.class.getSimpleName();

    private SwitchCompat mSwitchAddToBackStack;

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

    public void finishActivity(View view) {
        finish();
    }

    @Override
    public void onBackPressed() {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    public void recreateActivity(View view) {
        recreate();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_lifecycle);

        mSwitchAddToBackStack = (SwitchCompat) findViewById(R.id.switch_add_back_stack);
        final TextView textDontKeepActivities = (TextView) findViewById(
                R.id.text_dont_keep_activities);
        textDontKeepActivities.setText(
                isDontKeepActivities() ? R.string.dont_keep_activities_enabled
                        : R.string.dont_keep_activities_disabled);
    }

    private void addFragment(final Fragment fragment) {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
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
}
