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

package io.nosqlbench.nbr;

import io.nosqlbench.api.config.standard.TestComponent;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.ScenariosExecutor;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.ScenariosResults;
import io.nosqlbench.nbr.examples.SCDryRunScenarioTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class DirectRuntimeScenarioTests {

    @Test
    public void testDirect() {
        TestComponent testC = new TestComponent("testroot", "testroot");
        SCDryRunScenarioTest sc1 = new SCDryRunScenarioTest(TestComponent.EMPTY_COMPONENT, "test", Map.of(), "console:1s");
        ScenariosExecutor executor = new ScenariosExecutor(TestComponent.EMPTY_COMPONENT, "test", 1);
        executor.execute(sc1);
        ScenariosResults results = executor.awaitAllResults();
        System.out.println(results);
    }
}
