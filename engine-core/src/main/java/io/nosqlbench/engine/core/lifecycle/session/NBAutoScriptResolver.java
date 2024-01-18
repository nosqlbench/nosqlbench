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
import io.nosqlbench.engine.cmdstream.CmdArg;
import io.nosqlbench.engine.cmdstream.CmdParam;
import io.nosqlbench.engine.core.lifecycle.scenario.container.NBBufferedContainer;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBInvokableCommand;
import io.nosqlbench.engine.core.lifecycle.scenario.script.NBScriptedCommand;
import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.nb.api.nbio.Content;
import io.nosqlbench.nb.api.nbio.NBIO;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Service(value = NBInvokableResolver.class, selector = "autojs")
public class NBAutoScriptResolver implements NBInvokableResolver {
    @Override
    public NBInvokableCommand resolve(Cmd cmd, NBBufferedContainer parent, String phaseName) {

        Optional<Content<?>> scriptfile = NBIO.local()
            .searchPrefixes("scripts/auto")
            .pathname(cmd.getArgValue("_impl"))
            .extensionSet("js")
            .first();

        if (scriptfile.isPresent()) {
            Path pathOf = scriptfile.get().asPath();
            Map<String, CmdArg> newArgs = new LinkedHashMap<>(cmd.getArgs());
            newArgs.put("path",new CmdArg(new CmdParam("name",s->s,false),"=",pathOf.toString()));
            Cmd reformattedCmd = new Cmd("script", newArgs);
            return new NBScriptedCommand(parent, phaseName, cmd.getTargetContext()).add(reformattedCmd);
        } else {
            return null;
        }
    }

    @Override
    public boolean verify(Cmd cmd) {
        return NBIO.local()
                .searchPrefixes("scripts/auto")
                .pathname(cmd.getArgValue("_impl"))
                .extensionSet("js")
                .first()
            .isPresent();
    }


}
