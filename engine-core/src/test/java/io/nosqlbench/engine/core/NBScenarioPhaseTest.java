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
import io.nosqlbench.api.labels.NBLabels;
import io.nosqlbench.components.NBComponent;
import io.nosqlbench.engine.cli.Cmd;
import io.nosqlbench.engine.core.lifecycle.scenario.context.NBBufferedScenarioContext;
import io.nosqlbench.engine.core.lifecycle.scenario.context.NBScenarioContext;
import io.nosqlbench.engine.core.lifecycle.scenario.context.ScenarioPhaseParams;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.ScenarioPhaseResult;
import io.nosqlbench.engine.core.lifecycle.scenario.script.NBScriptedScenarioPhase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class NBScenarioPhaseTest {
    private final Logger logger = LogManager.getLogger(NBScenarioPhaseTest.class);

    @Test
    public void shouldLoadScriptText() {
        NBScriptedScenarioPhase phase = NBScriptedScenarioPhase.ofScripted("testing", Map.of(),new TestComponent(), NBScriptedScenarioPhase.Invocation.EXECUTE_SCRIPT);
        phase.addScriptText("print('loaded script environment...');\n");
        NBBufferedScenarioContext ctx = NBScenarioContext.builder().name("testing").build(NBComponent.EMPTY_COMPONENT);

        try {
            ScenarioPhaseResult result = phase.apply(ctx, ScenarioPhaseParams.of(Map.of()));
            assertThat(result.getIOLog()).contains("loaded script environment...");
        } catch (Exception e) {
            logger.debug(() -> "Scenario run encountered an exception: " + e.getMessage());
        }
    }

}
