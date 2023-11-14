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

package io.nosqlbench.engine.api.activityapi.errorhandling;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.List;

/**
 * Provide some basic error handlers
 */
public class CycleErrorHandlers {

    private final static Logger logger = LogManager.getLogger(CycleErrorHandlers.class);

    public static <T extends Throwable, R> CycleErrorHandler<T, R> log(R result) {
        return (cycle, error, errMsg) -> {
            logger.error("in cycle " + cycle + ": " + errMsg, error);
            return result;
        };
    }

    public static <T extends Throwable, R> CycleErrorHandler<T, R> store(
            List<CycleErrorHandler.Triple> list,
            R result) {
        return (cycle, error, errMsg) -> {
            list.add(new CycleErrorHandler.Triple<>(error, cycle, errMsg, result));
            return result;
        };
    }

    public static <T extends Throwable, R> CycleErrorHandler<T, R> rethrow(String prefix) {
        return (cycle, error, errMsg) -> {
            throw new RuntimeException("rethrown(" + prefix + ") in cycle(" + cycle + ") :" + errMsg, error);
        };
    }

}
