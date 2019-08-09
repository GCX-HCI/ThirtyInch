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

package net.grandcentrix.thirtyinch.rx;

import androidx.annotation.NonNull;
import net.grandcentrix.thirtyinch.TiPresenter;
import net.grandcentrix.thirtyinch.TiView;

/**
 * @author jannisveerkamp
 * @since 11.07.16.
 */
class TiMockPresenter extends TiPresenter<TiView> {

    protected int onAttachCalled = 0;

    protected int onCreateCalled = 0;

    protected int onDestroyCalled = 0;

    protected int onDetachCalled = 0;

    @Override
    protected void onCreate() {
        super.onCreate();
        onCreateCalled++;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        onDestroyCalled++;
    }

    @Override
    protected void onAttachView(@NonNull final TiView view) {
        super.onAttachView(view);
        onAttachCalled++;
    }

    @Override
    protected void onDetachView() {
        super.onDetachView();
        onDetachCalled++;
    }
}
