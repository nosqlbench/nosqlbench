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

@Service(value = NBCommandInfo.class,selector = "start")
public class INFO_start extends NBCommandInfo {
    @Override
    public Class<? extends NBInvokableCommand> getType() {
        return CMD_start.class;
    }

    @Override
    public String getHelp() {
        return """
            start an activity without blocking the main control thread

            This is the asynchronous (with respect to the main control thread) version
            of the run command. A NoSQLBench container can run arbitrary activities
            concurrently, each within their own thread pool. By using the start
            command, you can start them and then use other commands to observer,
            modify, or block on state for additional testing stages.

            By default, a NoSQLBench session (and all owned containers) will exit when
            any of the following is true:
            1. all session commands have completed
            2. OR all activities have completed or failed
            3. OR a command explicitly requests shutdown

            EXAMPLE:
            start alias=activity1 driver=stdout op="activity1: {{Identity()}}\n" cyclerate=10 cycles=600
            start alias=activity2 driver=stdout op="activity2: {{Identity()}}\n" cyclerate=20 cycles=600
            """;
    }
}
