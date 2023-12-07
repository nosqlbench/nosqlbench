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

import io.nosqlbench.engine.cmdstream.Cmd;
import io.nosqlbench.engine.cmdstream.CmdArg;
import io.nosqlbench.engine.cmdstream.CmdParam;
import io.nosqlbench.engine.core.lifecycle.session.NBCommandInvoker;
import io.nosqlbench.nb.api.components.NBComponent;
import io.nosqlbench.engine.core.lifecycle.scenario.container.NBBufferedContainer;
import io.nosqlbench.engine.core.lifecycle.scenario.container.NBContainer;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBCommandResult;
import io.nosqlbench.engine.core.lifecycle.scenario.script.NBScriptedCommand;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class NBBaseCommandTest {
    private final Logger logger = LogManager.getLogger(NBBaseCommandTest.class);

    @Test
    public void shouldLoadScriptText() {
        NBBufferedContainer ctx = NBContainer.builder().name("testing").build(NBComponent.EMPTY_COMPONENT);
        NBScriptedCommand cmd = NBScriptedCommand.ofScripted("testing", Map.of(),ctx, NBScriptedCommand.Invocation.EXECUTE_SCRIPT);
        cmd.add(new Cmd("fragment",Map.of(
            "fragment",new CmdArg(new CmdParam("fragment",s->s,false),"=","print('loaded script environment...');")
        )));

        try {
            NBCommandResult result = NBCommandInvoker.invoke(ctx,cmd);
            assertThat(result.getIOLog()).contains("loaded script environment...");
        } catch (Exception e) {
            logger.debug(() -> "Scenario run encountered an exception: " + e.getMessage());
        }
    }

}
