package net.grandcentrix.thirtyinch;


import net.grandcentrix.thirtyinch.internal.OnTimeRemovable;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.Fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents the Presenter of the popular Model-View-Presenter design pattern.
 */
public abstract class TiPresenter<V extends TiView> {

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
            + ":" + TiPresenter.class.getSimpleName()
            + "@" + Integer.toHexString(this.hashCode()));

    /**
     * used to check that lifecycle methods (starting with on..) cannot be called directly. i.e.
     * {@link #onCreate()} cannot be called. Instead use {@link #create()} which calls {@link
     * #onCreate()} and makes sure the Presenter has the correct state.
     */
    private boolean mCalled = true;

    private State mState = State.INITIALIZED;

    private V mView;

    public TiPresenter() {

    }

    /**
     * Observes the lifecycle state of this presenter.
     *
     * @param observer called when lifecycle state changes after the lifecycle method such as
     *                 {@link
     *                 #onCreate()} got called
     * @return a {@link Removable} allowing to remove the {@link TiLifecycleObserver} from the
     * {@link TiPresenter} before it reaches its termination state
     */
    public Removable addLifecycleObserver(final TiLifecycleObserver observer) {
        if (mState == State.DESTROYED) {
            throw new IllegalStateException("Don't add observers "
                    + "when the presenter reached the DESTROYED state. "
                    + "They wont get any new events anyways.");
        }

        mLifecycleObservers.add(observer);

        return new OnTimeRemovable() {

            @Override
            public void onRemove() {
                mLifecycleObservers.remove(observer);
            }
        };
    }

    // TODO check if this could be combined with #wakeUp

    /**
     * bind a new view to this presenter.
     *
     * @param view the new view, can't be null. To set the view to {@code null} call {@link
     *             #sleep()}
     */
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

        mView = view;
    }

    /**
     * @see #onCreate()
     */
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
     * Should be called when the view is about to die and will never come back.
     * <p/>
     * call this in {@link Fragment#onDestroy()} or {@link Activity#onDestroy()}
     *
     * @see #onDestroy()
     */
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

    /**
     * @return the view of this presenter
     */
    public V getView() {
        return mView;
    }

    public boolean isAwake() {
        return mState == State.VIEW_ATTACHED_AND_AWAKE;
    }

    public boolean isCreated() {
        return mState == State.CREATED_WITH_DETACHED_VIEW;
    }

    public boolean isDestroyed() {
        return mState == State.DESTROYED;
    }

    /**
     * call sleep as the opposite of {@link #wakeUp()}, when the view is not available anymore.
     * Calling sleep in {@code Fragment#onDestroyView()} makes sense because observing a discarded
     * view does not.
     *
     * @see #onSleep()
     */
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
     * when calling {@link #wakeUp()} the presenter can start communicating with the {@link
     * TiView}.
     *
     * @see #onWakeUp()
     */
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
