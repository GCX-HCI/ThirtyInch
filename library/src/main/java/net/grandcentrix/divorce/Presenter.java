package net.grandcentrix.divorce;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.lang.reflect.Proxy;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Represents the Presenter of the popular Model-View-Presenter design pattern.
 * <p/>
 * The presenter connects the View V to a model which don't know each other. The View is passive
 * and provides this Presenter with events from the UI. It's an RxPresenter because it works with
 * {@link rx.Observable} from RxJava to communicate with the View.
 * <p/>
 * Created by pascalwelsch on 4/17/15.
 */
public abstract class Presenter<V extends View> {

    private final String TAG = this.getClass().getSimpleName()
            + "@" + Integer.toHexString(this.hashCode())
            + ":" + Presenter.class.getSimpleName();

    private boolean mCalled = true;

    private boolean mCreated = false;

    private boolean mDestroyed = false;

    private boolean mInForeground = false;

    private CompositeSubscription mPresenterSubscriptions = new CompositeSubscription();

    private CompositeSubscription mUiSubscriptions = new CompositeSubscription();

    private V mView;

    private BehaviorSubject<Boolean> mViewReady = BehaviorSubject.create(false);

    public Presenter() {
    }

    public void bindNewView(final V view) {
        bindNewView(view, UiPlatform.PLAIN_JAVA);
    }

    public void bindNewView(final V view, final UiPlatform platform) {
        if (UiPlatform.ANDROID.equals(platform)) {
            Class<?> foundInterfaceClass = getInterfaceOfClassExtendingGivenInterface(
                    view.getClass(), View.class);
            if (foundInterfaceClass == null) {
                Log.w(TAG, "the interface extending RxMvpView could not be found");
                mView = view;
                return;
            }
            //noinspection unchecked,UnnecessaryLocalVariable
            final V wrappedView = (V) Proxy.newProxyInstance(
                    foundInterfaceClass.getClassLoader(), new Class<?>[]{foundInterfaceClass},
                    new CallOnAndroidMainThreadInvocationHandler<>(view));
            mView = wrappedView;
        } else {
            mView = view;
        }
    }

    public final void create() {
        if (mCreated) {
            Log.v(TAG, "not calling onCreate(), it was already called");
            return;
        }
        mCreated = true;
        mCalled = false;
        Log.v(TAG, "onCreate()");
        onCreate();
        if (!mCalled) {
            throw new SuperNotCalledException("Presenter " + this
                    + " did not call through to super.onCreate()");
        }
    }

