package net.grandcentrix.thirtyinch.android.internal;

import net.grandcentrix.thirtyinch.Removable;
import net.grandcentrix.thirtyinch.TiBindViewInterceptor;
import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.TiView;
import net.grandcentrix.thirtyinch.android.TiActivity;
import net.grandcentrix.thirtyinch.internal.InterceptableViewBinder;
import net.grandcentrix.thirtyinch.internal.PresenterSavior;
import net.grandcentrix.thirtyinch.internal.PresenterViewBinder;
import net.grandcentrix.thirtyinch.internal.TiPresenterLogger;
import net.grandcentrix.thirtyinch.internal.TiPresenterProvider;
import net.grandcentrix.thirtyinch.internal.TiViewProvider;
import net.grandcentrix.thirtyinch.util.AnnotationUtil;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

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
        implements TiViewProvider<V>, InterceptableViewBinder<V> {

    private static final String SAVED_STATE_PRESENTER_ID = "presenter_id";

    private final TiAppCompatActivityProvider mActivityProvider;

    /**
     * flag indicating the started state of the Activity between {@link Activity#onStart()} and
     * {@link Activity#onStop()}.
     */
    private volatile boolean mActivityStarted = false;

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

    private final TiPresenterProvider<P> mPresenterProvider;

    private TiActivityRetainedPresenterProvider<P> mRetainedPresenterProvider;

    private final PresenterViewBinder<V> mViewBinder;

    private TiViewProvider<V> mViewProvider;

    public TiActivityDelegate(final TiAppCompatActivityProvider activityProvider,
            final TiViewProvider<V> viewProvider,
            final TiPresenterProvider<P> presenterProvider,
            final TiActivityRetainedPresenterProvider<P> retainedPresenterProvider,
            final TiPresenterLogger logger) {
        mActivityProvider = activityProvider;
        mViewProvider = viewProvider;
        mPresenterProvider = presenterProvider;
        mRetainedPresenterProvider = retainedPresenterProvider;
        mLogger = logger;
        mViewBinder = new PresenterViewBinder<>(logger);
    }

    @NonNull
    @Override
    public Removable addBindViewInterceptor(final TiBindViewInterceptor interceptor) {
        return mViewBinder.addBindViewInterceptor(interceptor);
    }

    @Nullable
    @Override
    public V getInterceptedViewOf(final TiBindViewInterceptor interceptor) {
        return mViewBinder.getInterceptedViewOf(interceptor);
    }

    @NonNull
    @Override
    public List<TiBindViewInterceptor> getInterceptors(
            final Filter<TiBindViewInterceptor> predicate) {
        return mViewBinder.getInterceptors(predicate);
    }

    public P getPresenter() {
        return mPresenter;
    }

    /**
     * Invalidates the cache of the latest bound view. Forces the next binding of the view to run
     * through all the interceptors (again).
     */
    @Override
    public void invalidateView() {
        mViewBinder.invalidateView();
    }

    public void onConfigurationChanged_afterSuper(final Configuration newConfig) {
        // make sure the new view will be wrapped again
        mViewBinder.invalidateView();
    }

    public void onCreate_afterSuper(final Bundle savedInstanceState) {
        mLogger.logTiMessages("onCreate(" + savedInstanceState + ")");

        // try recover presenter via lastNonConfigurationInstance
        // this works most of the time
        mPresenter = mRetainedPresenterProvider.getRetainedPresenter();
        if (mPresenter != null) {
            mLogger.logTiMessages(
                    "recovered Presenter from lastCustomNonConfigurationInstance " + mPresenter);
        }

        if (mPresenter == null && savedInstanceState != null) {
            // recover with Savior
            // this should always work.
            final String recoveredPresenterId = savedInstanceState
                    .getString(SAVED_STATE_PRESENTER_ID);
            if (recoveredPresenterId != null) {
                mLogger.logTiMessages("try to recover Presenter with id: " + recoveredPresenterId);
                //noinspection unchecked
                mPresenter = (P) PresenterSavior.INSTANCE.recover(recoveredPresenterId);
                if (mPresenter != null) {
                    // save recovered presenter with new id. No other instance of this activity,
                    // holding the presenter before, is now able to remove the reference to
                    // this presenter from the savior
                    PresenterSavior.INSTANCE.free(recoveredPresenterId);
                    mPresenterId = PresenterSavior.INSTANCE.safe(mPresenter);
                }
                mLogger.logTiMessages("recovered Presenter " + mPresenter);
            }
        }

        if (mPresenter == null) {
            // create a new presenter
            mPresenter = mPresenterProvider.providePresenter();
            mLogger.logTiMessages("created Presenter: " + mPresenter);
            mPresenterId = PresenterSavior.INSTANCE.safe(mPresenter);
            mPresenter.create();
        }
    }

    public void onDestroy_afterSuper() {
        final AppCompatActivity activity = mActivityProvider.getAppCompatActivity();
        mLogger.logTiMessages("onDestroy() recreating=" + !activity.isFinishing());
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
        mLogger.logTiMessages("onStart()");
        mViewBinder.bindView(mPresenter, mViewProvider);
    }

    public void onStop_afterSuper() {
        mPresenter.sleep();
    }

    public void onStop_beforeSuper() {
        mLogger.logTiMessages("onStop()");
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
}
