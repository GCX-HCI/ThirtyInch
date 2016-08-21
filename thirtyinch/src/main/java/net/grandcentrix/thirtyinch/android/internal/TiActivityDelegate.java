package net.grandcentrix.thirtyinch.android.internal;

import net.grandcentrix.thirtyinch.BindViewInterceptor;
import net.grandcentrix.thirtyinch.OnTimeRemovable;
import net.grandcentrix.thirtyinch.Removable;
import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.TiView;
import net.grandcentrix.thirtyinch.android.TiActivity;
import net.grandcentrix.thirtyinch.internal.PresenterSavior;
import net.grandcentrix.thirtyinch.internal.TiPresenterLogger;
import net.grandcentrix.thirtyinch.util.AnnotationUtil;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * This delegate allows sharing the activity code between the {@link TiActivity} and {@code
 * TiActivityPlugin}. The {@link TiActivity} could be easily implemented by adding the {@code
 * TiActivityPlugin} but the resulting dependency to CompositeAndroid is not a good way since it is
 * currently in an early stage.
 * <p>
 * It also allows 3rd party developers do add this delegate to other Activities using composition.
 */
public class TiActivityDelegate<P extends TiPresenter<V>, V extends TiView>
        implements ViewProvider<V> {

    private static final String SAVED_STATE_PRESENTER_ID = "presenter_id";

    private final AppCompatActivityProvider mActivityProvider;

    /**
     * flag indicating the started state of the Activity between {@link Activity#onStart()} and
     * {@link Activity#onStop()}.
     */
    private volatile boolean mActivityStarted = false;

    private List<BindViewInterceptor> mBindViewInterceptors = new ArrayList<>();

    /**
     * the cached version of the view send to the presenter after it passed the interceptors
     */
    private V mLastView;

    private TiPresenterLogger mLogger;

    /**
     * The presenter to which this activity will be attached as view when in the right state.
     */
    private P mPresenter;

    /**
     * The id of the presenter this view got attached to. Will be stored in the savedInstanceState
     * to find the same presenter after the Activity got recreated.
     */
    private String mPresenterId;

    private final PresenterProvider<P> mPresenterProvider;

    private ActivityRetainedPresenterProvider<P> mRetainedPresenterProvider;

    private ViewProvider<V> mViewProvider;

    public TiActivityDelegate(final AppCompatActivityProvider activityProvider,
            final ViewProvider<V> viewProvider,
            final PresenterProvider<P> presenterProvider,
            final ActivityRetainedPresenterProvider<P> retainedPresenterProvider,
            final TiPresenterLogger logger) {
        mActivityProvider = activityProvider;
        mViewProvider = viewProvider;
        mPresenterProvider = presenterProvider;
        mRetainedPresenterProvider = retainedPresenterProvider;
        mLogger = logger;
    }

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

    public void onConfigurationChanged_afterSuper(final Configuration newConfig) {
        // make sure the new view will be wrapped again
        mLastView = null;
    }

    public void onCreate_afterSuper(final Bundle savedInstanceState) {
        mLogger.log("onCreate(" + savedInstanceState + ")");

        // try recover presenter via lastNonConfigurationInstance
        // this works most of the time
        mPresenter = mRetainedPresenterProvider.getRetainedPresenter();
        if (mPresenter != null) {
            mLogger.log(
                    "recovered Presenter from lastCustomNonConfigurationInstance " + mPresenter);
        }

        if (mPresenter == null && savedInstanceState != null) {
            // recover with Savior
            // this should always work.
            final String recoveredPresenterId = savedInstanceState
                    .getString(SAVED_STATE_PRESENTER_ID);
            if (recoveredPresenterId != null) {
                mLogger.log("try to recover Presenter with id: " + recoveredPresenterId);
                //noinspection unchecked
                mPresenter = (P) PresenterSavior.INSTANCE.recover(recoveredPresenterId);
                if (mPresenter != null) {
                    // save recovered presenter with new id. No other instance of this activity,
                    // holding the presenter before, is now able to remove the reference to
                    // this presenter from the savior
                    PresenterSavior.INSTANCE.free(recoveredPresenterId);
                    mPresenterId = PresenterSavior.INSTANCE.safe(mPresenter);
                }
                mLogger.log("recovered Presenter " + mPresenter);
            }
        }

        if (mPresenter == null) {
            // create a new presenter
            mPresenter = mPresenterProvider.providePresenter();
            mLogger.log("created Presenter: " + mPresenter);
            mPresenterId = PresenterSavior.INSTANCE.safe(mPresenter);
            mPresenter.create();
        }
    }

    public void onDestroy_afterSuper() {
        final AppCompatActivity activity = mActivityProvider.getAppCompatActivity();
        mLogger.log("onDestroy() recreating=" + !activity.isFinishing());
        if (activity.isFinishing()) {
            mPresenter.destroy();
            PresenterSavior.INSTANCE.free(mPresenterId);
        }
    }

    public void onSaveInstanceState_afterSuper(final Bundle outState) {
        outState.putString(SAVED_STATE_PRESENTER_ID, mPresenterId);
    }

    public void onStart_afterSuper() {
        mActivityStarted = true;
        mActivityProvider.getAppCompatActivity().getWindow().getDecorView().post(new Runnable() {
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

    public void onStart_beforeSuper() {
        mLogger.log("onStart()");
        bindViewToPresenter();
    }

    public void onStop_afterSuper() {
        mPresenter.sleep();
    }

    public void onStop_beforeSuper() {
        mLogger.log("onStop()");
        mActivityStarted = false;
    }

    @NonNull
    @Override
    public V provideView() {
        final AppCompatActivity activity = mActivityProvider.getAppCompatActivity();
        final Class<?> foundViewInterface = AnnotationUtil
                .getInterfaceOfClassExtendingGivenInterface(activity.getClass(), TiView.class);

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
                return (V) activity;
            }
        }
    }

    /**
     * binds the view (this Activity) to the {@link #mPresenter}. Allows interceptors to change,
     * delegate or wrap the view before it gets attached to the presenter.
     */
    private void bindViewToPresenter() {
        if (mLastView == null) {
            V interceptedView = mViewProvider.provideView();
            for (final BindViewInterceptor interceptor : mBindViewInterceptors) {
                interceptedView = interceptor.intercept(interceptedView);
            }
            mLastView = interceptedView;
            mLogger.log("binding NEW view to Presenter " + mLastView);
            mPresenter.bindNewView(mLastView);
        } else {
            mLogger.log("binding the cached view to Presenter " + mLastView);
            mPresenter.bindNewView(mLastView);
        }
    }
}
