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
import io.nosqlbench.engine.core.lifecycle.scenario.execution.ScenarioResult;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.ScenariosExecutor;
import io.nosqlbench.engine.core.lifecycle.scenario.script.NBScriptedScenario;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class NBScenarioTest {
    private final Logger logger = LogManager.getLogger(NBScenarioTest.class);

    @Test
    public void shouldLoadScriptText() {
        ScriptEnvBuffer buffer = new ScriptEnvBuffer();
        NBScriptedScenario scenario = NBScriptedScenario.ofScripted("testing", Map.of(),new TestComponent(), NBScriptedScenario.Invocation.EXECUTE_SCRIPT);
        scenario.addScriptText("print('loaded script environment...');\n");
        try {
            ScenariosExecutor executor = new ScenariosExecutor(TestComponent.INSTANCE, "test", 1);
            executor.execute(scenario);
            ScenarioResult result = executor.awaitAllResults().getOne();
            assertThat(result.getIOLog()).contains("loaded script environment...");
        } catch (Exception e) {
            logger.debug(() -> "Scenario run encountered an exception: " + e.getMessage());
        }
    }

}
