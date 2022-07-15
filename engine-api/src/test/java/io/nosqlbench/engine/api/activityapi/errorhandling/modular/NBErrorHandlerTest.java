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

package io.nosqlbench.engine.api.activityapi.errorhandling.modular;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import io.nosqlbench.engine.api.activityapi.errorhandling.ErrorMetrics;
import io.nosqlbench.api.engine.activityimpl.ActivityDef;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class NBErrorHandlerTest {

    private final RuntimeException runtimeException = new RuntimeException("test exception");

    @Test
    public void testNullConfig() {
        ErrorMetrics errorMetrics = new ErrorMetrics(ActivityDef.parseActivityDef("alias=testalias_stop"));
        NBErrorHandler errhandler = new NBErrorHandler(() -> "stop", () -> errorMetrics);
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> errhandler.handleError(runtimeException, 1, 2));
    }

    @Test
    public void testMultipleWithRetry() {
        ErrorMetrics errorMetrics = new ErrorMetrics(ActivityDef.parseActivityDef("alias=testalias_wr"));
        NBErrorHandler eh = new NBErrorHandler(() -> "warn,retry", () -> errorMetrics);
        ErrorDetail detail = eh.handleError(runtimeException, 1, 2);
        assertThat(detail.isRetryable()).isTrue();
    }

    @Test
    public void testHistogramErrorHandler() {
        ErrorMetrics errorMetrics = new ErrorMetrics(ActivityDef.parseActivityDef("alias=testalias_histos"));
        NBErrorHandler eh = new NBErrorHandler(() -> "histogram", () -> {
            return errorMetrics;
        });
        ErrorDetail detail = eh.handleError(runtimeException, 1, 2);
        assertThat(detail.isRetryable()).isFalse();
        List<Histogram> histograms = errorMetrics.getExceptionHistoMetrics().getHistograms();
        assertThat(histograms).hasSize(1);
    }

    @Test
    public void testTimerErrorHandler() {
        ErrorMetrics errorMetrics = new ErrorMetrics(ActivityDef.parseActivityDef("alias=testalias_timers"));
        NBErrorHandler eh = new NBErrorHandler(() -> "timer", () -> {
            return errorMetrics;
        });
        ErrorDetail detail = eh.handleError(runtimeException, 1, 2);
        assertThat(detail.isRetryable()).isFalse();
        List<Timer> histograms = errorMetrics.getExceptionTimerMetrics().getTimers();
        assertThat(histograms).hasSize(1);
    }

    @Test
    public void testCounterErrorHandler() {
        ErrorMetrics errorMetrics = new ErrorMetrics(ActivityDef.parseActivityDef("alias=testalias_counters"));
        NBErrorHandler eh = new NBErrorHandler(() -> "counter", () -> {
            return errorMetrics;
        });
        ErrorDetail detail = eh.handleError(runtimeException, 1, 2);
        assertThat(detail.isRetryable()).isFalse();
        List<Counter> histograms = errorMetrics.getExceptionCountMetrics().getCounters();
        assertThat(histograms).hasSize(1);
    }

    @Test
    public void testMeterErrorHandler() {
        ErrorMetrics errorMetrics = new ErrorMetrics(ActivityDef.parseActivityDef("alias=testalias_meters"));
        NBErrorHandler eh = new NBErrorHandler(() -> "meter", () -> {
            return errorMetrics;
        });
        ErrorDetail detail = eh.handleError(runtimeException, 1, 2);
        assertThat(detail.isRetryable()).isFalse();
        List<Meter> histograms = errorMetrics.getExceptionMeterMetrics().getMeters();
        assertThat(histograms).hasSize(1);
    }

    @Test
    public void testCodeShorthand() {
        ErrorMetrics errorMetrics = new ErrorMetrics(ActivityDef.parseActivityDef("alias=testalias_meters"));
        NBErrorHandler eh = new NBErrorHandler(() -> "handler=code code=42", () -> {
            return errorMetrics;
        });
        ErrorDetail detail = eh.handleError(runtimeException, 1, 2);
        assertThat(detail.isRetryable()).isFalse();
        assertThat(detail.resultCode).isEqualTo(42);
    }
}
