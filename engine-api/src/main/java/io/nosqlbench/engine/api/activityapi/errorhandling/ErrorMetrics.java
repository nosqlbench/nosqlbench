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

package io.nosqlbench.engine.api.activityapi.errorhandling;

import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.metrics.ExceptionCountMetrics;
import io.nosqlbench.engine.api.metrics.ExceptionHistoMetrics;
import io.nosqlbench.engine.api.metrics.ExceptionMeterMetrics;
import io.nosqlbench.engine.api.metrics.ExceptionTimerMetrics;

import java.util.function.Supplier;

public class ErrorMetrics {

    private final ActivityDef activityDef;
    private ExceptionCountMetrics exceptionCountMetrics;
    private ExceptionHistoMetrics exceptionHistoMetrics;
    private ExceptionMeterMetrics exceptionMeterMetrics;
    private ExceptionTimerMetrics exceptionTimerMetrics;

    public ErrorMetrics(ActivityDef activityDef) {
        this.activityDef = activityDef;
    }

    public synchronized ExceptionCountMetrics getExceptionCountMetrics() {
        if (exceptionCountMetrics == null) {
            exceptionCountMetrics = new ExceptionCountMetrics(activityDef);
        }
        return exceptionCountMetrics;
    }

    public synchronized ExceptionHistoMetrics getExceptionHistoMetrics() {
        if (exceptionHistoMetrics == null) {
            exceptionHistoMetrics = new ExceptionHistoMetrics(activityDef);
        }
        return exceptionHistoMetrics;
    }

    public synchronized ExceptionMeterMetrics getExceptionMeterMetrics() {
        if (exceptionMeterMetrics == null) {
            exceptionMeterMetrics = new ExceptionMeterMetrics(activityDef);
        }
        return exceptionMeterMetrics;
    }

    public synchronized ExceptionTimerMetrics getExceptionTimerMetrics() {
        if (exceptionTimerMetrics == null) {
            exceptionTimerMetrics = new ExceptionTimerMetrics(activityDef);
        }
        return exceptionTimerMetrics;
    }

    public interface Aware {
        void setErrorMetricsSupplier(Supplier<ErrorMetrics> supplier);
    }

}
