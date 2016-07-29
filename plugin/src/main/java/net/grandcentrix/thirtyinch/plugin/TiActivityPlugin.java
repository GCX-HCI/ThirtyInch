package net.grandcentrix.thirtyinch.plugin;

import com.pascalwelsch.compositeandroid.activity.ActivityPlugin;
import com.pascalwelsch.compositeandroid.activity.CompositeNonConfigurationInstance;

import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.TiView;
import net.grandcentrix.thirtyinch.android.internal.CallOnMainThreadViewWrapper;
import net.grandcentrix.thirtyinch.android.internal.PresenterProvider;
import net.grandcentrix.thirtyinch.internal.PresenterSavior;
import net.grandcentrix.thirtyinch.util.AnnotationUtil;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

public class TiActivityPlugin<P extends TiPresenter<V>, V extends TiView>
        extends ActivityPlugin {

    private static final String SAVED_STATE_PRESENTER_ID = "presenter_id";

    public static final String NCI_KEY_PRESENTER = "presenter";

    private final String TAG = this.getClass().getSimpleName()
            + "@" + Integer.toHexString(this.hashCode())
            + ":" + TiActivityPlugin.class.getSimpleName();

    private volatile boolean mActivityStarted = false;

    private V mLastView;

    private boolean mNewConfig;

    private P mPresenter;

    private String mPresenterId;

    private PresenterProvider<P> mPresenterProvider;

    /**
     * Binds a {@link TiPresenter} returned by the {@link PresenterProvider} to the {@link
     * Activity} and all future {@link Activity} instances created due to configuration changes.
     * The provider will be only called once during {@link TiActivityPlugin#onCreate(Bundle)}. This
     * lets you inject objects which require a {@link android.content.Context} and can't be
     * instantiated in the constructor of the {@link Activity}. Using the interface also prevents
     * instantiating the (possibly) heavy {@link TiPresenter} which will never be used when a
     * presenter is already created for this {@link Activity}.
     *
     * @param presenterProvider callback returning the presenter.
     */
    public TiActivityPlugin(@NonNull final PresenterProvider<P> presenterProvider) {
        mPresenterProvider = presenterProvider;
    }

    public P getPresenter() {
        return mPresenter;
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
        if (nci instanceof TiPresenter) {
            //noinspection unchecked
            mPresenter = (P) nci;
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
            mPresenter = mPresenterProvider.providePresenter();
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
        String presenter = mPresenter == null ? "null" :
                mPresenter.getClass().getSimpleName()
                        + "@" + Integer.toHexString(mPresenter.hashCode());

        return getClass().getSimpleName() + "@" + Integer.toHexString(hashCode())
                + "{presenter=" + presenter + "}";
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
                .getInterfaceOfClassExtendingGivenInterface(getActivity().getClass(), TiView.class);

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
                return (V) getActivity();
            }
        }
    }
}
