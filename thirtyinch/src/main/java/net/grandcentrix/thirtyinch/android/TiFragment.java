package net.grandcentrix.thirtyinch.android;

import net.grandcentrix.thirtyinch.BindViewInterceptor;
import net.grandcentrix.thirtyinch.OnTimeRemovable;
import net.grandcentrix.thirtyinch.Removable;
import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.TiView;
import net.grandcentrix.thirtyinch.android.internal.PresenterProvider;
import net.grandcentrix.thirtyinch.internal.PresenterSavior;
import net.grandcentrix.thirtyinch.util.AnnotationUtil;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public abstract class TiFragment<P extends TiPresenter<V>, V extends TiView>
        extends Fragment implements PresenterProvider<P>, TiView {

    private static final String SAVED_STATE_PRESENTER_ID = "presenter_id";

    private final String TAG = this.getClass().getSimpleName()
            + "@" + Integer.toHexString(this.hashCode())
            + ":" + TiFragment.class.getSimpleName();

    private volatile boolean mActivityStarted = false;

    private List<BindViewInterceptor> mBindViewInterceptors = new ArrayList<>();

    /**
     * the cached version of the view send to the presenter after it passed the interceptors
     */
    private V mLastView;

    private P mPresenter;

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
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        Log.v(TAG, "onAttach()");

        if (mPresenter == null) {
            mPresenter = providePresenter();
            Log.d(TAG, "created Presenter: " + mPresenter);
            mPresenter.create();
        }
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate(" + savedInstanceState + ")");
        setRetainInstance(true);

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
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVED_STATE_PRESENTER_ID, mPresenterId);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.v(TAG, "onStart()");
        mActivityStarted = true;

        if (isUiPossible()) {
            bindViewToPresenter();
            getActivity().getWindow().getDecorView().post(new Runnable() {
                @Override
                public void run() {
                    if (isUiPossible() && mActivityStarted) {
                        mPresenter.wakeUp();
                    }
                }
            });
        }
    }

    @Override
    public void onStop() {
        Log.v(TAG, "onStop()");
        mActivityStarted = false;
        mPresenter.sleep();
        super.onStop();
    }

    @Override
    final public void setRetainInstance(final boolean retain) {
        super.setRetainInstance(true);
    }

    /**
     * the default implementation assumes that the fragment is the view and implements the {@link
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
                return (V) this;
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState) {
        mLastView = null;
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    /**
     * binds the view (this Fragment) to the {@link #mPresenter}. Allows interceptors to change,
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

    private boolean isUiPossible() {
        return isAdded() && !isDetached();
    }
}