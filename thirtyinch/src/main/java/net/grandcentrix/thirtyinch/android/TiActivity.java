package net.grandcentrix.thirtyinch.android;

import net.grandcentrix.thirtyinch.BindViewInterceptor;
import net.grandcentrix.thirtyinch.OnTimeRemovable;
import net.grandcentrix.thirtyinch.Removable;
import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.TiView;
import net.grandcentrix.thirtyinch.android.internal.PresenterNonConfigurationInstance;
import net.grandcentrix.thirtyinch.android.internal.PresenterProvider;
import net.grandcentrix.thirtyinch.internal.PresenterSavior;
import net.grandcentrix.thirtyinch.util.AnnotationUtil;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pascalwelsch on 9/8/15.
 */
public abstract class TiActivity<P extends TiPresenter<V>, V extends TiView>
        extends AppCompatActivity implements PresenterProvider<P> {

    private static final String SAVED_STATE_PRESENTER_ID = "presenter_id";

    private final String TAG = this.getClass().getSimpleName()
            + "@" + Integer.toHexString(this.hashCode())
            + ":" + TiActivity.class.getSimpleName();

    /**
     * flag indicating the started state of the Activity between {@link #onStart()} and {@link
     * #onStop()}.
     */
    private volatile boolean mActivityStarted = false;

    private List<BindViewInterceptor> mBindViewInterceptors = new ArrayList<>();

    /**
     * the cached version of the view send to the presenter after it passed the interceptors
     */
    private V mLastView;

    /**
     * The presenter to which this activity will be attached as view when in the right state.
     */
    private P mPresenter;

    /**
     * The id of the presenter this view got attached to. Will be stored in the savedInstanceState
     * to find the same presenter after the Activity got recreated.
     */
    private String mPresenterId;

    public Removable addBindViewInterceptor(final BindViewInterceptor interceptor) {
        mBindViewInterceptors.add(interceptor);
        mLastView = null;

        return new OnTimeRemovable() {
            @Override
            public void onRemove() {
                mBindViewInterceptors.remove(interceptor);
            }
        };
    }

    public P getPresenter() {
        return mPresenter;
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // make sure the new view will be wrapped again
        mLastView = null;
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
            mPresenter = providePresenter();
            Log.d(TAG, "created Presenter: " + mPresenter);
            mPresenterId = PresenterSavior.INSTANCE.safe(mPresenter);
            mPresenter.create();
        }
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
        bindViewToPresenter();
        super.onStart();
        mActivityStarted = true;
        getWindow().getDecorView().post(new Runnable() {
            @Override
            public void run() {
                // check if still started. It happens that onStop got already called, specially
                // when the Activity is not the top Activity and a configuration change happens
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

    /**
     * binds the view (this Activity) to the {@link #mPresenter}. Allows interceptors to change,
     * delegate or wrap the view before it gets attached to the presenter.
     */
    private void bindViewToPresenter() {
        if (mLastView == null) {
            V interceptedView = provideView();
            for (final BindViewInterceptor interceptor : mBindViewInterceptors) {
                interceptedView = interceptor.intercept(interceptedView);
            }
            mLastView = interceptedView;
            Log.v(TAG, "binding NEW view to Presenter " + mLastView);
            mPresenter.bindNewView(mLastView);
        } else {
            Log.v(TAG, "binding the cached view to Presenter " + mLastView);
            mPresenter.bindNewView(mLastView);
        }
    }

}
