package net.grandcentrix.thirtyinch.plugin;

import com.pascalwelsch.compositeandroid.activity.ActivityPlugin;
import com.pascalwelsch.compositeandroid.activity.CompositeActivity;
import com.pascalwelsch.compositeandroid.activity.CompositeNonConfigurationInstance;

import net.grandcentrix.thirtyinch.Presenter;
import net.grandcentrix.thirtyinch.View;
import net.grandcentrix.thirtyinch.android.internal.CallOnMainThreadViewWrapper;
import net.grandcentrix.thirtyinch.internal.PresenterSavior;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

public abstract class ThirtyInchActivityPlugin<V extends View> extends ActivityPlugin {

    private static final String SAVED_STATE_PRESENTER_ID = "presenter_id";

    public static final String NCI_KEY_PRESENTER = "presenter";

    private final String TAG = this.getClass().getSimpleName()
            + "@" + Integer.toHexString(this.hashCode())
            + ":" + ThirtyInchActivityPlugin.class.getSimpleName();

    private volatile boolean mActivityStarted = false;

    private V mLastView;

    private boolean mNewConfig;

    private Presenter<V> mPresenter;

    private String mPresenterId;

    public ThirtyInchActivityPlugin() {

    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mNewConfig = true;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate(" + savedInstanceState + ")");

        // try recover presenter via lastNonConfigurationInstance
        // this works most of the time
        final Object nci = getLastNonConfigurationInstance(NCI_KEY_PRESENTER);
        if (nci instanceof Presenter) {
            //noinspection unchecked
            mPresenter = (Presenter<V>) nci;
            Log.d(TAG, "recovered Presenter from lastCustomNonConfigurationInstance " + mPresenter);
        }

        if (mPresenter == null && savedInstanceState != null) {
            // recover with Savior
            // this should always work.
            final String recoveredPresenterId = savedInstanceState
                    .getString(SAVED_STATE_PRESENTER_ID);
            if (recoveredPresenterId != null) {
                Log.d(TAG, "try to recover Presenter with id: " + recoveredPresenterId);
                //noinspection unchecked
                mPresenter = PresenterSavior.INSTANCE.recover(recoveredPresenterId);
                if (mPresenter != null) {
                    // save recovered presenter with new id. No other instance of this activity,
                    // holding the presenter before, is now able to remove the reference to
                    // this presenter from the savior
                    PresenterSavior.INSTANCE.free(recoveredPresenterId);
                    mPresenterId = PresenterSavior.INSTANCE.safe(mPresenter);
                }
                Log.d(TAG, "recovered Presenter " + mPresenter);
            }
        }

        if (mPresenter == null) {
            // create a new presenter
            Bundle activityExtras = getActivity().getIntent().getExtras();
            if (activityExtras == null) {
                activityExtras = new Bundle();
            }
            mPresenter = providePresenter(activityExtras);
            Log.d(TAG, "created Presenter: " + mPresenter);
            mPresenterId = PresenterSavior.INSTANCE.safe(mPresenter);
            mPresenter.create();
        }

        mNewConfig = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        final boolean finishing = getActivity().isFinishing();
        Log.v(TAG, "onDestroy() recreating=" + !finishing);
        if (finishing) {
            mPresenter.destroy();
            PresenterSavior.INSTANCE.free(mPresenterId);
        }
    }

    @Override
    public CompositeNonConfigurationInstance onRetainNonConfigurationInstance() {
        return new CompositeNonConfigurationInstance(NCI_KEY_PRESENTER, mPresenter);
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVED_STATE_PRESENTER_ID, mPresenterId);
    }

    @Override
    public void onStart() {
        Log.v(TAG, "onStart()");
        if (mNewConfig || mLastView == null) {
            mNewConfig = false;
            final CompositeActivity activity = getActivity();
            if (!(activity instanceof View)) {
                throw new IllegalStateException("Activity doesn't implement the View interface");
            }

            final V view = (V) activity;
            mLastView = view;
            mLastView = CallOnMainThreadViewWrapper.wrap(mLastView);
            mPresenter.bindNewView(mLastView);
            Log.d(TAG, "bound NEW View (" + mLastView + ") to Presenter (" + mPresenter + ")");
        } else {
            mPresenter.bindNewView(mLastView);
            Log.d(TAG, "bound View (" + mLastView + ") to Presenter (" + mPresenter + ")");
        }
        super.onStart();
        mActivityStarted = true;
        getActivity().getWindow().getDecorView().post(new Runnable() {
            @Override
            public void run() {
                if (mActivityStarted) {
                    mPresenter.wakeUp();
                }
            }
        });
    }

    @Override
    public void onStop() {
        Log.v(TAG, "onStop()");
        mActivityStarted = false;
        super.onStop();
        mPresenter.sleep();
    }

    @Override
    public String toString() {
        final Presenter<V> p = mPresenter;
        String presenter;
        if (p == null) {
            presenter = "null";
        } else {
            presenter = p.getClass().getSimpleName()
                    + "@" + Integer.toHexString(p.hashCode());
        }

        return getClass().getSimpleName() + "@" + Integer.toHexString(hashCode())
                + "{"
                + "presenter=" + presenter
                + "}";
    }

    protected Presenter<V> getPresenter() {
        return mPresenter;
    }

    @NonNull
    protected abstract Presenter<V> providePresenter(
            @NonNull final Bundle activityIntentBundle);
}
