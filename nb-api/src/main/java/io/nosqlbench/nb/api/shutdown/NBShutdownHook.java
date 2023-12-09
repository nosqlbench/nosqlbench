/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.nb.api.shutdown;

import io.nosqlbench.nb.api.components.core.NBBaseComponent;
import io.nosqlbench.nb.api.components.core.NBComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Function;

public class NBShutdownHook extends NBBaseComponent {
    private final Logger logger = LogManager.getLogger(NBShutdownHook.class);

    public NBShutdownHook(NBComponent baseComponent) {
        super(baseComponent);
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
