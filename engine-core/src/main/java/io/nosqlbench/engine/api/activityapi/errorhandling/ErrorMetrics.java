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

import io.nosqlbench.nb.api.engine.activityimpl.ActivityDef;
import io.nosqlbench.nb.api.components.NBComponent;
import io.nosqlbench.engine.api.metrics.ExceptionCountMetrics;
import io.nosqlbench.engine.api.metrics.ExceptionExpectedResultVerificationMetrics;
import io.nosqlbench.engine.api.metrics.ExceptionHistoMetrics;
import io.nosqlbench.engine.api.metrics.ExceptionMeterMetrics;
import io.nosqlbench.engine.api.metrics.ExceptionTimerMetrics;

import java.util.function.Supplier;

public class ErrorMetrics {

    private final NBComponent parent;
    private ExceptionCountMetrics exceptionCountMetrics;
    private ExceptionHistoMetrics exceptionHistoMetrics;
    private ExceptionMeterMetrics exceptionMeterMetrics;
    private ExceptionTimerMetrics exceptionTimerMetrics;
    private ExceptionExpectedResultVerificationMetrics exceptionExpectedResultVerificationMetrics;

    public ErrorMetrics(final NBComponent parent) {
        this.parent = parent;
    }

    public synchronized ExceptionCountMetrics getExceptionCountMetrics() {
        if (null == exceptionCountMetrics) this.exceptionCountMetrics = new ExceptionCountMetrics(this.parent);
        return this.exceptionCountMetrics;
    }

    public synchronized ExceptionHistoMetrics getExceptionHistoMetrics() {
        if (null == exceptionHistoMetrics)
            this.exceptionHistoMetrics = new ExceptionHistoMetrics(this.parent, ActivityDef.parseActivityDef(""));
        return this.exceptionHistoMetrics;
    }

    public synchronized ExceptionMeterMetrics getExceptionMeterMetrics() {
        if (null == exceptionMeterMetrics) this.exceptionMeterMetrics = new ExceptionMeterMetrics(this.parent);
        return this.exceptionMeterMetrics;
    }

    public synchronized ExceptionTimerMetrics getExceptionTimerMetrics() {
        if (null == exceptionTimerMetrics)
            this.exceptionTimerMetrics = new ExceptionTimerMetrics(this.parent, ActivityDef.parseActivityDef(""));
        return this.exceptionTimerMetrics;
    }

    public synchronized ExceptionExpectedResultVerificationMetrics getExceptionExpectedResultVerificationMetrics() {
        if (null == exceptionExpectedResultVerificationMetrics)
            this.exceptionExpectedResultVerificationMetrics = new ExceptionExpectedResultVerificationMetrics(this.parent);
        return this.exceptionExpectedResultVerificationMetrics;
    }

    public interface Aware {
        void setErrorMetricsSupplier(Supplier<ErrorMetrics> supplier);
    }

}
