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

package io.nosqlbench.engine.cmdstream;

import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBInvokableCommand;
import io.nosqlbench.nb.api.components.NBComponent;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBCommandInfo;
import io.nosqlbench.nb.annotations.ServiceSelector;

import java.util.List;
import java.util.ServiceLoader;

public class NBJavaCommandLoader {
    public static Class<? extends NBInvokableCommand> oneExists(String cmdName) {
        ServiceLoader<NBCommandInfo> loader = ServiceLoader.load(NBCommandInfo.class);
        ServiceSelector<NBCommandInfo> selector = ServiceSelector.of(cmdName, loader);
        List<? extends ServiceLoader.Provider<? extends NBCommandInfo>> providers = selector.getAllProviders();
        if (providers.size()>1) {
            throw new RuntimeException("looking for an optional command for cmdName '" + cmdName +"' but found " + providers.size());
        }
        if (!providers.isEmpty()) {
            return providers.get(0).get().getType();
        } else {
            return null;
        }
    }

    public static NBInvokableCommand init(String cmdSelector, NBComponent parent, String cmdAlias, String ctxName) {
        NBCommandInfo cmdInfo = getSelector(cmdSelector).getOne();
        NBInvokableCommand command = cmdInfo.create(parent, cmdAlias, ctxName);
        return command;
    }

    private static ServiceSelector<NBCommandInfo> getSelector(String cmdName) {
        ServiceLoader<NBCommandInfo> loader = ServiceLoader.load(NBCommandInfo.class);
        ServiceSelector<NBCommandInfo> selector = ServiceSelector.of(cmdName, loader);
        return selector;
    }

}
