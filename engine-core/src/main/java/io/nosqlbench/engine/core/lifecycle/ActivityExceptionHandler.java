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

package io.nosqlbench.engine.core.lifecycle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ActivityExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static final Logger logger = LogManager.getLogger(ActivityExceptionHandler.class);

    private final ActivityThreadsManager executor;

    public ActivityExceptionHandler(ActivityThreadsManager executor) {
        this.executor = executor;
        logger.debug(() -> "Activity exception handler starting up for executor '" + executor + "'");
    }


    @Override
    public void uncaughtException(Thread t, Throwable e) {
        logger.error("Uncaught exception in thread '" + t.getName() + ", state[" + t.getState() + "], notifying executor '" + executor + "'");
        executor.notifyException(t, e);
    }
}
