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

package net.grandcentrix.thirtyinch

/**
 * @author jannisveerkamp
 * @since 11.07.16.
 */
internal class TiMockPresenter : TiPresenter<TiView> {

    var onCreateCalled = 0
        private set

    var onDestroyCalled = 0
        private set

    var onSleepCalled = 0
        private set

    var onWakeUpCalled = 0
        private set

    constructor()

    constructor(config: TiConfiguration) : super(config)

    public override fun onCreate() {
        super.onCreate()
        onCreateCalled++
    }

    public override fun onDestroy() {
        super.onDestroy()
        onDestroyCalled++
    }

    public override fun onSleep() {
        super.onSleep()
        onSleepCalled++
    }

    public override fun onWakeUp() {
        super.onWakeUp()
        onWakeUpCalled++
    }
}
