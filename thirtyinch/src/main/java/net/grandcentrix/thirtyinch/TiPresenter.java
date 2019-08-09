/*
 * Copyright (C) 2017 grandcentrix GmbH
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


import static androidx.annotation.RestrictTo.Scope.SUBCLASSES;

import android.app.Activity;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.Fragment;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import net.grandcentrix.thirtyinch.internal.OneTimeRemovable;
import net.grandcentrix.thirtyinch.test.TiTestPresenter;

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

    private LinkedBlockingQueue<ViewAction<V>> mPostponedViewActions = new LinkedBlockingQueue<>();

    private State mState = State.INITIALIZED;

    /**
     * Executor for UI operations, must be set by the view implementation
     */
    @Nullable
    private Executor mUiThreadExecutor;

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
     * Observes the lifecycle state of this presenter. Observers get called in order they are
     * added for constructive events and in reversed order for destructive events. First in, last
     * out.
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
        moveToState(State.VIEW_ATTACHED, true);

        sendPostponedActionsToView(view);
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
        if (isViewAttached()) {
            throw new IllegalStateException(
                    "view is attached, can't destroy the presenter. First call detachView()");
        }

        if (!isInitialized() || isDestroyed()) {
            TiLog.v(TAG, "not calling onDestroy(), destroy was already called");
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
    }

    /**
     * call detachView as the opposite of {@link #attachView(TiView)}, when the view is not
     * available anymore.
     * Calling detachView in {@code Fragment#onDestroyView()} makes sense because observing a
     * discarded view does not.
     */
    public final void detachView() {
        if (!isViewAttached()) {
            TiLog.v(TAG, "not calling onDetachView(), not woken up");
            return;
        }
        moveToState(State.VIEW_DETACHED, false);
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
     * Gets the currently attached view. The view is attached between the lifecycle callbacks
     * {@link #onAttachView(TiView)} and {@link #onDetachView()}.
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

    /**
     * Gets the currently attached view or throws an {@link IllegalStateException} if the view
     * is not attached. Use this method if you are sure that a view is currently attached to the
     * presenter. If you're not sure you should better use {@link #sendToView(ViewAction)} where the
     * action will be executed when the view is attached.
     *
     * @return the currently attached view of this presenter
     */
    @NonNull
    public V getViewOrThrow() {
        final V view = getView();
        if (view == null) {
            throw new IllegalStateException(
                    "The view is currently not attached. Use 'sendToView(ViewAction)' instead.");
        }

        return view;
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

    /**
     * Runs the specified action on the UI thread. It only works when a view is attached
     * <p>
     * When you are looking for a way to execute code when the view got available in the future
     * have a look at {@link #sendToView(ViewAction)}
     *
     * @param action the action to run on the UI thread
     * @throws IllegalStateException when the executor is not available (most likely because the
     *                               view is not attached)
     */
    public void runOnUiThread(@NonNull final Runnable action) {
        if (mUiThreadExecutor != null) {
            mUiThreadExecutor.execute(action);
        } else {
            if (getView() == null) {
                throw new IllegalStateException("view is not attached, "
                        + "no executor available to run ui interactions on");
            } else {
                throw new IllegalStateException("no ui thread executor available");
            }
        }
    }

    /**
     * Executes the {@link ViewAction} when the view is available on the UI thread.
     * Once a view is attached the actions get called in the same order they have been added.
     * When the view is already attached the action will be executed immediately.
     * <p>
     * This method might be very useful for single actions which invoke function like {@link
     * Activity#finish()}, {@link Activity#startActivity(Intent)} or showing a {@link Toast} in the
     * view.
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
    @RestrictTo(SUBCLASSES)
    public void sendToView(final ViewAction<V> action) {
        final V view = getView();
        if (view != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    action.call(view);
                }
            });
        } else {
            mPostponedViewActions.add(action);
        }
    }

    /**
     * sets the Executor used for the {@link #runOnUiThread(Runnable)} method.
     * <p>
     * This Executor is most likely the {@link net.grandcentrix.thirtyinch.internal.UiThreadExecutor}
     * posting the work on the Android Main Thread.
     * When using the {@code TiPresenterInstructor} in your tests an {@link Executor} for the
     * current {@link Thread} is used, therefore all executed actions run synchronous.
     *
     * @param uiThreadExecutor executor for view interactions
     */
    public void setUiThreadExecutor(@Nullable final Executor uiThreadExecutor) {
        mUiThreadExecutor = uiThreadExecutor;
    }

    /**
     * Creates {@link TiTestPresenter} that simplifies testing by calling the presenter lifecycle
     * methods automatically in the correct order. It also sets the ui thread Executors which allows
     * the usage of {@link TiPresenter#sendToView(ViewAction)} in unit test.
     * <code>
     * <pre>
     *    &#64;Test
     *    public void testLoadData() throws Exception {
     *        final LoginPresenter loginPresenter = new LoginPresenter();
     *        final TiTestPresenter<LoginView> testPresenter = loginPresenter.test();
     *        final LoginView view = testPresenter.attachView(mock(LoginView.class));
     *
     *        loginPresenter.onSubmitClicked();
     *        verify(view).showError("No username entered");
     *    }
     *
     *    public class LoginPresenter extends TiPresenter<LoginView> {
     *
     *        public void onSubmitClicked() {
     *            sendToView(new ViewAction<LoginView>() {
     *                &#64;Override
     *                public void call(final LoginView view) {
     *                    view.showError("No username entered");
     *                }
     *            });
     *        }
     *    }
     *
     *    public interface LoginView extends TiView {
     *        void showError(String msg);
     *    }
     * </pre>
     * </code>
     *
     * <p>
     * The problem is that {@link TiPresenter#sendToView(ViewAction)} needs a ui executor thread.
     * Unfortunately a ui executor thread isn't available in unit test. Instead a mock implementation
     * is provided which executes the actions immediately on the testing thread.
     * </p>
     * <p>
     * This {@link TiTestPresenter} holds the {@link TiPresenter} under test.
     * </p>
     *
     * @return instance of {@link TiTestPresenter}
     */
    public TiTestPresenter<V> test() {
        return new TiTestPresenter<>(this);
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

        if (mLifecycleObservers.size() > 0) {
            // make a local copy to call all observers,
            // even observers which will been removed by other observers which received this event
            final List<TiLifecycleObserver> observers = new ArrayList<>(mLifecycleObservers);
            switch (newState) {
                case INITIALIZED:
                case VIEW_ATTACHED:
                    for (int i = 0; i < observers.size(); i++) {
                        observers.get(i).onChange(newState, hasLifecycleMethodBeenCalled);
                    }
                    break;

                case VIEW_DETACHED:
                case DESTROYED:
                    // reverse observer order for teardown events; first in, last out
                    for (int i = observers.size() - 1; i >= 0; i--) {
                        observers.get(i).onChange(newState, hasLifecycleMethodBeenCalled);
                    }
            }
        }
    }

    /**
     * Executes all postponed view actions
     *
     * @param view where the actions will be sent to
     */
    private void sendPostponedActionsToView(@NonNull final V view) {
        while (!mPostponedViewActions.isEmpty()) {
            mPostponedViewActions.poll().call(view);
        }
    }
}
