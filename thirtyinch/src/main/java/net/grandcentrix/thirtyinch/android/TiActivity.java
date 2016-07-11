package net.grandcentrix.thirtyinch.android;

import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.TiView;
import net.grandcentrix.thirtyinch.android.internal.ActivityPresenterProvider;
import net.grandcentrix.thirtyinch.android.internal.CallOnMainThreadViewWrapper;
import net.grandcentrix.thirtyinch.android.internal.PresenterNonConfigurationInstance;
import net.grandcentrix.thirtyinch.internal.PresenterSavior;
import net.grandcentrix.thirtyinch.util.AnnotationUtil;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by pascalwelsch on 9/8/15.
 */
public abstract class TiActivity<P extends TiPresenter<V>, V extends TiView>
        extends AppCompatActivity implements ActivityPresenterProvider<P> {

    private static final String SAVED_STATE_PRESENTER_ID = "presenter_id";

    private final String TAG = this.getClass().getSimpleName()
            + "@" + Integer.toHexString(this.hashCode())
            + ":" + TiActivity.class.getSimpleName();

    private volatile boolean mActivityStarted = false;

    private V mLastView;

    private boolean mNewConfig;

    private P mPresenter;

    private String mPresenterId;

    public P getPresenter() {
        return mPresenter;
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mNewConfig = true;
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        final Object object = super.onRetainCustomNonConfigurationInstance();
        return new PresenterNonConfigurationInstance<>(mPresenter, object);
    }

    @Override
    public String toString() {
        String presenter = mPresenter == null ? "null" :
                mPresenter.getClass().getSimpleName()
                        + "@" + Integer.toHexString(mPresenter.hashCode());

        return getClass().getSimpleName() + "@" + Integer.toHexString(hashCode())
                + "{presenter=" + presenter + "}";
    }

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
            mPresenter = (P) pnci.getPresenter();
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
                mPresenter = (P) PresenterSavior.INSTANCE.recover(recoveredPresenterId);
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
            mPresenter = providePresenter(activityExtras);
            Log.d(TAG, "created Presenter: " + mPresenter);
            mPresenterId = PresenterSavior.INSTANCE.safe(mPresenter);
            mPresenter.create();
        }

        mNewConfig = true;
    }

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
        if (mNewConfig || mLastView == null) {
            mNewConfig = false;
            final V view = provideView();
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
        getWindow().getDecorView().post(new Runnable() {
            @Override
            public void run() {
                if (mActivityStarted) {
                    mPresenter.wakeUp();
                }
            }
        });
    }

    @Override
    protected void onStop() {
        Log.v(TAG, "onStop()");
        mActivityStarted = false;
        super.onStop();
        mPresenter.sleep();
    }

    /**
     * the default implementation assumes that the activity is the view and implements the {@link
     * TiView} interface. Override this method for a different behaviour.
     *
     * @return the object implementing the TiView interface
     */
    @NonNull
    protected V provideView() {

        final Class<?> foundViewInterface = AnnotationUtil
                .getInterfaceOfClassExtendingGivenInterface(this.getClass(), TiView.class);

        if (foundViewInterface == null) {
            throw new IllegalArgumentException(
                    "This Activity doesn't implement a TiView interface. "
                            + "This is the default behaviour. Override provideView() to explicitly change this.");
        } else {
            if (foundViewInterface.getSimpleName().equals("TiView")) {
                throw new IllegalArgumentException(
                        "extending TiView doesn't make sense, it's an empty interface."
                                + " This is the default behaviour. Override provideView() to explicitly change this.");
            } else {
                // assume that the activity itself is the view and implements the TiView interface
                //noinspection unchecked
                return (V) this;
            }
        }
    }

}
