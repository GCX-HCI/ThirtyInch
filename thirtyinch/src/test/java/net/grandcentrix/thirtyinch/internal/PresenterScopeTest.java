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

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import net.grandcentrix.thirtyinch.TiPresenter;
import org.junit.*;

public class PresenterScopeTest {

    @Test
    public void addOneMapping() throws Exception {

        final PresenterScope scope = new PresenterScope();
        final TiPresenter presenter = new TiPresenter() {
        };
        scope.save("a", presenter);

        assertThat(scope.getAll())
                .hasSize(1)
                .contains(presenter);

        assertThat(scope.size()).isEqualTo(1);
        assertThat(scope.getAllMappings()).hasSize(1);
        assertThat(scope.getAllMappings().get(0).getKey()).isEqualTo("a");
        assertThat(scope.getAllMappings().get(0).getValue()).isEqualTo(presenter);
    }

    @Test
    public void addTwoMappings() throws Exception {

        final PresenterScope scope = new PresenterScope();
        final TiPresenter presenter1 = new TiPresenter() {
        };
        scope.save("a", presenter1);

        // add second presenter with different id
        final TiPresenter presenter2 = new TiPresenter() {
        };
        scope.save("b", presenter2);

        assertThat(scope.size()).isEqualTo(2);

        assertThat(scope.getAll())
                .hasSize(2)
                .contains(presenter1, presenter2);
    }

    @Test
    public void getAllIsEmptyNotNullForEmptyScope() throws Exception {
        final PresenterScope scope = new PresenterScope();
        assertThat(scope.size()).isEqualTo(0);
        assertThat(scope.getAll()).isEqualTo(new ArrayList<>());
    }

    @Test
    public void getAllMappingsIsEmptyNotNullForEmptyScope() throws Exception {
        final PresenterScope scope = new PresenterScope();
        assertThat(scope.size()).isEqualTo(0);
        assertThat(scope.getAllMappings()).isEqualTo(new ArrayList<>());
    }

    @Test
    public void overrideMappingThrows() throws Exception {

        final PresenterScope scope = new PresenterScope();
        final TiPresenter presenter1 = new TiPresenter() {
        };
        scope.save("myId", presenter1);

        // override with same id throws
        final TiPresenter presenter2 = new TiPresenter() {
        };
        try {
            scope.save("myId", presenter2);
            fail("did not throw");
        } catch (IllegalStateException e) {
            assertThat(e).hasMessageContaining("myId");
        }
    }

    @Test
    public void removeOneMapping() throws Exception {

        final PresenterScope scope = new PresenterScope();
        final TiPresenter presenter = new TiPresenter() {
        };
        scope.save("a", presenter);

        assertThat(scope.getAll())
                .hasSize(1)
                .contains(presenter);

        final TiPresenter removedPresenter = scope.remove("a");

        assertThat(scope.getAll()).isEmpty();
        assertThat(removedPresenter).isEqualTo(presenter);
    }

    @Test
    public void removePresenterNotInScope() throws Exception {
        final PresenterScope scope = new PresenterScope();
        final TiPresenter removedPresenter = scope.remove("a");
        assertThat(removedPresenter).isNull();
    }

    @Test
    public void saveNullPresenterThrows() throws Exception {
        final PresenterScope scope = new PresenterScope();
        try {
            scope.save("a", null);
            fail("did not throw");
        } catch (IllegalStateException e) {
            assertThat(e).hasMessageContaining("null").hasMessageContaining("presenter");
        }
    }

    @Test
    public void savePresenterWithNullIdThrows() throws Exception {
        final PresenterScope scope = new PresenterScope();
        try {
            scope.save(null, new TiPresenter() {
            });
            fail("did not throw");
        } catch (IllegalStateException e) {
            assertThat(e).hasMessageContaining("null").hasMessageContaining("id");
        }
    }

    @Test
    public void saveSamePresenterTwiceThrows() throws Exception {

        final PresenterScope scope = new PresenterScope();
        final TiPresenter presenter = new TiPresenter() {
        };
        scope.save("myId", presenter);

        // try to save the same presenter with a different id
        try {
            scope.save("b", presenter);
            fail("did not throw");
        } catch (IllegalStateException e) {
            assertThat(e).hasMessageContaining("myId");
        }
    }
}