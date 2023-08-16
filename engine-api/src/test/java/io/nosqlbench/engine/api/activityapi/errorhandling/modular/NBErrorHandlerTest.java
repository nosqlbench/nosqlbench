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

package io.nosqlbench.engine.api.activityapi.errorhandling.modular;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import io.nosqlbench.api.errors.ResultMismatchError;
import io.nosqlbench.engine.api.activityapi.errorhandling.ErrorMetrics;
import io.nosqlbench.engine.api.activityapi.errorhandling.modular.handlers.CountErrorHandler;
import io.nosqlbench.engine.api.activityapi.errorhandling.modular.handlers.CounterErrorHandler;
import io.nosqlbench.api.config.NBLabeledElement;
import io.nosqlbench.util.NBMock;
import io.nosqlbench.util.NBMock.LogAppender;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class NBErrorHandlerTest {

    private static final String ERROR_HANDLER_APPENDER_NAME = "ErrorHandler";
    private final RuntimeException runtimeException = new RuntimeException("test exception");


    @Test
    void testNullConfig() {
        final ErrorMetrics errorMetrics = new ErrorMetrics(NBLabeledElement.forKV("activity","testalias_stop"));
        final NBErrorHandler errhandler = new NBErrorHandler(() -> "stop", () -> errorMetrics);
        assertThatExceptionOfType(RuntimeException.class)
            .isThrownBy(() -> errhandler.handleError(this.runtimeException, 1, 2));
    }

    @Test
    void testMultipleWithRetry() {
        final ErrorMetrics errorMetrics = new ErrorMetrics(NBLabeledElement.forKV("activity","testalias_wr"));
        final NBErrorHandler eh = new NBErrorHandler(() -> "warn,retry", () -> errorMetrics);
        final ErrorDetail detail = eh.handleError(this.runtimeException, 1, 2);
        assertThat(detail.isRetryable()).isTrue();
    }

    @Test
    void testWarnErrorHandler() {
        final Logger logger = (Logger) LogManager.getLogger("ERRORS");
        final LogAppender appender = NBMock.registerTestLogger(NBErrorHandlerTest.ERROR_HANDLER_APPENDER_NAME, logger, Level.WARN);

        final ErrorMetrics errorMetrics = new ErrorMetrics(NBLabeledElement.forKV("activity","testalias_warn"));
        final NBErrorHandler eh = new NBErrorHandler(() -> "warn", () -> errorMetrics);
        final ErrorDetail detail = eh.handleError(this.runtimeException, 1, 2);

        logger.getContext().stop(); // force any async appenders to flush
        logger.getContext().start(); // resume processing

        assertThat(detail.isRetryable()).isFalse();
        assertThat(appender.getFirstEntry()).contains("error with cycle");
        appender.cleanup(logger);
    }


    @Test
    void testHistogramErrorHandler() {
        final ErrorMetrics errorMetrics = new ErrorMetrics(NBLabeledElement.forKV("activity","testalias_histos"));
        final NBErrorHandler eh = new NBErrorHandler(() -> "histogram", () -> errorMetrics);
        final ErrorDetail detail = eh.handleError(this.runtimeException, 1, 2);
        assertThat(detail.isRetryable()).isFalse();
        final List<Histogram> histograms = errorMetrics.getExceptionHistoMetrics().getHistograms();
        assertThat(histograms).hasSize(1);
    }

    @Test
    void testTimerErrorHandler() {
        final ErrorMetrics errorMetrics = new ErrorMetrics(NBLabeledElement.forKV("activity","testalias_timers"));
        final NBErrorHandler eh = new NBErrorHandler(() -> "timer", () -> errorMetrics);
        final ErrorDetail detail = eh.handleError(this.runtimeException, 1, 2);
        assertThat(detail.isRetryable()).isFalse();
        final List<Timer> histograms = errorMetrics.getExceptionTimerMetrics().getTimers();
        assertThat(histograms).hasSize(1);
    }

    @Test
    void testCounterErrorHandler() {
        final Logger logger = (Logger) LogManager.getLogger(CounterErrorHandler.class);
        final LogAppender appender = NBMock.registerTestLogger(NBErrorHandlerTest.ERROR_HANDLER_APPENDER_NAME, logger, Level.INFO);

        final ErrorMetrics errorMetrics = new ErrorMetrics(NBLabeledElement.forKV("activity","testalias_counters"));
        final NBErrorHandler eh = new NBErrorHandler(() -> "counter", () -> errorMetrics);
        final ErrorDetail detail = eh.handleError(this.runtimeException, 1, 2);
        assertThat(detail.isRetryable()).isFalse();
        final List<Counter> histograms = errorMetrics.getExceptionCountMetrics().getCounters();
        assertThat(histograms).hasSize(1);

        logger.getContext().stop(); // force any async appenders to flush
        logger.getContext().start(); // resume processing

        assertThat(appender.getFirstEntry()).isNull();
        appender.cleanup(logger);
    }

    @Test
    void testCountErrorHandler() {
        final Logger logger = (Logger) LogManager.getLogger(CountErrorHandler.class);
        final LogAppender appender = NBMock.registerTestLogger(NBErrorHandlerTest.ERROR_HANDLER_APPENDER_NAME, logger, Level.WARN);

        final ErrorMetrics errorMetrics = new ErrorMetrics(NBLabeledElement.forKV("activity","testalias_count"));
        final NBErrorHandler eh = new NBErrorHandler(() -> "count", () -> errorMetrics);
        final ErrorDetail detail = eh.handleError(this.runtimeException, 1, 2);
        assertThat(detail.isRetryable()).isFalse();
        final List<Counter> histograms = errorMetrics.getExceptionCountMetrics().getCounters();
        assertThat(histograms).hasSize(1);

        logger.getContext().stop(); // force any async appenders to flush
        logger.getContext().start(); // resume processing

        assertThat(appender.getFirstEntry()).contains("Starting with v4.17 onward, use 'counter'");
        appender.cleanup(logger);
    }


    @Test
    void testMeterErrorHandler() {
        final ErrorMetrics errorMetrics = new ErrorMetrics(NBLabeledElement.forKV("activity","testalias_meters"));
        final NBErrorHandler eh = new NBErrorHandler(() -> "meter", () -> errorMetrics);
        final ErrorDetail detail = eh.handleError(this.runtimeException, 1, 2);
        assertThat(detail.isRetryable()).isFalse();
        final List<Meter> histograms = errorMetrics.getExceptionMeterMetrics().getMeters();
        assertThat(histograms).hasSize(1);
    }

    @Test
    void testCodeShorthand() {
        final ErrorMetrics errorMetrics = new ErrorMetrics(NBLabeledElement.forKV("activity","testalias_meters"));
        final NBErrorHandler eh = new NBErrorHandler(() -> "handler=code code=42", () -> errorMetrics);
        final ErrorDetail detail = eh.handleError(this.runtimeException, 1, 2);
        assertThat(detail.isRetryable()).isFalse();
        assertThat(detail.resultCode).isEqualTo(42);
    }

    @Test
    void testErrorLogAppender() {

        final Logger logger = (Logger) LogManager.getLogger(ErrorHandler.class);
        final LogAppender appender = NBMock.registerTestLogger(NBErrorHandlerTest.ERROR_HANDLER_APPENDER_NAME, logger, Level.DEBUG);

        logger.debug("NBErrorHandler is cool.");
        logger.debug("I second that.");

        logger.getContext().stop(); // force any async appenders to flush
        logger.getContext().start(); // resume processing

        final List<String> entries = appender.getEntries();
        assertThat(entries).hasSize(2);
        assertThat(appender.getFirstEntry()).isEqualTo("NBErrorHandler is cool.");
        assertThat(entries.get(1)).isEqualTo("I second that.");
        appender.cleanup(logger);
    }

    @ParameterizedTest(name = "Error with {0}")
    @MethodSource
    void testExpectedResultVerificationErrorHandler(String name, Exception error, String log, long retriesCount, long errorsCount, Logger logger) {
        // given
        NBMock.LogAppender appender = NBMock.registerTestLogger(ERROR_HANDLER_APPENDER_NAME, logger, Level.INFO);
        var errorMetrics = new ErrorMetrics(NBLabeledElement.forKV("activity","testalias_result_verification_" + name));
        var eh = new NBErrorHandler(() -> "verifyexpected", () -> errorMetrics);
        var retries = errorMetrics.getExceptionExpectedResultVerificationMetrics().getVerificationRetries();
        var errors = errorMetrics.getExceptionExpectedResultVerificationMetrics().getVerificationErrors();

        Assertions.assertThat(retries.getCount()).isEqualTo(0);
        Assertions.assertThat(errors.getCount()).isEqualTo(0);

        // when
        eh.handleError(error, 1, 2);

        // then
        Assertions.assertThat(retries.getCount()).isEqualTo(retriesCount);
        Assertions.assertThat(errors.getCount()).isEqualTo(errorsCount);

        logger.getContext().stop(); // force any async appenders to flush
        logger.getContext().start(); // resume processing

        assertThat(appender.getFirstEntry()).contains(log);
        appender.cleanup(logger);
    }

    @SuppressWarnings("unused")
    private static Stream<Arguments> testExpectedResultVerificationErrorHandler() {
        Logger logger = (Logger) LogManager.getLogger("VERIFY");
        return Stream.of(
            Arguments.of(
                "retries left",
                new ResultMismatchError("error-message", 5, "<expression>"),
                "Cycle: 1 Verification of result did not pass. 5 retries left.",
                1,
                0,
                logger
            ),
            Arguments.of(
                "no retries left",
                new ResultMismatchError("error-message", 0, "<expression>"),
                "Cycle: 1 Verification of result did not pass following expression: <expression>",
                0,
                1,
                logger
            )
        );
    }

}
