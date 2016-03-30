package net.grandcentrix.divorce.android;

import net.grandcentrix.divorce.Presenter;
import net.grandcentrix.divorce.PresenterSavior;
import net.grandcentrix.divorce.View;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by pascalwelsch on 9/8/15.
 */
public abstract class DivorceActivity<V extends View> extends AppCompatActivity {

    private static final String SAVED_STATE_PRESENTER_ID = "presenter_id";

    private final String TAG = this.getClass().getSimpleName()
            + "@" + Integer.toHexString(this.hashCode())
            + ":" + DivorceActivity.class.getSimpleName();

    private Presenter<V> mPresenter;

    private String mPresenterId;

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        final Object object = super.onRetainCustomNonConfigurationInstance();
        return new PresenterNonConfigurationInstance<>(mPresenter, object);
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
    protected abstract V provideView();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate(" + savedInstanceState + ")");

        // try recover presenter via lastNonConfigurationInstance
        // this works most of the time
        final Object nci = getLastCustomNonConfigurationInstance();
        if (nci instanceof PresenterNonConfigurationInstance) {
            final PresenterNonConfigurationInstance pnci = (PresenterNonConfigurationInstance) nci;
            //noinspection unchecked
            mPresenter = pnci.getPresenter();
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
            Bundle activityExtras = getIntent().getExtras();
            if (activityExtras == null) {
                activityExtras = new Bundle();
            }
            mPresenter = onCreatePresenter(activityExtras);
            Log.d(TAG, "created Presenter: " + mPresenter);
            mPresenterId = PresenterSavior.INSTANCE.safe(mPresenter);
            mPresenter.create();
        }
    }

    @NonNull
    protected abstract Presenter<V> onCreatePresenter(@NonNull final Bundle activityIntentBundle);

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy() recreating=" + !isFinishing());
        if (isFinishing()) {
            mPresenter.destroy();
            PresenterSavior.INSTANCE.free(mPresenterId);
        }
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVED_STATE_PRESENTER_ID, mPresenterId);
    }

    @Override
    protected void onStart() {
        Log.v(TAG, "onStart()");
        final V view = provideView();
        mPresenter.bindNewView(MainThreadViewWrapper.wrap(view));
        Log.d(TAG, "bound new View (" + view + ") to Presenter (" + mPresenter + ")");
        super.onStart();
        getWindow().getDecorView().post(new Runnable() {
            @Override
            public void run() {
                mPresenter.wakeUp();
            }
        });
    }

    @Override
    protected void onStop() {
        Log.v(TAG, "onStop()");
        super.onStop();
        mPresenter.sleep();
    }
}
