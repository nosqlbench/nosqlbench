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

class NBComponentViewsTest {
    @Test
    public void testBasicTreeView() {
        var root1 = new TestComponent("a", "b");
        var cd = new TestComponent(root1, "c", "d");
        var UV = new TestComponent(cd, "U", "V");
        var YZ = new TestComponent(cd, "Y", "Z");
        var ef = new TestComponent(root1, "e", "f");

        var root2 = new TestComponent("a", "b");

        root2.attach(new TestComponent(root2, "c", "d")
                .attach(new TestComponent("U", "V"))
                .attach(new TestComponent("Y", "Z")))
            .attach(new TestComponent("e", "f"));

        System.out.println("root1:\n" + NBComponentViews.treeView(root1));
        System.out.println("root1:\n" + NBComponentViews.treeView(root1, c -> String.valueOf(c.hashCode())));

        System.out.println("root2:\n" + NBComponentViews.treeView(root2));
        System.out.println("root2:\n" + NBComponentViews.treeView(root2, c -> String.valueOf(c.hashCode())));
    }

}
