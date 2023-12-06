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

package io.nosqlbench.engine.core.lifecycle.scenario.execution;

import io.nosqlbench.engine.core.lifecycle.scenario.context.NBBufferedCommandContext;
import io.nosqlbench.nb.api.components.NBComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Implement this stub service to identify {@link NBInvokableCommand}s which can be loaded in the runtime.
 * Each concrete implementation of NBCommandInfo needs to be annotated with
 * <pre>{@code
 * @Service(value = NBCommandInfo.class, selector = "<cmdname>")
 * }</pre>
 */
public abstract class NBCommandInfo {
    private final static Logger logger = LogManager.getLogger(NBCommandInfo.class);
    public abstract Class<? extends NBInvokableCommand> getType();
    public NBInvokableCommand create(NBComponent parent, String cmdName, String ctxName) {
        Constructor<? extends NBInvokableCommand> cmdCtor;
        try {
            cmdCtor = getType().getConstructor(NBBufferedCommandContext.class, String.class, String.class);
            return cmdCtor.newInstance(parent, cmdName, ctxName);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Unable to instantiate command via ctor(parent,name,ctx): " + e,e);
        }
    }
}
