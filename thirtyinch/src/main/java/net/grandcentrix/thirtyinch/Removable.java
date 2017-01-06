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
 * A Removable returns from a method which adds an object to something. It allows removing the
 * added object without keeping track of the added or the object which got something added. Only
 * holding a reference to this Removable is required.
 * <p>
 * This interface is the equivalent of RxJava {@code Subscription} or {@code IDisposable} in
 * Microsoft's Rx implementation.
 */
public interface Removable {

    /**
     * Indicates whether the added Object is still added
     *
     * @return {@code true} if the added Object is currently added, {@code false} otherwise
     */
    boolean isRemoved();

    /**
     * Removes the Object which got previously added
     */
    void remove();
}
