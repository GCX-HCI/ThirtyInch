package net.grandcentrix.thirtyinch.internal;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import net.grandcentrix.thirtyinch.BindViewInterceptor;
import net.grandcentrix.thirtyinch.Removable;
import net.grandcentrix.thirtyinch.TiConfiguration;
import net.grandcentrix.thirtyinch.TiFragment;
import net.grandcentrix.thirtyinch.TiLog;
import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.TiView;
import net.grandcentrix.thirtyinch.callonmainthread.CallOnMainThreadInterceptor;
import net.grandcentrix.thirtyinch.distinctuntilchanged.DistinctUntilChangedInterceptor;
import net.grandcentrix.thirtyinch.util.AndroidDeveloperOptions;
import net.grandcentrix.thirtyinch.util.AnnotationUtil;

import java.util.List;

/**
 * This delegate allows sharing the fragment code between the {@link TiFragment} and other
 * {@link Fragment} implementations in 3rd party code.
 */
public class TiFragmentDelegate<P extends TiPresenter<V>, V extends TiView, F extends Fragment & TiPresenterProvider<P>>
        implements TiLoggingTagProvider, TiViewProvider<V>, InterceptableViewBinder<V> {

    private static final String SAVED_STATE_PRESENTER_ID = "presenter_id";

    /**
     * enables debug logging during development
     */
    private static final boolean ENABLE_DEBUG_LOGGING = false;

    private final String TAG = this.getClass().getSimpleName()
            + ":" + TiFragmentDelegate.class.getSimpleName()
            + "@" + Integer.toHexString(this.hashCode());

    private volatile boolean mActivityStarted = false;

    private P mPresenter;

    private String mPresenterId;

    private PresenterViewBinder<V> mViewBinder = new PresenterViewBinder<>(this);

    private final F mFragment;

    public TiFragmentDelegate(F fragment) {
        mFragment = fragment;
    }

    @NonNull
    @Override
    public Removable addBindViewInterceptor(@NonNull final BindViewInterceptor interceptor) {
        return mViewBinder.addBindViewInterceptor(interceptor);
    }

    @Nullable
    @Override
    public V getInterceptedViewOf(@NonNull final BindViewInterceptor interceptor) {
        return mViewBinder.getInterceptedViewOf(interceptor);
    }

    @NonNull
    @Override
    public List<BindViewInterceptor> getInterceptors(
            @NonNull final Filter<BindViewInterceptor> predicate) {
        return mViewBinder.getInterceptors(predicate);
    }

    @Override
    public String getLoggingTag() {
        return TAG;
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

    // TODO
    public void onCreate(final Bundle savedInstanceState) {
        if (mPresenter == null && savedInstanceState != null) {
            // recover with Savior
            // this should always work.
            final String recoveredPresenterId = savedInstanceState
                    .getString(SAVED_STATE_PRESENTER_ID);
            if (recoveredPresenterId != null) {
                TiLog.v(TAG, "try to recover Presenter with id: " + recoveredPresenterId);
                //noinspection unchecked
                mPresenter = (P) PresenterSavior.INSTANCE.recover(recoveredPresenterId);
                if (mPresenter != null) {
                    // save recovered presenter with new id. No other instance of this activity,
                    // holding the presenter before, is now able to remove the reference to
                    // this presenter from the savior
                    PresenterSavior.INSTANCE.free(recoveredPresenterId);
                    mPresenterId = PresenterSavior.INSTANCE.safe(mPresenter);
                }
                TiLog.v(TAG, "recovered Presenter " + mPresenter);
            }
        }

        if (mPresenter == null) {
            mPresenter = mFragment.providePresenter();
            TiLog.v(TAG, "created Presenter: " + mPresenter);
            final TiConfiguration config = mPresenter.getConfig();
            if (config.shouldRetainPresenter() && config.useStaticSaviorToRetain()) {
                mPresenterId = PresenterSavior.INSTANCE.safe(mPresenter);
            }
            mPresenter.create();
        }

        final TiConfiguration config = mPresenter.getConfig();
        if (config.isCallOnMainThreadInterceptorEnabled()) {
            addBindViewInterceptor(new CallOnMainThreadInterceptor());
        }

        if (config.isDistinctUntilChangedInterceptorEnabled()) {
            addBindViewInterceptor(new DistinctUntilChangedInterceptor());
        }

        if (config.shouldRetainPresenter()) {
            mFragment.setRetainInstance(true);
        }
    }

    // TODO
    @SuppressWarnings("UnusedParameters")
    public void onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        mViewBinder.invalidateView();
    }

    // TODO
    public void onDestroy() {
        //FIXME handle attach/detach state

        logState();

        boolean destroyPresenter = false;
        if (mFragment.getActivity().isFinishing()) {
            // Probably a backpress and not a configuration change
            // Activity will not be recreated and finally destroyed, also destroyed the presenter
            destroyPresenter = true;
            TiLog.v(TAG, "Activity is finishing, destroying presenter " + mPresenter);
        }

        final TiConfiguration config = mPresenter.getConfig();
        if (!destroyPresenter &&
                !config.shouldRetainPresenter()) {
            // configuration says the presenter should not be retained, a new presenter instance
            // will be created and the current presenter should be destroyed
            destroyPresenter = true;
            TiLog.v(TAG, "presenter configured as not retaining, destroying " + mPresenter);
        }

        if (!destroyPresenter &&
                !config.useStaticSaviorToRetain()
                && AndroidDeveloperOptions.isDontKeepActivitiesEnabled(mFragment.getActivity())) {
            // configuration says the PresenterSavior should not be used. Retaining the presenter
            // relays on the Activity nonConfigurationInstance which is always null when
            // "don't keep activities" is enabled.
            // a new presenter instance will be created and the current presenter should be destroyed
            destroyPresenter = true;
            TiLog.v(TAG, "the PresenterSavior is disabled and \"don\'t keep activities\" is "
                    + "activated. The presenter can't be retained. Destroying " + mPresenter);
        }

        if (destroyPresenter) {
            mPresenter.destroy();
            PresenterSavior.INSTANCE.free(mPresenterId);
        } else {
            TiLog.v(TAG, "not destroying " + mPresenter
                    + " which will be reused by the next Activity instance, recreating...");
        }
    }

    // TODO
    public void onDestroyView() {
        mPresenter.detachView();
    }

    // TODO
    public void onSaveInstanceState(final Bundle outState) {
        outState.putString(SAVED_STATE_PRESENTER_ID, mPresenterId);
    }

    // TODO
    public void onStart() {
        mActivityStarted = true;

        if (isUiPossible()) {
            mFragment.getActivity().getWindow().getDecorView().post(new Runnable() {
                @Override
                public void run() {
                    if (isUiPossible() && mActivityStarted) {
                        mViewBinder.bindView(mPresenter, TiFragmentDelegate.this);
                    }
                }
            });
        }
    }

    // TODO
    public void onStop() {
        mActivityStarted = false;
        mPresenter.detachView();
    }

    /**
     * the default implementation assumes that the fragment is the view and implements the {@link
     * TiView} interface. Override this method for a different behaviour.
     *
     * @return the object implementing the TiView interface
     */
    @NonNull
    public V provideView() {

        final Class<?> foundViewInterface = AnnotationUtil
                .getInterfaceOfClassExtendingGivenInterface(mFragment.getClass(), TiView.class);

        if (foundViewInterface == null) {
            throw new IllegalArgumentException(
                    "This Fragment doesn't implement a TiView interface. "
                            + "This is the default behaviour. Override provideView() to explicitly change this.");
        } else {
            if (foundViewInterface.getSimpleName().equals("TiView")) {
                throw new IllegalArgumentException(
                        "extending TiView doesn't make sense, it's an empty interface."
                                + " This is the default behaviour. Override provideView() to explicitly change this.");
            } else {
                // assume that the fragment itself is the view and implements the TiView interface
                //noinspection unchecked
                return (V) mFragment;
            }
        }
    }

    @Override
    public String toString() {
        String presenter = getPresenter() == null ? "null" :
                getPresenter().getClass().getSimpleName()
                        + "@" + Integer.toHexString(getPresenter().hashCode());

        return getClass().getSimpleName()
                + ":" + TiFragmentDelegate.class.getSimpleName()
                + "@" + Integer.toHexString(hashCode())
                + "{presenter=" + presenter + "}";
    }

    public String fragmentToString() {
        String presenter = getPresenter() == null ? "null" :
                getPresenter().getClass().getSimpleName()
                        + "@" + Integer.toHexString(getPresenter().hashCode());

        return mFragment.getClass().getSimpleName()
                + "@" + Integer.toHexString(hashCode())
                + "{presenter=" + presenter + "}";
    }

    private boolean isUiPossible() {
        return mFragment.isAdded() && !mFragment.isDetached();
    }

    private void logState() {
        if (ENABLE_DEBUG_LOGGING) {
            TiLog.v(TAG, "isChangingConfigurations = " + mFragment.getActivity().isChangingConfigurations());
            TiLog.v(TAG, "isActivityFinishing = " + mFragment.getActivity().isFinishing());
            TiLog.v(TAG, "isAdded = " + mFragment.isAdded());
            TiLog.v(TAG, "isDetached = " + mFragment.isDetached());
            TiLog.v(TAG, "isDontKeepActivitiesEnabled = " + AndroidDeveloperOptions
                    .isDontKeepActivitiesEnabled(mFragment.getActivity()));

            final TiConfiguration config = mPresenter.getConfig();
            TiLog.v(TAG, "shouldRetain = " + config.shouldRetainPresenter());
            TiLog.v(TAG, "useStaticSavior = " + config.useStaticSaviorToRetain());
        }
    }
}