/*
 * Copyright (C) 2015 grandcentrix GmbH
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
 * Represents the Presenter of the popular Model-View-Presenter design pattern. If used with {@link
 * TiActivity} or {@link TiFragment} this presenter survives configuration changes.
 *
 * @see TiConfiguration
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

    private static TiConfiguration sDefaultConfig = TiConfiguration.DEFAULT;

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

    private final TiConfiguration mConfig;

    private State mState = State.INITIALIZED;

    private V mView;

    public static void setDefaultConfig(final TiConfiguration config) {
        sDefaultConfig = config;
    }


    public TiPresenter() {
        this(sDefaultConfig);
    }

    /**
     * Constructs a presenter with a different configuration then the default one. Change the
     * default configuration with {@link #setDefaultConfig(TiConfiguration)}
     */
    public TiPresenter(final TiConfiguration config) {
        mConfig = config;
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


    /**
     * bind a new view to this presenter.
     *
     * @param view the new view, can't be null. To set the view to {@code null} call {@link
     *             #sleep()}
     * @see #wakeUp()
     */
    // TODO check if this could be combined with #wakeUp
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
     * Initializes the presenter. This is like the constructor. Keeping things separate allows
     * manually injecting fields in test cases after initializing the presenter and then start the
     * work with {@link #create()}
     *
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
     * @return the presenter configuration
     */
    @NonNull
    public TiConfiguration getConfig() {
        return mConfig;
    }

    /**
     * @return the current lifecycle state
     */
    @NonNull
    public State getState() {
        return mState;
    }

    /**
     * @return the currently attached view of this presenter
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
        return getClass().getSimpleName()
                + ":" + TiPresenter.class.getSimpleName()
                + "@" + Integer.toHexString(hashCode())
                + "{view = " + viewName + "}";
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
     *
     * @see #create()
     * @see #onDestroy()
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
     *
     * @see #destroy()
     * @see #onCreate()
     */
    protected void onDestroy() {
        if (mCalled) {
            throw new IllegalAccessError("don't call #onDestroy() directly, call #destroy()");
        }
        mCalled = true;
    }

    /**
     * Right after this method the view will be detached. {@link #getView()} will return
     * <code>null</code> afterwards.
     *
     * @see #sleep()
     */
    protected void onSleep() {
        if (mCalled) {
            throw new IllegalAccessError("don't call #onSleep() directly, call #sleep()");
        }
        mCalled = true;
    }

    /**
     * The view is now attached and ready to receive events. {@link #getView()} is not guaranteed
     * to
     * be not <code>null</code>
     *
     * @see #wakeUp()
     * @see #onSleep()
     */
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
