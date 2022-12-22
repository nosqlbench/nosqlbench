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
import io.nosqlbench.api.engine.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityapi.errorhandling.ErrorMetrics;
import io.nosqlbench.engine.api.activityapi.errorhandling.modular.handlers.CountErrorHandler;
import io.nosqlbench.engine.api.activityapi.errorhandling.modular.handlers.CounterErrorHandler;
import io.nosqlbench.util.NBMock;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class NBErrorHandlerTest {

    private static final String ERROR_HANDLER_APPENDER_NAME = "ErrorHandler";
    private final RuntimeException runtimeException = new RuntimeException("test exception");


    @Test
    void testNullConfig() {
        ErrorMetrics errorMetrics = new ErrorMetrics(ActivityDef.parseActivityDef("alias=testalias_stop"));
        NBErrorHandler errhandler = new NBErrorHandler(() -> "stop", () -> errorMetrics);
        assertThatExceptionOfType(RuntimeException.class)
            .isThrownBy(() -> errhandler.handleError(runtimeException, 1, 2));
    }

    @Test
    void testMultipleWithRetry() {
        ErrorMetrics errorMetrics = new ErrorMetrics(ActivityDef.parseActivityDef("alias=testalias_wr"));
        NBErrorHandler eh = new NBErrorHandler(() -> "warn,retry", () -> errorMetrics);
        ErrorDetail detail = eh.handleError(runtimeException, 1, 2);
        assertThat(detail.isRetryable()).isTrue();
    }

    @Test
    void testWarnErrorHandler() {
        Logger logger = (Logger) LogManager.getLogger("ERRORS");
        NBMock.LogAppender appender = NBMock.registerTestLogger(ERROR_HANDLER_APPENDER_NAME, logger, Level.WARN);

        ErrorMetrics errorMetrics = new ErrorMetrics(ActivityDef.parseActivityDef("alias=testalias_warn"));
        NBErrorHandler eh = new NBErrorHandler(() -> "warn", () -> errorMetrics);
        ErrorDetail detail = eh.handleError(runtimeException, 1, 2);

        logger.getContext().stop(); // force any async appenders to flush
        logger.getContext().start(); // resume processing

        assertThat(detail.isRetryable()).isFalse();
        assertThat(appender.getFirstEntry()).contains("error with cycle");
        appender.cleanup(logger);
    }


    @Test
    void testHistogramErrorHandler() {
        ErrorMetrics errorMetrics = new ErrorMetrics(ActivityDef.parseActivityDef("alias=testalias_histos"));
        NBErrorHandler eh = new NBErrorHandler(() -> "histogram", () -> errorMetrics);
        ErrorDetail detail = eh.handleError(runtimeException, 1, 2);
        assertThat(detail.isRetryable()).isFalse();
        List<Histogram> histograms = errorMetrics.getExceptionHistoMetrics().getHistograms();
        assertThat(histograms).hasSize(1);
    }

    @Test
    void testTimerErrorHandler() {
        ErrorMetrics errorMetrics = new ErrorMetrics(ActivityDef.parseActivityDef("alias=testalias_timers"));
        NBErrorHandler eh = new NBErrorHandler(() -> "timer", () -> errorMetrics);
        ErrorDetail detail = eh.handleError(runtimeException, 1, 2);
        assertThat(detail.isRetryable()).isFalse();
        List<Timer> histograms = errorMetrics.getExceptionTimerMetrics().getTimers();
        assertThat(histograms).hasSize(1);
    }

    @Test
    void testCounterErrorHandler() {
        Logger logger = (Logger) LogManager.getLogger(CounterErrorHandler.class);
        NBMock.LogAppender appender = NBMock.registerTestLogger(ERROR_HANDLER_APPENDER_NAME, logger, Level.INFO);

        ErrorMetrics errorMetrics = new ErrorMetrics(ActivityDef.parseActivityDef("alias=testalias_counters"));
        NBErrorHandler eh = new NBErrorHandler(() -> "counter", () -> errorMetrics);
        ErrorDetail detail = eh.handleError(runtimeException, 1, 2);
        assertThat(detail.isRetryable()).isFalse();
        List<Counter> histograms = errorMetrics.getExceptionCountMetrics().getCounters();
        assertThat(histograms).hasSize(1);

        logger.getContext().stop(); // force any async appenders to flush
        logger.getContext().start(); // resume processing

        assertThat(appender.getFirstEntry()).isNull();
        appender.cleanup(logger);
    }

    @Test
    void testCountErrorHandler() {
        Logger logger = (Logger) LogManager.getLogger(CountErrorHandler.class);
        NBMock.LogAppender appender = NBMock.registerTestLogger(ERROR_HANDLER_APPENDER_NAME, logger, Level.WARN);

        ErrorMetrics errorMetrics = new ErrorMetrics(ActivityDef.parseActivityDef("alias=testalias_count"));
        NBErrorHandler eh = new NBErrorHandler(() -> "count", () -> errorMetrics);
        ErrorDetail detail = eh.handleError(runtimeException, 1, 2);
        assertThat(detail.isRetryable()).isFalse();
        List<Counter> histograms = errorMetrics.getExceptionCountMetrics().getCounters();
        assertThat(histograms).hasSize(1);

        logger.getContext().stop(); // force any async appenders to flush
        logger.getContext().start(); // resume processing

        assertThat(appender.getFirstEntry()).contains("Starting with v4.17 onward, use 'counter'");
        appender.cleanup(logger);
    }


    @Test
    void testMeterErrorHandler() {
        ErrorMetrics errorMetrics = new ErrorMetrics(ActivityDef.parseActivityDef("alias=testalias_meters"));
        NBErrorHandler eh = new NBErrorHandler(() -> "meter", () -> errorMetrics);
        ErrorDetail detail = eh.handleError(runtimeException, 1, 2);
        assertThat(detail.isRetryable()).isFalse();
        List<Meter> histograms = errorMetrics.getExceptionMeterMetrics().getMeters();
        assertThat(histograms).hasSize(1);
    }

    @Test
    void testCodeShorthand() {
        ErrorMetrics errorMetrics = new ErrorMetrics(ActivityDef.parseActivityDef("alias=testalias_meters"));
        NBErrorHandler eh = new NBErrorHandler(() -> "handler=code code=42", () -> errorMetrics);
        ErrorDetail detail = eh.handleError(runtimeException, 1, 2);
        assertThat(detail.isRetryable()).isFalse();
        assertThat(detail.resultCode).isEqualTo(42);
    }

    @Test
    void testErrorLogAppender() {

        Logger logger = (Logger) LogManager.getLogger(ErrorHandler.class);
        NBMock.LogAppender appender = NBMock.registerTestLogger(ERROR_HANDLER_APPENDER_NAME, logger, Level.DEBUG);

        logger.debug("NBErrorHandler is cool.");
        logger.debug("I second that.");

        logger.getContext().stop(); // force any async appenders to flush
        logger.getContext().start(); // resume processing

        List<String> entries = appender.getEntries();
        assertThat(entries).hasSize(2);
        assertThat(appender.getFirstEntry()).isEqualTo("NBErrorHandler is cool.");
        assertThat(entries.get(1)).isEqualTo("I second that.");
        appender.cleanup(logger);
    }


}
