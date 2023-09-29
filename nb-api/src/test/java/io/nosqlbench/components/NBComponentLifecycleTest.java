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

class NBComponentLifecycleTest {

    @Test
    public void testBasicLifecycleHooks() {
        TestComponent root = new TestComponent("role", "root");
        TestComponent node1 = new TestComponent(root, "node1", "node1");
        TestComponent node2 = new TestComponent(root, "node2", "node2");
        TestComponent node3 = new TestComponent(root, "node3", "node3");

        try (NBComponentSubScope scope = new NBComponentSubScope(node1)) {
            System.out.println("node1 active");
        }

        System.out.print("node1 inactive");

    }

}
