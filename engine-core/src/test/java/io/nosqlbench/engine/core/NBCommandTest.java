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
import io.nosqlbench.components.NBComponent;
import io.nosqlbench.engine.core.lifecycle.scenario.context.NBBufferedCommandContext;
import io.nosqlbench.engine.core.lifecycle.scenario.context.NBCommandContext;
import io.nosqlbench.engine.core.lifecycle.scenario.context.NBCommandParams;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBCommandResult;
import io.nosqlbench.engine.core.lifecycle.scenario.script.NBScriptedCommand;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class NBCommandTest {
    private final Logger logger = LogManager.getLogger(NBCommandTest.class);

    @Test
    public void shouldLoadScriptText() {
        NBScriptedCommand phase = NBScriptedCommand.ofScripted("testing", Map.of(),new TestComponent(), NBScriptedCommand.Invocation.EXECUTE_SCRIPT);
        phase.addScriptText("print('loaded script environment...');\n");
        NBBufferedCommandContext ctx = NBCommandContext.builder().name("testing").build(NBComponent.EMPTY_COMPONENT);

        try {
            NBCommandResult result = phase.apply(ctx, NBCommandParams.of(Map.of()));
            assertThat(result.getIOLog()).contains("loaded script environment...");
        } catch (Exception e) {
            logger.debug(() -> "Scenario run encountered an exception: " + e.getMessage());
        }
    }

}
