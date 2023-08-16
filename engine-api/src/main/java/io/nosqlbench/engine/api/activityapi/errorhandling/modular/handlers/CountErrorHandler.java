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

package io.nosqlbench.engine.api.activityapi.errorhandling.modular.handlers;

import io.nosqlbench.engine.api.activityapi.errorhandling.modular.ErrorDetail;
import io.nosqlbench.engine.api.activityapi.errorhandling.modular.ErrorHandler;
import io.nosqlbench.nb.annotations.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * This is here to allow the classic name 'count' to work although the
 * modern error handler scheme uses canonical metric type names.
 */
@Service(value = ErrorHandler.class, selector = "count")
public class CountErrorHandler extends CounterErrorHandler {

    public CountErrorHandler() {
        logger.warn("Starting with v4.17 onward, use 'counter'.  See cql-errors.md for usage.");
    }

    private static final Logger logger = LogManager.getLogger(CountErrorHandler.class);

    @Override
    public ErrorDetail handleError(String name, Throwable t, long cycle, long durationInNanos, ErrorDetail detail) {
        return super.handleError(name, t, cycle, durationInNanos, detail);
    }
}
