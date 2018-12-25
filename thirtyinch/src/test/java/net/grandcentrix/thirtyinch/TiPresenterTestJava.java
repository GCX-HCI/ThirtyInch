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

import static org.assertj.core.api.Assertions.*;

import org.junit.*;

/**
 * This class is necessary as there is one method which can be only tested from Java.
 *
 * @author jannisveerkamp
 * @since 11.07.16.
 */
public class TiPresenterTestJava {

    private TiMockPresenter mPresenter;

    @Before
    public void setUp() {
        mPresenter = new TiMockPresenter();
    }

    @After
    public void tearDown() {
        mPresenter = null;
    }

    @Test
    public void attachNullView() {

        mPresenter.create();
        try {
            // calling this from Kotlin simply doesn't compile; in Java it's only a warning
            mPresenter.attachView(null);
            fail("no exception thrown");
        } catch (IllegalStateException e) {
            assertThat(e).hasMessageContaining("detachView()");
        }
    }
}