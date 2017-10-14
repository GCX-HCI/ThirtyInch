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

import java.util.concurrent.Executor;
import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.TiView;

/**
 * @deprecated Use {@link TiTestPresenter} instead
 */
@Deprecated
public class TiPresenterInstructor<V extends TiView> {

    private TiPresenter<V> mPresenter;

    public TiPresenterInstructor(final TiPresenter<V> presenter) {
        mPresenter = presenter;
    }

    /**
     * attaches the new view and takes care for removing the old view when one is attached
     */
    public void attachView(final V view) {
        detachView();

        mPresenter.setUiThreadExecutor(new Executor() {
            @Override
            public void execute(final Runnable action) {
                action.run();
            }
        });
        mPresenter.attachView(view);
    }

    public void create() {
        mPresenter.create();
    }

    public void destroy() {
        detachView();
        mPresenter.destroy();
    }

    /**
     * moves the presenter into state {@link net.grandcentrix.thirtyinch.TiPresenter.State#VIEW_DETACHED}
     * from every state
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
