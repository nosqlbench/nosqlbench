/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.engine.core;

import io.nosqlbench.api.config.standard.TestComponent;
import io.nosqlbench.engine.api.scripting.ScriptEnvBuffer;
import io.nosqlbench.engine.core.lifecycle.scenario.NBScenario;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NBScenarioTest {
    private final Logger logger = LogManager.getLogger(NBScenarioTest.class);

    @Test
    public void shouldLoadScriptText() {
        ScriptEnvBuffer buffer = new ScriptEnvBuffer();
        NBScenario scenario = NBScenario.forTesting("testing", "stdout:300", new TestComponent());
        scenario.addScriptText("print('loaded script environment...');\n");
        try {
            var result=scenario.call();
        } catch (Exception e) {
            logger.debug(() -> "Scenario run encountered an exception: " + e.getMessage());

        }
        assertThat(scenario.getIOLog().get().get(0)).contains("loaded script environment...");
    }

}
