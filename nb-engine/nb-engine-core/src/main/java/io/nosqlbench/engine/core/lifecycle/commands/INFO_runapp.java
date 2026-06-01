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

@Service(value = NBCommandInfo.class, selector = "runapp")
public class INFO_runapp extends NBCommandInfo {
    @Override
    public Class<? extends NBInvokableCommand> getType() {
        return CMD_runapp.class;
    }

    @Override
    public String getHelp() {
        return """
            run a bundled app as a step in the command stream (also usable in named scenarios)

            Select the app with the reserved parameter:
            * appname      the bundled app selector (see --list-apps)

            Remaining name=value parameters are translated into the app's command line. For an app
            with a picocli command model, each name=value is mapped to its --name option and
            validated against the app's real options; a command=<name> parameter selects a
            subcommand. For example:

                runapp appname=stress-report sqlite=logs/metrics.db
                  -> stress-report --sqlite=logs/metrics.db

            A non-zero exit code from the app stops the command stream.

            Note: apps without a picocli command model (e.g. those delegating to an external CLI)
            cannot express subcommands or positional arguments through runapp; invoke those at the
            top level as 'nb5 <app> ...' with their native arguments instead.
            """;
    }
}
