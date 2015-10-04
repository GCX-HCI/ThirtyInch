package net.grandcentrix.rxmvp;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;

/**
 * Created by pascalwelsch on 9/9/15.
 */
public abstract class RxMvpFragment<V extends RxMvpView> extends Fragment implements RxMvpView {

    private static final String TAG = RxMvpFragment.class.getSimpleName();

    private RxMvpPresenter<V> mPresenter;

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().getWindow().getDecorView().post(new Runnable() {
            @Override
            public void run() {
                mPresenter.wakeUp();
            }
        });
    }

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);

        if (mPresenter == null) {
            final Bundle activityExtras = activity.getIntent().getExtras();
            mPresenter = onCreatePresenter(activityExtras, getArguments());
            Log.d(TAG, "created Presenter: " + mPresenter);
        }

        mPresenter.bindView(getRxView());
        Log.d(TAG, "bound new View to Presenter: " + mPresenter);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onDestroy() {
        mPresenter.destroy();
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        mPresenter.sleep();
        super.onDestroyView();
    }

    @Override
    final public void setRetainInstance(final boolean retain) {
        super.setRetainInstance(true);
    }

    @NonNull
    protected abstract V getRxView();

    @NonNull
    protected abstract RxMvpPresenter<V> onCreatePresenter(
            @NonNull final Bundle activityIntentBundle, @NonNull final Bundle fragmentArguments);
}
