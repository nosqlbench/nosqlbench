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

import io.nosqlbench.engine.cmdstream.Cmd;
import io.nosqlbench.engine.core.lifecycle.scenario.container.NBBufferedContainer;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBInvokableCommand;
import io.nosqlbench.engine.core.lifecycle.scenario.script.NBScriptedCommand;
import io.nosqlbench.nb.annotations.Service;

@Service(value = NBInvokableResolver.class, selector = "js")
public class NBScriptCommandResolver implements NBInvokableResolver {
    @Override
    public NBInvokableCommand resolve(Cmd cmd, NBBufferedContainer parent, String stepname) {
        return switch (cmd.getCmdType()) {
            case fragment, script->
            new NBScriptedCommand(parent, stepname, cmd.getTargetContext()).add(cmd);
//            case fragment ->
//                new NBScriptedCommand(parent, phaseName, cmd.getTargetContext()).addScriptText(cmd.getArgValue("fragment"));
//            case script ->
//                new NBScriptedCommand(parent, phaseName, cmd.getTargetContext()).addScriptFiles(cmd.getArgValue("path"));
            default -> null;
        };
    }

    @Override
    public boolean verify(Cmd cmd) {
        return true;
    }
}
