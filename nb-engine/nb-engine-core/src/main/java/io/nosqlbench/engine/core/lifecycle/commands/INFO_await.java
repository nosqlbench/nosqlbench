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

package io.nosqlbench.engine.core.lifecycle.commands;

import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBCommandInfo;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBInvokableCommand;
import io.nosqlbench.nb.annotations.Service;

@Service(value = NBCommandInfo.class,selector = "await")
public class INFO_await extends NBCommandInfo {
    @Override
    public Class<? extends NBInvokableCommand> getType() {
        return CMD_await.class;
    }

    @Override
    public String getHelp() {
        return """
            block the session thread until the named activity is no longer running

            This means that the named activity is one of:
            1. Not started yet (or ever)
            2. Completed (having run all of its cycles)
            3. Errored
            4. Stopped by some other command

            EXAMPLE:
            await alias=myactivity

            """;
    }

}
