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

import net.grandcentrix.thirtyinch.Removable;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Removable which allows removing only once
 */
public abstract class OneTimeRemovable implements Removable {

    private final AtomicBoolean removed = new AtomicBoolean(false);

    @Override
    public boolean isRemoved() {
        return removed.get();
    }

    /**
     * Called when the added Object should be removed. Only called once
     */
    public abstract void onRemove();

    @Override
    public void remove() {
        // allow calling remove only once
        if (removed.compareAndSet(false, true)) {
            onRemove();
        }
    }
}
