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

package io.nosqlbench.engine.cli;

import io.nosqlbench.engine.cmdstream.Cmd;
import io.nosqlbench.engine.cmdstream.CmdType;
import io.nosqlbench.engine.cmdstream.NBJavaCommandLoader;
import io.nosqlbench.engine.core.lifecycle.scenario.container.NBBufferedContainer;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBInvokableCommand;
import io.nosqlbench.engine.core.lifecycle.session.NBInvokableResolver;
import io.nosqlbench.nb.annotations.Service;

@Service(value= NBInvokableResolver.class,selector = "java")
public class NBJavaNativeResolver implements NBInvokableResolver {
    @Override
    public NBInvokableCommand resolve(Cmd cmd, NBBufferedContainer parent, String phaseName) {
        return switch (cmd.getCmdType()) {
            case CmdType.indirect -> {
                String implName = cmd.takeArgValue("_impl");
                yield NBJavaCommandLoader.init(implName, parent, phaseName, cmd.getTargetContext());
            }
            case CmdType.java -> NBJavaCommandLoader.init(cmd.getArgValue("class"), parent, phaseName, cmd.getTargetContext());
            default -> null;
        };
    }
}
