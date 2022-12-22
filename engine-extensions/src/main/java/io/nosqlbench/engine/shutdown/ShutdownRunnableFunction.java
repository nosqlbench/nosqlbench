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

import org.apache.logging.log4j.Logger;

import java.util.function.Function;


public class ShutdownRunnableFunction extends Thread {
    private final String name;
    private final Function<Object[],Object> function;
    private final Logger logger;

    public ShutdownRunnableFunction(Logger logger, String name, Function<?, ?> function) {
        this.logger = logger;
        this.name = name;
        this.function = (Function<Object[],Object>)function;
    }

    @Override
    public void run() {
        logger.info(() -> "Running shutdown hook '" + name + "'...");
        try {
            Object result = function.apply(new Object[0]);
            if (result instanceof CharSequence) {
                logger.info(() -> "shutdown hook returned output:\n" + result);
            }
            logger.info(() -> "Completed shutdown hook '" + name + "'...");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
