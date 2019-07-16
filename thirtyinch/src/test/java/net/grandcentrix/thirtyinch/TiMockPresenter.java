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

/**
 * @author jannisveerkamp
 * @since 11.07.16.
 */
class TiMockPresenter extends TiPresenter<TiView> {

    protected int onCreateCalled = 0;

    protected int onDestroyCalled = 0;

    public TiMockPresenter() {
    }

    public TiMockPresenter(final TiConfiguration config) {
        super(config);
    }

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

}
