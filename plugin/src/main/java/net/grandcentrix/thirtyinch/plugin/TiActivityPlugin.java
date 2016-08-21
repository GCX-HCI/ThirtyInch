package net.grandcentrix.thirtyinch.plugin;

import com.pascalwelsch.compositeandroid.activity.ActivityPlugin;
import com.pascalwelsch.compositeandroid.activity.CompositeNonConfigurationInstance;

import net.grandcentrix.thirtyinch.BindViewInterceptor;
import net.grandcentrix.thirtyinch.Removable;
import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.TiView;
import net.grandcentrix.thirtyinch.android.internal.ActivityRetainedPresenterProvider;
import net.grandcentrix.thirtyinch.android.internal.AppCompatActivityProvider;
import net.grandcentrix.thirtyinch.android.internal.PresenterProvider;
import net.grandcentrix.thirtyinch.android.internal.TiActivityDelegate;
import net.grandcentrix.thirtyinch.android.internal.ViewProvider;
import net.grandcentrix.thirtyinch.internal.TiPresenterLogger;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class TiActivityPlugin<P extends TiPresenter<V>, V extends TiView>
        extends ActivityPlugin implements ActivityRetainedPresenterProvider<P>, ViewProvider<V>,
        AppCompatActivityProvider, TiPresenterLogger {

    public static final String NCI_KEY_PRESENTER = "presenter";

    private String TAG = this.getClass().getSimpleName()
            + "@" + Integer.toHexString(this.hashCode());

    private TiActivityDelegate<P, V> mDelegate;

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
        mDelegate = new TiActivityDelegate<>(this, this, presenterProvider, this, this);
    }

    public Removable addBindViewInterceptor(final BindViewInterceptor interceptor) {
        return mDelegate.addBindViewInterceptor(interceptor);
    }

    @NonNull
    @Override
    public AppCompatActivity getAppCompatActivity() {
        // getOriginal is null until the plugin is attached.
        return getOriginal();
    }

    public P getPresenter() {
        return mDelegate.getPresenter();
    }

    @Nullable
    @Override
    public P getRetainedPresenter() {
        final Object nci = getLastNonConfigurationInstance(NCI_KEY_PRESENTER);
        if (nci instanceof CompositeNonConfigurationInstance) {
            final CompositeNonConfigurationInstance cnci = (CompositeNonConfigurationInstance) nci;
            //noinspection unchecked
            return (P) cnci.getNonConfigurationInstance();
        }
        return null;
    }

    @Override
    public void log(final String msg) {
        Log.v(TAG, msg);
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDelegate.onConfigurationChanged_afterSuper(newConfig);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDelegate.onCreate_afterSuper(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDelegate.onDestroy_afterSuper();
    }

    @Override
    public CompositeNonConfigurationInstance onRetainNonConfigurationInstance() {
        return new CompositeNonConfigurationInstance(NCI_KEY_PRESENTER, mDelegate.getPresenter());
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        mDelegate.onSaveInstanceState_afterSuper(outState);
    }

    @Override
    public void onStart() {
        mDelegate.onStart_beforeSuper();
        super.onStart();
        mDelegate.onStart_afterSuper();
    }

    @Override
    public void onStop() {
        mDelegate.onStop_beforeSuper();
        super.onStop();
        mDelegate.onStop_afterSuper();
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
    protected void onAddedToDelegate() {
        super.onAddedToDelegate();
        TAG = this.getClass().getSimpleName()
                + "@" + Integer.toHexString(this.hashCode())
                + ":" + getOriginal().getClass().getSimpleName()
                + "@" + Integer.toHexString(getOriginal().hashCode());
    }

    @Override
    protected void onRemovedFromDelegated() {
        super.onRemovedFromDelegated();
        TAG = this.getClass().getSimpleName()
                + "@" + Integer.toHexString(this.hashCode());
    }
}
