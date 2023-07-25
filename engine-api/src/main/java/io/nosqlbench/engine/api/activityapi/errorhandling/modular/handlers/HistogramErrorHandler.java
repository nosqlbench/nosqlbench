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

import io.nosqlbench.engine.api.activityapi.errorhandling.ErrorMetrics;
import io.nosqlbench.engine.api.activityapi.errorhandling.modular.ErrorDetail;
import io.nosqlbench.engine.api.metrics.ExceptionHistoMetrics;
import io.nosqlbench.engine.api.activityapi.errorhandling.modular.ErrorHandler;
import io.nosqlbench.nb.annotations.Service;

import java.util.function.Supplier;

/**
 * The histogram error handler will create, if needed, a histogram metrics
 * object for the named {@code "errorhistos.simplename" + e.getClass().getSimpleName()},
 * and then add the current session time to it.
 */
@Service(value = ErrorHandler.class, selector = "histogram")
public class HistogramErrorHandler implements ErrorHandler, ErrorMetrics.Aware {
    private ExceptionHistoMetrics exceptionHistoMetrics;

    @Override
    public ErrorDetail handleError(String name, Throwable t, long cycle, long durationInNanos, ErrorDetail detail) {
        exceptionHistoMetrics.update(name, durationInNanos);
        return detail;
    }

    @Override
    public void setErrorMetricsSupplier(Supplier<ErrorMetrics> supplier) {
        this.exceptionHistoMetrics = supplier.get().getExceptionHistoMetrics();
    }
}
