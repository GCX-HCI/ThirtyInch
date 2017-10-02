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

package net.grandcentrix.thirtyinch.internal;

import java.util.concurrent.Executor;
import net.grandcentrix.thirtyinch.TiLifecycleObserver;
import net.grandcentrix.thirtyinch.TiPresenter;

/**
 * binds a ui thread executor to the presenter when this view is attached
 */
public class UiThreadExecutorAutoBinder implements TiLifecycleObserver {

    private final TiPresenter mPresenter;

    private final Executor mUiThreadExecutor;

    public UiThreadExecutorAutoBinder(final TiPresenter presenter,
            final Executor uiThreadExecutor) {
        mPresenter = presenter;
        mUiThreadExecutor = uiThreadExecutor;
    }

    @Override
    public void onChange(final TiPresenter.State state,
            final boolean hasLifecycleMethodBeenCalled) {

        if (state == TiPresenter.State.VIEW_ATTACHED && !hasLifecycleMethodBeenCalled) {
            // before super.onAttachView(view)
            mPresenter.setUiThreadExecutor(mUiThreadExecutor);
        }
        if (state == TiPresenter.State.VIEW_DETACHED && hasLifecycleMethodBeenCalled) {
            // after super.onDetachView()
            mPresenter.setUiThreadExecutor(null);
        }
    }
}
