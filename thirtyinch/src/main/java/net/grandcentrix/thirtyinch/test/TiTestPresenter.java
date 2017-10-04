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

package net.grandcentrix.thirtyinch.test;

import android.support.annotation.NonNull;
import java.util.concurrent.Executor;
import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.TiView;
import net.grandcentrix.thirtyinch.ViewAction;

/**
 * Simplifies testing by calling the presenter lifecycle methods automatically in the correct
 * order. It also sets the ui thread Executors which allows the usage of
 * {@link TiPresenter#sendToView(ViewAction)} in unit test.
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
 */
public class TiTestPresenter<V extends TiView> {

    private TiPresenter<V> mPresenter;

    public TiTestPresenter(final TiPresenter<V> presenter) {
        mPresenter = presenter;
    }

    /**
     * attaches the new view and takes care for removing the old view when one is attached
     *
     * @see TiPresenter#onAttachView(TiView)
     */
    public V attachView(final V view) {
        detachView();

        // execute actions immediately on the same thread
        mPresenter.setUiThreadExecutor(new Executor() {
            @Override
            public void execute(@NonNull final Runnable action) {
                action.run();
            }
        });
        mPresenter.attachView(view);
        return view;
    }

    /**
     * initialize the presenter
     *
     * @see TiPresenter#onCreate()
     */
    public void create() {
        mPresenter.create();
    }

    /**
     * destroys the presenter, last lifecycle method
     *
     * @see TiPresenter#onDestroy()
     */
    public void destroy() {
        detachView();
        mPresenter.destroy();
    }

    /**
     * moves the presenter into state {@link TiPresenter.State#VIEW_DETACHED}
     * from every state
     *
     * @see TiPresenter#onDetachView()
     */
    public void detachView() {
        final TiPresenter.State state = mPresenter.getState();
        switch (state) {
            case INITIALIZED:
                mPresenter.create();
                break;
            case VIEW_DETACHED:
                // already there
                break;
            case VIEW_ATTACHED:
                mPresenter.detachView();
                mPresenter.setUiThreadExecutor(null);
                break;
            case DESTROYED:
                throw new IllegalStateException(
                        "Presenter is already destroyed, further lifecycle changes aren't allowed");
        }
    }
}