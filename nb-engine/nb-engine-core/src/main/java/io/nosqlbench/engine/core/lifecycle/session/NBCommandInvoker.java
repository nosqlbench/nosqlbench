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

package io.nosqlbench.engine.core.lifecycle.session;

import io.nosqlbench.engine.core.lifecycle.scenario.container.NBBufferedContainer;
import io.nosqlbench.engine.core.lifecycle.scenario.container.NBContainer;
import io.nosqlbench.engine.core.lifecycle.scenario.container.NBCommandParams;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBCommandResult;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBInvokableCommand;
import io.nosqlbench.nb.api.config.standard.TestComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class NBCommandInvoker {
    private final static Logger logger = LogManager.getLogger(NBCommandInvoker.class);

    public static NBCommandResult invoke(NBInvokableCommand command, NBCommandParams params) {
        return invoke(createContext(),command,params);
    }

    public static NBCommandResult invoke(NBBufferedContainer container, NBInvokableCommand command) {
        return invoke(container, command, NBCommandParams.of(Map.of()));
    }

    private static NBBufferedContainer createContext() {
        return NBContainer.builder().name("testing").build(TestComponent.EMPTY_COMPONENT);
    }

    public static NBCommandResult invoke(NBBufferedContainer container, NBInvokableCommand command, NBCommandParams params) {
        return command.invokeSafe(container,params);
    }
}
