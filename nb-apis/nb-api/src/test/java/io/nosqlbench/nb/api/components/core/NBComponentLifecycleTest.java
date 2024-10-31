/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.nb.api.components.core;

import io.nosqlbench.nb.api.config.standard.TestComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

class NBComponentLifecycleTest {
    private final static Logger logger = LogManager.getLogger(NBComponentLifecycleTest.class);

    @Test
    public void testBasicLifecycleHooks() {
        TestComponent root = new TestComponent("role", "root");
        TestComponent node1 = new TestComponent(root, "node1", "node1");
        TestComponent node2 = new TestComponent(root, "node2", "node2");

        try (NBComponentExecutionScope scope = new NBComponentExecutionScope(node1)) {
            logger.info(node1.description() + " active");
       }
        try (NBComponentExecutionScope scope = new NBComponentExecutionScope(node2)) {
            logger.info(node2.description() + " active");
        }

        logger.info("all inactive");

    }

}
