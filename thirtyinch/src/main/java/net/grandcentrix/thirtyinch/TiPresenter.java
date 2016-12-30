/*
 * Copyright (C) 2016 grandcentrix GmbH
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


import net.grandcentrix.thirtyinch.internal.OneTimeRemovable;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.Fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

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
         * transitions to {@link #VIEW_ATTACHED} or the presenter gets destroyed ->
         * {@link
         * #DESTROYED}
         */
        VIEW_DETACHED,
        /**
         * the view is attached. In any case, the next step will be {@link
         * #VIEW_DETACHED}
         */
        VIEW_ATTACHED,
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

    private final String TAG = this.getClass().getSimpleName()
            + ":" + TiPresenter.class.getSimpleName()
            + "@" + Integer.toHexString(this.hashCode());

    /**
     * used to check that lifecycle methods (starting with on..) cannot be called directly. i.e.
     * {@link #onCreate()} cannot be called. Instead use {@link #create()} which calls {@link
     * #onCreate()} and makes sure the Presenter has the correct state.
     */
    private boolean mCalled = true;

    private final TiConfiguration mConfig;

    private String mId;

    private final ExecutorService mPersistentStateExecutorService =
            Executors.newSingleThreadExecutor();

    private Future<byte[]> mPersistentStateFuture;

    private LinkedBlockingQueue<ViewAction<V>> mPostponedViewActions = new LinkedBlockingQueue<>();

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

        return new OneTimeRemovable() {

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
     *             #detachView()}
     */
    public void attachView(@NonNull final V view) {

        //noinspection ConstantConditions
        if (view == null) {
            throw new IllegalStateException(
                    "the view cannot be set to null. Call #detachView() instead");
        }

        if (isDestroyed()) {
            throw new IllegalStateException(
                    "The presenter is already in it's terminal state and waits for garbage collection. "
                            + "Binding a view is not allowed");
        }

        if (isViewAttached()) {
            if (view.equals(mView)) {
                TiLog.v(TAG, "not calling onAttachView(), view already attached");
                return;
            } else {
                throw new IllegalStateException(
                        "a view is already attached, call #detachView first");
            }
        }

        if (!isInitialized()) {
            throw new IllegalStateException("Presenter is not created, call #create() first");
        }

        mView = view;
        moveToState(State.VIEW_ATTACHED, false);
        mCalled = false;
        TiLog.v(TAG, "onAttachView(TiView)");
        onAttachView(view);
        if (!mCalled) {
            throw new SuperNotCalledException("Presenter " + this
                    + " did not call through to super.onAttachView(TiView)");
        }
        mCalled = false;
        TiLog.v(TAG, "deprecated onWakeUp()");
        onWakeUp();
        if (!mCalled) {
            throw new SuperNotCalledException("Presenter " + this
                    + " did not call through to super.onWakeUp()");
        }
        moveToState(State.VIEW_ATTACHED, true);
    }

    /**
     * Initializes the presenter. This is like the constructor. Keeping things separate allows
     * manually injecting fields in test cases after initializing the presenter and then start the
     * work with {@link #create()}
     *
     * @see #onCreate()
     */
    public final void create() {
        if (isInitialized()) {
            TiLog.w(TAG, "not calling onCreate(), it was already called");
            return;
        }
        moveToState(State.VIEW_DETACHED, false);
        mCalled = false;
        TiLog.v(TAG, "onCreate()");
        onCreate();
        if (!mCalled) {
            throw new SuperNotCalledException("Presenter " + this
                    + " did not call through to super.onCreate()");
        }
        moveToState(State.VIEW_DETACHED, true);
    }

    /**
     * Should be called when the view is about to die and will never come back.
     * <p/>
     * call this in {@link Fragment#onDestroy()} or {@link Activity#onDestroy()}
     *
     * @see #onDestroy()
     */
    public final void destroy() {
        if (!isInitialized() || isDestroyed()) {
            TiLog.w(TAG, "not calling onDestroy(), destroy was already called");
            return;
        }

        moveToState(State.DESTROYED, false);
        mCalled = false;
        TiLog.v(TAG, "onDestroy()");
        onDestroy();
        if (!mCalled) {
            throw new SuperNotCalledException("Presenter " + this
                    + " did not call through to super.onDestroy()");
        }
        moveToState(State.DESTROYED, true);

        // release everything, no new states will be posted
        mLifecycleObservers.clear();

        final TiPresenterSerializer serializer = getConfig().getPresenterSerializer();
        if (serializer != null) {
            serializer.free(this);
        }
    }

    /**
     * call detachView as the opposite of {@link #attachView(TiView)}, when the view is not
     * available anymore.
     * Calling detachView in {@code Fragment#onDestroyView()} makes sense because observing a
     * discarded view does not.
     *
     * @see #onSleep()
     */
    public final void detachView() {
        if (!isViewAttached()) {
            TiLog.v(TAG, "not calling onDetachView(), not woken up");
            return;
        }
        moveToState(State.VIEW_DETACHED, false);
        mCalled = false;
        TiLog.v(TAG, "deprecated onSleep()");
        onSleep();
        if (!mCalled) {
            throw new SuperNotCalledException("Presenter " + this
                    + " did not call through to super.onSleep()");
        }
        mCalled = false;
        TiLog.v(TAG, "onDetachView()");
        onDetachView();
        if (!mCalled) {
            throw new SuperNotCalledException("Presenter " + this
                    + " did not call through to super.onDetachView()");
        }

        moveToState(State.VIEW_DETACHED, true);
        mView = null;
    }

    public void generatNewId() {
        final String id = getClass().getSimpleName() + ":" + hashCode() + ":" + System.nanoTime();
        setId(id);
    }

    /**
     * @return the presenter configuration
     */
    @NonNull
    public TiConfiguration getConfig() {
        return mConfig;
    }

    /**
     * @return A unique id of this instance.
     */
    public final String getId() {
        return mId;
    }

    /**
     * @return the current lifecycle state
     */
    @NonNull
    public State getState() {
        return mState;
    }

    /**
     * Returns the currently attached view. The view is attached between the lifecycle callbacks
     * {@link #onAttachView(TiView)} and {@link #onSleep()}.
     * <p>
     * If you don't care about the view being attached or detached you should either rethink your
     * architecture or use {@link #sendToView(ViewAction)} where the action will be executed when
     * the view is attached.
     *
     * @return the currently attached view of this presenter, {@code null} when no view is attached.
     */
    @Nullable
    public V getView() {
        return mView;
    }

    public boolean isDestroyed() {
        return mState == State.DESTROYED;
    }

    public boolean isInitialized() {
        return mState == State.VIEW_DETACHED;
    }

    public boolean isViewAttached() {
        return mState == State.VIEW_ATTACHED;
    }

    //TODO documentation
    public void persistState() {
        mPersistentStateExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                TiPresenterSerializer serializer = mConfig.getPresenterSerializer();
                if (serializer != null) {
                    final byte[] data = onSavePersistentState();
                    serializer.serialize(TiPresenter.this, data);
                }
            }
        });
    }

    @NonNull
    public Future<byte[]> prefetchPersistentState() {
        mPersistentStateFuture = mPersistentStateExecutorService.submit(new Callable<byte[]>() {
            @Override
            public byte[] call() throws Exception {
                final TiPresenterSerializer serializer = mConfig.getPresenterSerializer();
                if (serializer != null) {
                    return serializer.deserialize(TiPresenter.this);
                }
                return null;
            }
        });

        return mPersistentStateFuture;
    }

    /**
     * the id can only be set once
     */
    public void setId(@NonNull final String id) {
        //noinspection ConstantConditions
        if (id == null) {
            throw new IllegalArgumentException("the id cannot be null");
        }
        if (mId == null) {
            mId = id;
        } else {
            throw new IllegalArgumentException("the id can only be set once");
        }
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

    //TODO documentation
    @Nullable
    protected byte[] getPersistentState() {
        Future<byte[]> future = mPersistentStateFuture;
        if (future == null) {
            future = mPersistentStateFuture = prefetchPersistentState();
        }
        try {
            // wait for result even when not prefetched
            return future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gives access to the postponed actions while the view is not attached.
     *
     * @return the queued actions
     */
    protected Queue<ViewAction<V>> getQueuedViewActions() {
        return mPostponedViewActions;
    }

    /**
     * The view is now attached and ready to receive events.
     *
     * @see #onDetachView()
     * @see #attachView(TiView)
     */
    protected void onAttachView(@NonNull V view) {
        if (mCalled) {
            throw new IllegalAccessError(
                    "don't call #onAttachView(TiView) directly, call #attachView(TiView)");
        }
        mCalled = true;

        // send all queued actions since the view was detached to the new view.
        // It's part of the super call because there might be usecases where the implementer
        // wants to execute actions on the view before executing the queued ones.
        sendPostponedActionsToView(view);
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
     * @see #onAttachView(TiView)
     * @see #detachView()
     */
    protected void onDetachView() {
        if (mCalled) {
            throw new IllegalAccessError("don't call #onDetachView() directly, call #detachView()");
        }
        mCalled = true;
    }

    //TODO documentation
    @Nullable
    protected byte[] onSavePersistentState() {
        return null;
    }

    /**
     * @deprecated use {@link #onDetachView()} instead
     */
    @Deprecated
    protected void onSleep() {
        if (mCalled) {
            throw new IllegalAccessError("don't call #onSleep() directly, call #detachView()");
        }
        mCalled = true;
    }

    /**
     * @deprecated use {@link #onAttachView(TiView)} instead
     */
    @Deprecated
    protected void onWakeUp() {
        if (mCalled) {
            throw new IllegalAccessError(
                    "don't call #onWakeUp() directly, call #attachView(TiView)");
        }
        mCalled = true;
    }

    /**
     * Executes the {@link ViewAction} when the view is available.
     * Once a view is attached the actions get called in the same order they have been added.
     * When the view is already attached the action will be executed immediately.
     * <p>
     * This method might be very useful for single actions which invoke function like {@link
     * Activity#finish()}, {@link Activity#startActivity(Intent)} or showing a {@link
     * android.widget.Toast} in the view.
     * <p>
     * <b>But don't overuse it.</b>
     * The action will only be called <b>once</b>.
     * When a new view attaches (after a configuration change) it doesn't know about the previously
     * sent actions.
     * If your using this method too often you should rethink your architecture.
     * A model which can be bound to the view in {@link #onAttachView(TiView)} and when changes
     * happen might be a better solution.
     * See the <a href="https://github.com/passsy/thirtyinch-sample">thirtyinch-sample</a> project
     * for ideas.
     *
     * @see #sendPostponedActionsToView
     * @see #onAttachView(TiView)
     */
    protected void sendToView(ViewAction<V> action) {
        final V view = getView();
        if (view != null) {
            action.call(view);
        } else {
            mPostponedViewActions.add(action);
        }
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
                    if (newState == State.VIEW_DETACHED) {
                        // move allowed
                        break;
                    } else {
                        throw new IllegalStateException("Can't move to state " + newState
                                + ", the next state after INITIALIZED has to be VIEW_DETACHED");
                    }
                case VIEW_DETACHED:
                    if (newState == State.VIEW_ATTACHED) {
                        // move allowed
                        break;
                    } else if (newState == State.DESTROYED) {
                        // move allowed
                        break;
                    } else {
                        throw new IllegalStateException("Can't move to state " + newState
                                + ", the allowed states after VIEW_DETACHED are VIEW_ATTACHED or DESTROYED");
                    }
                case VIEW_ATTACHED:
                    // directly moving to DESTROYED is not possible, first detach the view
                    if (newState == State.VIEW_DETACHED) {
                        // move allowed
                        break;
                    } else {
                        throw new IllegalStateException("Can't move to state " + newState
                                + ", the next state after VIEW_ATTACHED has to be VIEW_DETACHED");
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

    /**
     * Executes all postponed view actions
     *
     * @param view where the actions will be sent to
     */
    private void sendPostponedActionsToView(V view) {
        while (!mPostponedViewActions.isEmpty()) {
            mPostponedViewActions.poll().call(view);
        }
    }
}
