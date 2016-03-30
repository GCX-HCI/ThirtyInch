package net.grandcentrix.divorce;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;

public abstract class DivorceFragment<V extends View> extends Fragment implements
        View {

    private static final String SAVED_STATE_PRESENTER_ID = "presenter_id";

    private final String TAG = this.getClass().getSimpleName()
            + "@" + Integer.toHexString(this.hashCode())
            + ":" + DivorceFragment.class.getSimpleName();

    private Presenter<V> mPresenter;

    private String mPresenterId;

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        Log.v(TAG, "onDestroy()");

        if (mPresenter == null) {
            final Bundle activityExtras = activity.getIntent().getExtras();
            mPresenter = onCreatePresenter(activityExtras, getArguments());
            Log.d(TAG, "created Presenter: " + mPresenter);
            mPresenter.create();
        }
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate(" + savedInstanceState + ")");
        //setRetainInstance(true);

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
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "onDestroy()");
        if (isUiPossible()) {
            mPresenter.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        Log.v(TAG, "onDestroyView()");
        mPresenter.sleep();
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.v(TAG, "onDetach()");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v(TAG, "onPause()");
        if (isUiPossible()) {
            mPresenter.movedToBackground();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v(TAG, "onResume()");
        if (isUiPossible()) {
            getActivity().getWindow().getDecorView().post(new Runnable() {
                @Override
                public void run() {
                    if (isUiPossible()) {
                        mPresenter.moveToForeground();
                    }
                }
            });
        }
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVED_STATE_PRESENTER_ID, mPresenterId);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.v(TAG, "onStart()");

        if (isUiPossible()) {
            final V view = getRxView();
            mPresenter.bindNewView(view, UiPlatform.ANDROID);
            Log.d(TAG, "bound new View (" + view + ") to Presenter (" + mPresenter + ")");
            getActivity().getWindow().getDecorView().post(new Runnable() {
                @Override
                public void run() {
                    if (isUiPossible()) {
                        mPresenter.wakeUp();
                    }
                }
            });
        }
    }

    @Override
    public void onStop() {
        Log.v(TAG, "onStop()");
        mPresenter.sleep();
        super.onStop();
    }

    @Override
    final public void setRetainInstance(final boolean retain) {
        super.setRetainInstance(true);
    }

    @NonNull
    protected abstract V getRxView();

    @NonNull
    protected abstract Presenter<V> onCreatePresenter(
            @NonNull final Bundle activityIntentBundle, @NonNull final Bundle fragmentArguments);

    private boolean isUiPossible() {
        return isAdded() && !isDetached();
    }
}