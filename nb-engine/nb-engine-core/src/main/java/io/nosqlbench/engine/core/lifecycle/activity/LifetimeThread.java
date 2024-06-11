/*
 * Copyright (c) 2024 nosqlbench
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

package io.nosqlbench.engine.core.lifecycle.activity;

import io.nosqlbench.engine.core.lifecycle.ExecutionResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class LifetimeThread implements Callable<ExecutionResult> {
    private final static Logger logger = LogManager.getLogger();
    private ExecutionResult result = null;

    private final String name;
    private CompletableFuture<ExecutionResult> future;

    public LifetimeThread(String name) {
        this.name = name;
    }

    @Override
    public ExecutionResult call() {
        logger.debug("lifetime scope '" + name + "' starting");
        this.future = new CompletableFuture<ExecutionResult>();

        while (result == null) {
            try {
                this.result = future.get();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        logger.debug("lifetime scope '" + name + "' ending");
        return result;
    }

    public void shutdown(ExecutionResult result) {
        this.future.complete(result);
    }

}
