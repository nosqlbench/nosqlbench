/*
 * Copyright (c) 2023 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.components;

import io.nosqlbench.api.config.standard.TestComponent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NBComponentViewsTest {
    @Test
    public void testBasicTreeView() {
        var root = new TestComponent("a", "b")
            .attach(new TestComponent("c", "d")
                .attach(new TestComponent("U", "V"))
                .attach(new TestComponent("Y","Z")))
            .attach(new TestComponent("e", "f"));
        System.out.println(NBComponentViews.treeView(root));

        System.out.println(NBComponentViews.treeView(root, c -> String.valueOf(c.hashCode())));
    }

}
