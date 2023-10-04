/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.engine.shutdown;

import io.nosqlbench.components.NBComponent;
import org.apache.logging.log4j.Logger;

import javax.script.ScriptContext;
import java.util.function.Function;

public class ShutdownHookPlugin {
    private final Logger logger;
    private final NBComponent baseComponent;

    public ShutdownHookPlugin(Logger logger, NBComponent baseComponent) {

        this.logger = logger;
        this.baseComponent = baseComponent;
    }

    public void addShutdownHook(String name, Object f) {
        if (!(f instanceof Function)) {
            throw new RuntimeException("The object provided to the shutdown hook plugin was not recognized as a function.");
        }
        String shutdownName = "shutdown-function-" + name;
        Thread runnable = new ShutdownRunnableFunction(logger, name, (Function<?,?>)f);
        runnable.setName(shutdownName);
        Runtime.getRuntime().addShutdownHook(runnable);
        logger.info(() -> "Registered shutdown hook to run under name '" + shutdownName + "'");

    }
}
