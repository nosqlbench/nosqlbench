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

package io.nosqlbench.engine.core.lifecycle.commands;

import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBCommandInfo;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBInvokableCommand;
import io.nosqlbench.nb.annotations.Service;

@Service(value = NBCommandInfo.class,selector = "getenv")
public class INFO_getenv extends NBCommandInfo {
    @Override
    public Class<? extends NBInvokableCommand> getType() {
        return CMD_getenv.class;
    }

    @Override
    public String getHelp() {
        return """
            hoist environment variables into the container state under the given names

            EXAMPLE:
            getenv authfile=AUTHFILE usermode=FOOSELECTED

            This imports the AUTHFILE and FOOSELECTED environment variables into the container state
            under the names authfile and usermode. The variables are then available to
            other commands using the container state variable syntax, like ${stepname.authfile}
            or ${stepname.usermode}

            """;
    }

}