    /**
     * Returns a transformer that will delay onNext, onError and onComplete emissions unless a view
     * become available. getRxView() is guaranteed to be != null during all emissions. This
     * transformer can only be used on application's main thread.
     * <p/>
     * If the transformer receives a next value while the previous value has not been delivered,
     * the
     * previous value will be dropped.
     * <p/>
     * The transformer will duplicate the latest onNext emission in case if a view has been
     * reattached.
     * <p/>
     * This operator ignores onComplete emission and never sends one.
     * <p/>
     * Use this operator when you need to show updatable data that needs to be cached in memory.
     *
     * @param <T> a type of onNext value.
     * @return the delaying operator.
     */
    public <T> Observable.Transformer<T, T> deliverLatestCacheToView() {
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> observable) {
                return observable.lift(OperatorSemaphore.<T>semaphoreLatestCache(isViewReady()));
            }
        };
    }

    /**
     * Returns a transformer that will delay onNext, onError and onComplete emissions unless a view
     * become available. getRxView() is guaranteed to be != null during all emissions. This
     * transformer can only be used on application's main thread.
     * <p/>
     * If this transformer receives a next value while the previous value has not been delivered,
     * the previous value will be dropped.
     * <p/>
     * Use this operator when you need to show updatable data.
     *
     * @param <T> a type of onNext value.
     * @return the delaying operator.
     */
    public <T> Observable.Transformer<T, T> deliverLatestToView() {
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> observable) {
                return observable.lift(OperatorSemaphore.<T>semaphoreLatest(isViewReady()));
            }
        };
    }

    /**
     * Returns a transformer that will delay onNext, onError and onComplete emissions unless a view
     * become available. getRxView() is guaranteed to be != null during all emissions. This
     * transformer can only be used on application's main thread. See the correct order:
     * <pre>
     * <code>
     *
     * .observeOn(AndroidSchedulers.mainThread())
     * .compose(this.&lt;T&gt;deliverToView())
     * </code>
     * </pre>
     * Use this operator if you need to deliver *all* emissions to a view, in example when you're
     * sending items into adapter one by one.
     *
     * @param <T> a type of onNext value.
     * @return the delaying operator.
     */
    public <T> Observable.Transformer<T, T> deliverToView() {
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> observable) {
                return observable.lift(OperatorSemaphore.<T>semaphore(isViewReady()));
            }
        };
    }

    /**
     * completes all observables of this presenter. Should be called when the view is about to die
     * and will never come back.
     * <p/>
     * call this in {@link Fragment#onDestroy()}
     * <p/>
     * complete all {@link rx.Observer}, i.e. BehaviourSubjects with {@link Observer#onCompleted()}
     * to unsubscribe all observers
     */
    public final void destroy() {
        if (!mCreated) {
            Log.v(TAG, "not calling onDestroy(), destroy was already called");
            return;
        }
        mViewReady.onNext(false);
        mPresenterSubscriptions.unsubscribe();
        mPresenterSubscriptions = new CompositeSubscription();
        mDestroyed = true;
        mCalled = false;
        Log.v(TAG, "onDestroy()");
        onDestroy();
        if (!mCalled) {
            throw new SuperNotCalledException("Presenter " + this
                    + " did not call through to super.onDestroy()");
        }
    }

    public final void moveToForeground() {
        if (mInForeground) {
            Log.v(TAG, "not calling onMoveToForeground(), not in background");
            return;
        }
        mInForeground = true;
        mCalled = false;
        Log.v(TAG, "onMoveToForeground()");
        onMoveToForeground();
        if (!mCalled) {
            throw new SuperNotCalledException("Presenter " + this
                    + " did not call through to super.onMoveToForeground()");
        }
    }

    public final void movedToBackground() {
        if (!mInForeground) {
            Log.v(TAG, "not calling onMoveToForeground(), already in background");
            return;
        }
        mInForeground = false;
        mCalled = false;
        Log.v(TAG, "onMovedToBackground()");
        onMovedToBackground();
        if (!mCalled) {
            throw new SuperNotCalledException("Presenter " + this
                    + " did not call through to super.onMovedToBackground()");
        }
    }

    /**
     * call sleep as the opposite of {@link #wakeUp()} to unsubscribe all observers listening to
     * the
     * UI observables of the view. Calling sleep in {@link Fragment#onDestroyView()} makes sense
     * because observing a discarded view does not.
     */
    public final void sleep() {
        if (!mViewReady.getValue()) {
            Log.v(TAG, "not calling onSleep(), not woken up");
            return;
        }
        mViewReady.onNext(false);
        // unsubscribe all UI subscriptions created in wakeUp() and added
        // via manageViewSubscription(Subscription)
        mUiSubscriptions.unsubscribe();
        // there is no reuse possible. recreation works fine
        mUiSubscriptions = new CompositeSubscription();
        mCalled = false;
        Log.v(TAG, "onSleep()");
        onSleep();
        if (!mCalled) {
            throw new SuperNotCalledException("Presenter " + this
                    + " did not call through to super.onSleep()");
        }

        mView = null;
    }

    @Override
    public String toString() {
        final String viewName;
        if (getView() != null) {
            viewName = getView().toString();
        } else {
            viewName = "null";
        }
        return getClass().getSimpleName() + "@" + hashCode() + "{"
                + "view = " + viewName
                + "}";
    }

    /**
     * when calling wakeUp the presenter starts to observe the observables of the View.
     * <p/>
     * Call this in a Fragment after {@link Fragment#onCreateView(LayoutInflater, ViewGroup,
     * Bundle)} and after you created and published all observables the presenter will use. At the
     * end of {@link Fragment#onViewCreated(android.view.View, Bundle)} is an appropriate place.
     */
    public final void wakeUp() {
        if (mViewReady.getValue()) {
            Log.v(TAG, "not calling onWakeUp(), already woken up");
            return;
        }
        mCalled = false;
        Log.v(TAG, "onWakeUp()");
        onWakeUp();
        if (!mCalled) {
            throw new SuperNotCalledException("Presenter " + this
                    + " did not call through to super.onWakeUp()");
        }
        mViewReady.onNext(true);
    }

    /**
     * @return the view of this presenter
     */
    protected V getView() {
        return mView;
    }

    protected void manageSubscription(final Subscription subscription) {
        if (subscription.isUnsubscribed()) {
            return;
        }
        if (mDestroyed) {
            subscription.unsubscribe();
        }
        mPresenterSubscriptions.add(subscription);
    }

    /**
     * add your subscriptions for View events to this method to get them automatically cleaned up
     * in
     * {@link #sleep()}. typically call this in {@link #wakeUp()} where you subscribe to the UI
     * events
     */
    protected void manageViewSubscription(final Subscription subscription) {
        mUiSubscriptions.add(subscription);
    }

    /**
     * the first lifecycle method after the presenter was created. This will be called only once!
     * The view is not attached at this state. But doing network requests is possible at this
     * state.
     * Use {@link #deliverToView()} to make sure you don't try to touch the view before it is
     * ready.
     */
    protected void onCreate() {
        if (mCalled) {
            throw new IllegalAccessError("don't call #onCreate() directly, call #create()");
        }
        mCalled = true;
    }

    /**
     * this Presenter is about to die. make a complete cleanup and don't leak anything. i.e.
     * complete Subjects
     */
    protected void onDestroy() {
        if (mCalled) {
            throw new IllegalAccessError("don't call #onDestroy() directly, call #destroy()");
        }
        mCalled = true;
    }

    /**
     * the view is now visible to the user. Good point to start battery intensive background tasks
     * like GPS
     */
    protected void onMoveToForeground() {
        if (mCalled) {
            throw new IllegalAccessError(
                    "don't call #onMoveToForeground() directly, call #moveToForeground()");
        }
        mCalled = true;
    }

    /**
     * the view is now in the background and not visible to the user.
     */
    protected void onMovedToBackground() {
        if (mCalled) {
            throw new IllegalAccessError(
                    "don't call #onMovedToBackground() directly, call #movedToBackground()");
        }
        mCalled = true;
    }

    protected void onSleep() {
        if (mCalled) {
            throw new IllegalAccessError("don't call #onSleep() directly, call #sleep()");
        }
        mCalled = true;
    }

    protected void onWakeUp() {
        if (mCalled) {
            throw new IllegalAccessError("don't call #onWakeUp() directly, call #wakeup()");
        }
        mCalled = true;
    }

    /**
     * Observable of the view state. The View is ready to receive calls after calling {@link
     * #wakeUp()} and before calling {@link #sleep()}.
     */
    private Observable<Boolean> isViewReady() {
        return mViewReady.asObservable().distinctUntilChanged();
    }

    private static Class<?> getInterfaceOfClassExtendingGivenInterface(
            final Class<?> possibleExtendingClass,
            final Class<?> givenInterface) {
        if (!givenInterface.isAssignableFrom(possibleExtendingClass)) {
            // not possible
            return null;
        }

        // assignable, find the interface
        Class<?> viewClass = possibleExtendingClass;
        while (viewClass != null) {
            final Class<?>[] interfaces = viewClass.getInterfaces();
            for (final Class<?> clazz : interfaces) {
                if (givenInterface.isAssignableFrom(clazz)) {
                    return clazz;
                }
            }

            // check super
            viewClass = viewClass.getSuperclass();
        }

        // should never happen
        return null;
    }
}
