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

package io.nosqlbench.engine.core.lifecycle.session;

import io.nosqlbench.engine.core.lifecycle.scenario.context.NBBufferedCommandContext;
import io.nosqlbench.engine.core.lifecycle.scenario.context.NBCommandContext;
import io.nosqlbench.engine.core.lifecycle.scenario.context.NBCommandParams;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBCommandResult;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBInvokableCommand;
import io.nosqlbench.nb.api.config.standard.TestComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Objects;

public class NBCommandInvoker {
    private final static Logger logger = LogManager.getLogger(NBCommandInvoker.class);

    public static NBCommandResult invoke(NBInvokableCommand command, NBCommandParams params) {
        return invoke(createContext(),command,params);
    }

    public static NBCommandResult invoke(NBBufferedCommandContext context, NBInvokableCommand command) {
        return invoke(context, command, NBCommandParams.of(Map.of()));
    }

    private static NBBufferedCommandContext createContext() {
        return NBCommandContext.builder().name("testing").build(TestComponent.EMPTY_COMPONENT);
    }

    public static NBCommandResult invoke(NBBufferedCommandContext context, NBInvokableCommand command, NBCommandParams params) {
        return command.invokeSafe(context,params);
    }
}
