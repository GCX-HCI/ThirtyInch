package net.grandcentrix.thirtyinch.android;

import net.grandcentrix.thirtyinch.BindViewInterceptor;
import net.grandcentrix.thirtyinch.Removable;
import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.TiView;
import net.grandcentrix.thirtyinch.android.internal.ActivityRetainedPresenterProvider;
import net.grandcentrix.thirtyinch.android.internal.AppCompatActivityProvider;
import net.grandcentrix.thirtyinch.android.internal.PresenterNonConfigurationInstance;
import net.grandcentrix.thirtyinch.android.internal.PresenterProvider;
import net.grandcentrix.thirtyinch.android.internal.TiActivityDelegate;
import net.grandcentrix.thirtyinch.android.internal.ViewProvider;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by pascalwelsch on 9/8/15.
 */
public abstract class TiActivity<P extends TiPresenter<V>, V extends TiView>
        extends AppCompatActivity implements PresenterProvider<P>, ViewProvider<V>,
        ActivityRetainedPresenterProvider<P>, AppCompatActivityProvider {

    private final TiActivityDelegate<P, V> mDelegate
            = new TiActivityDelegate<>(this, this, this, this);

    public Removable addBindViewInterceptor(final BindViewInterceptor interceptor) {
        return mDelegate.addBindViewInterceptor(interceptor);
    }

    @NonNull
    @Override
    public AppCompatActivity getAppCompatActivity() {
        return this;
    }

    @Nullable
    public P getRetainedPresenter() {
        // try recover presenter via lastNonConfigurationInstance
        // this works most of the time
        final Object nci = getLastCustomNonConfigurationInstance();
        if (nci instanceof PresenterNonConfigurationInstance) {
            final PresenterNonConfigurationInstance pnci = (PresenterNonConfigurationInstance) nci;
            //noinspection unchecked
            return (P) pnci.getPresenter();
        }
        return null;
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDelegate.onConfigurationChanged(newConfig);
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return new PresenterNonConfigurationInstance<>(mDelegate.getPresenter(),
                super.onRetainCustomNonConfigurationInstance());
    }

    /**
     * the default implementation assumes that the activity is the view and implements the {@link
     * TiView} interface. Override this method for a different behaviour.
     *
     * @return the object implementing the TiView interface
     */
    @NonNull
    @Override
    public V provideView() {
        return mDelegate.provideView();
    }

    @Override
    public String toString() {
        String presenter = mDelegate.getPresenter() == null ? "null" :
                mDelegate.getPresenter().getClass().getSimpleName()
                        + "@" + Integer.toHexString(mDelegate.getPresenter().hashCode());

        return getClass().getSimpleName() + "@" + Integer.toHexString(hashCode())
                + "{presenter=" + presenter + "}";
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDelegate.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDelegate.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        mDelegate.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        mDelegate.onStart_beforeSuper();
        super.onStart();
        mDelegate.onStart_afterSuper();
    }

    @Override
    protected void onStop() {
        mDelegate.onStop_beforeSuper();
        super.onStop();
        mDelegate.onStop_afterSuper();
    }
}
