package net.grandcentrix.thirtyinch;


import net.grandcentrix.thirtyinch.internal.DistinctUntilChangedViewWrapper;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import rx.Observable;
import rx.Observer;

/**
 * Represents the Presenter of the popular Model-View-Presenter design pattern.
 * <p/>
 * The presenter connects the View V to a model which don't know each other. The View is passive
 * and provides this Presenter with events from the UI. It's an Presenter because it works with
 * {@link Observable} from RxJava to communicate with the View.
 */
public abstract class TiPresenter<V extends TiView> implements
        net.grandcentrix.thirtyinch.internal.PresenterLifecycle<V> {

    /**
     * The LifecycleState of a {@link TiPresenter}
     */
    public enum State {
        /**
         * Initial state of the presenter before {@link #create()} got called
         */
        INITIALIZED,
        /**
         * presenter is running fine but has no attached view. Either it gets a view  and
         * transitions to {@link #VIEW_ATTACHED_AND_AWAKE} or the presenter gets destroyed ->
         * {@link
         * #DESTROYED}
         */
        CREATED_WITH_DETACHED_VIEW,
        /**
         * the view is attached. In any case, the next step will be {@link
         * #CREATED_WITH_DETACHED_VIEW}
         */
        VIEW_ATTACHED_AND_AWAKE,
        /**
         * termination state. It will never change again.
         */
        DESTROYED
    }

    /**
     * list of the added observers
     */
    @VisibleForTesting
    final List<TiLifecycleObserver> mLifecycleObservers = new ArrayList<>();

    Logger mLogger = Logger.getLogger(this.getClass().getSimpleName()
            + "@" + Integer.toHexString(this.hashCode())
            + ":" + TiPresenter.class.getSimpleName());

    /**
     * used to check that lifecycle methods (starting with on..) cannot be called directly. i.e.
     * {@link #onCreate()} cannot be called. Instead use {@link #create()} which calls {@link
     * #onCreate()} and makes sure the Presenter has the correct state.
     */
    private boolean mCalled = true;

    /**
     * reference to the last view which was provided with {@link #bindNewView(TiView)}
     */
    private WeakReference<V> mOriginalView;

    private State mState = State.INITIALIZED;

    private V mView;

    private WeakReference<V> mWrappedView;

    public TiPresenter() {

    }

    /**
     * Observes the lifecycle state of this presenter.
     *
     * @param observer called when lifecycle state changes after the lifecycle method such as
     *                 {@link
     *                 #onCreate()} got called
     * @return a {@link Removable} allowing to remove the {@link TiLifecycleObserver} from the
     * presenter
     */
    public Removable addLifecycleObserver(final TiLifecycleObserver observer) {
        if (mState == State.DESTROYED) {
            throw new IllegalStateException("Don't add observers "
                    + "when the presenter reached the DESTROYED state. "
                    + "They wont get any new events anyways.");
        }

        mLifecycleObservers.add(observer);
        final AtomicBoolean removed = new AtomicBoolean(false);

        return new Removable() {
            @Override
            public boolean isRemoved() {
                return removed.get();
            }

            @Override
            public void remove() {
                // allow calling remove only once
                if (removed.compareAndSet(false, true)) {
                    mLifecycleObservers.remove(observer);
                }
            }
        };
    }

    @Override
    public void bindNewView(@NonNull final V view) {

        if (!isCreated()) {
            throw new IllegalStateException("Presenter is not created, call #create() first");
        }

        if (isAwake()) {
            throw new IllegalStateException(
                    "Can't bind new view, Presenter #wakeUp() already called. First call #sleep()");
        }

        if (isDestroyed()) {
            throw new IllegalStateException(
                    "The presenter is already in it's terminal state and waits for garbage collection. "
                            + "Binding a view is not allowed");
        }

        if (view == null) {
            throw new IllegalStateException(
                    "the view cannot be set to null. Call #sleep() instead");
        }

        // check if view has changed
        if (mWrappedView == null || mWrappedView.get() == null
                || mOriginalView == null || mOriginalView.get() == null
                || !mOriginalView.get().equals(view)) {

            // safe the original view to detect a change
            mOriginalView = new WeakReference<>(view);

            // proxy the view for the distinct until change feature
            final V wrappedView = DistinctUntilChangedViewWrapper.wrap(view);

            // safe the wrapped view. The detection of distinct until changed happens inside the
            // proxy. wrapping it again for every bindView would break the feature
            // will be reused when view did not change
            mWrappedView = new WeakReference<>(wrappedView);
            mView = wrappedView;
        } else {
            mView = mWrappedView.get();
        }
    }

    @Override
    public final void create() {
        if (isCreated()) {
            mLogger.log(Level.WARNING, "not calling onCreate(), it was already called");
            return;
        }
        moveToState(State.CREATED_WITH_DETACHED_VIEW, false);
        mCalled = false;
        mLogger.log(Level.FINE, "onCreate()");
        onCreate();
        if (!mCalled) {
            throw new SuperNotCalledException("Presenter " + this
                    + " did not call through to super.onCreate()");
        }
        moveToState(State.CREATED_WITH_DETACHED_VIEW, true);
    }

    /**
     * completes all observables of this presenter. Should be called when the view is about to die
     * and will never come back.
     * <p/>
     * call this in {@code Fragment#onDestroy()}
     * <p/>
     * complete all {@link Observer}, i.e. BehaviourSubjects with {@link Observer#onCompleted()}
     * to unsubscribe all observers
     */
    @Override
    public final void destroy() {
        if (!isCreated() || isDestroyed()) {
            mLogger.log(Level.WARNING, "not calling onDestroy(), destroy was already called");
            return;
        }

        moveToState(State.DESTROYED, false);
        mCalled = false;
        mLogger.log(Level.FINE, "onDestroy()");
        onDestroy();
        if (!mCalled) {
            throw new SuperNotCalledException("Presenter " + this
                    + " did not call through to super.onDestroy()");
        }
        moveToState(State.DESTROYED, true);

        // release everything, no new states will be posted
        mLifecycleObservers.clear();
    }

    /**
     * @return the current lifecycle state
     */
    public State getState() {
        return mState;
    }

    public boolean isDestroyed() {
        return mState == State.DESTROYED;
    }


    /**
     * call sleep as the opposite of {@link #wakeUp()} to unsubscribe all observers listening to
     * the
     * UI observables of the view. Calling sleep in {@code Fragment#onDestroyView()} makes sense
     * because observing a discarded view does not.
     */
    @Override
    public final void sleep() {
        if (!isAwake()) {
            mLogger.log(Level.FINE, "not calling onSleep(), not woken up");
            return;
        }
        moveToState(State.CREATED_WITH_DETACHED_VIEW, false);
        mCalled = false;
        mLogger.log(Level.FINE, "onSleep()");
        onSleep();
        if (!mCalled) {
            throw new SuperNotCalledException("Presenter " + this
                    + " did not call through to super.onSleep()");
        }

        mView = null;
        moveToState(State.CREATED_WITH_DETACHED_VIEW, true);
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
     * Call this in a Fragment after {@code Fragment#onCreateView(LayoutInflater, ViewGroup,
     * Bundle)} and after you created and published all observables the presenter will use. At the
     * end of {@code Fragment#onViewCreated(android.view.View, Bundle)} is an appropriate place.
     */
    @Override
    public final void wakeUp() {
        if (isAwake()) {
            mLogger.log(Level.FINE, "not calling onWakeUp(), already woken up");
            return;
        }
        moveToState(State.VIEW_ATTACHED_AND_AWAKE, false);
        mCalled = false;
        mLogger.log(Level.FINE, "onWakeUp()");
        onWakeUp();
        if (!mCalled) {
            throw new SuperNotCalledException("Presenter " + this
                    + " did not call through to super.onWakeUp()");
        }
        moveToState(State.VIEW_ATTACHED_AND_AWAKE, true);
    }

    /**
     * @return the view of this presenter
     */
    protected V getView() {
        return mView;
    }

    /**
     * the first lifecycle method after the presenter was created. This will be called only once!
     * The view is not attached at this state. But doing network requests is possible at this
     * state.
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

    private Boolean isAwake() {
        return mState == State.VIEW_ATTACHED_AND_AWAKE;
    }

    private boolean isCreated() {
        return mState == State.CREATED_WITH_DETACHED_VIEW;
    }


    /**
     * moves the presenter to the new state and validates the correctness of the transition
     *
     * @param newState the new state to set
     */
    private void moveToState(final State newState, final boolean hasLifecycleMethodBeenCalled) {
        final State oldState = mState;

        if (hasLifecycleMethodBeenCalled) {
            if (newState != oldState) {
                throw new IllegalStateException("first call moveToState(<state>, false);");
            }
        }

        if (newState != oldState) {
            switch (oldState) {
                case INITIALIZED:
                    if (newState == State.CREATED_WITH_DETACHED_VIEW) {
                        // move allowed
                        break;
                    } else {
                        throw new IllegalStateException("Can't move to state " + newState
                                + ", the next state after INITIALIZED has to be CREATED_WITH_DETACHED_VIEW");
                    }
                case CREATED_WITH_DETACHED_VIEW:
                    if (newState == State.VIEW_ATTACHED_AND_AWAKE) {
                        // move allowed
                        break;
                    } else if (newState == State.DESTROYED) {
                        // move allowed
                        break;
                    } else {
                        throw new IllegalStateException("Can't move to state " + newState
                                + ", the allowed states after CREATED_WITH_DETACHED_VIEW are VIEW_ATTACHED_AND_AWAKE or DESTROYED");
                    }
                case VIEW_ATTACHED_AND_AWAKE:
                    // directly moving to DESTROYED is not possible, first detach the view
                    if (newState == State.CREATED_WITH_DETACHED_VIEW) {
                        // move allowed
                        break;
                    } else {
                        throw new IllegalStateException("Can't move to state " + newState
                                + ", the next state after VIEW_ATTACHED_AND_AWAKE has to be CREATED_WITH_DETACHED_VIEW");
                    }
                case DESTROYED:
                    throw new IllegalStateException(
                            "once destroyed the presenter can't be moved to a different state");
            }

            mState = newState;
        }

        for (int i = 0; i < mLifecycleObservers.size(); i++) {
            mLifecycleObservers.get(i).onChange(newState, hasLifecycleMethodBeenCalled);
        }
    }
}
