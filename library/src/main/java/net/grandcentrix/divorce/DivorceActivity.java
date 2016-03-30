package net.grandcentrix.divorce;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public abstract class DivorceActivity<V extends View> extends AppCompatActivity {

    private static final String TAG = DivorceActivity.class.getSimpleName();

    private Presenter<V> mPresenter;

    @Override
    public void onPause() {
        super.onPause();
        mPresenter.movedToBackground();
    }

    @Override
    public void onResume() {
        super.onResume();
        getWindow().getDecorView().post(new Runnable() {
            @Override
            public void run() {
                mPresenter.moveToForeground();
            }
        });
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        final Object object = super.onRetainCustomNonConfigurationInstance();
        return new RxPresenterNonConfigurationInstance<>(mPresenter, object);
    }

    @NonNull
    protected abstract V getRxView();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Object nci = getLastCustomNonConfigurationInstance();
        if (nci instanceof RxPresenterNonConfigurationInstance) {
            final RxPresenterNonConfigurationInstance pnci
                    = (RxPresenterNonConfigurationInstance) nci;
            //noinspection unchecked
            mPresenter = pnci.getPresenter();
        }

        if (mPresenter == null) {
            final Bundle activityExtras = getIntent().getExtras();
            mPresenter = onCreatePresenter(activityExtras);
            Log.d(TAG, "created Presenter: " + mPresenter);
        }

        mPresenter.bindNewView(getRxView());
        Log.d(TAG, "bound new View to Presenter: " + mPresenter);
    }

    @NonNull
    protected abstract Presenter<V> onCreatePresenter(
            @NonNull final Bundle activityIntentBundle);

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.destroy();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        mPresenter.bindNewView(getRxView());
        Log.d(TAG, "rebound View to Presenter: " + mPresenter);
    }

    @Override
    protected void onStart() {
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
        super.onStop();
        mPresenter.sleep();
    }
}
