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
import io.nosqlbench.nb.annotations.Service;

import java.util.*;

/**
 * This is the core wrapper around any resolvers for NB commands.
 * Call it directly and it will invoke all others in the runtime as long as they
 * are registered as a service for SPI.
 */
public class NBCoreInvokableResolver implements NBInvokableResolver {
    private final static String[] precedence = new String[]{"js", "java", "autojs"};
    private SequencedMap<String,NBInvokableResolver> resolvers = new LinkedHashMap<>();

    @Override
    public NBInvokableCommand resolve(Cmd cmd, NBBufferedContainer parent, String phaseName) {
        for (NBInvokableResolver resolver : getResolvers().values()) {
            NBInvokableCommand loadedCommand = resolver.resolve(cmd, parent, phaseName);
            if (loadedCommand!=null) {
                return loadedCommand;
            }
        }
//        if (cmd.getCmdType() == CmdType.indirect) {
//        }
        return null;
    }

    private SequencedMap<String, NBInvokableResolver> getResolvers() {
        if (this.resolvers == null || this.resolvers.isEmpty()) {
            SequencedMap<String,NBInvokableResolver> resolverMap = new LinkedHashMap<>();
            ServiceLoader<NBInvokableResolver> resolvers = ServiceLoader.load(NBInvokableResolver.class);
            for (NBInvokableResolver resolver : resolvers) {
                String selector = resolver.getClass().getAnnotation(Service.class).selector();
                resolverMap.put(selector,resolver);
            }
            for (int i = precedence.length-1; i >= 0; i--) {
                if (resolverMap.containsKey(precedence[i])) {
                    NBInvokableResolver resolver = resolverMap.remove(precedence[i]);
                    resolverMap.putFirst(precedence[i],resolver);
                }
            }
            this.resolvers = resolverMap;
        }
        return this.resolvers;
    }

}
