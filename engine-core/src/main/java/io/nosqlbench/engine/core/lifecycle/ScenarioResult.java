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

import com.codahale.metrics.*;
import io.nosqlbench.api.engine.metrics.ActivityMetrics;
import io.nosqlbench.engine.core.logging.Log4JMetricsReporter;
import io.nosqlbench.engine.core.metrics.NBMetricsSummary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ScenarioResult {

    private final static Logger logger = LogManager.getLogger(ScenarioResult.class);
    public static final Set<MetricAttribute> INTERVAL_ONLY_METRICS = Set.of(
        MetricAttribute.MIN,
        MetricAttribute.MAX,
        MetricAttribute.MEAN,
        MetricAttribute.STDDEV,
        MetricAttribute.P50,
        MetricAttribute.P75,
        MetricAttribute.P95,
        MetricAttribute.P98,
        MetricAttribute.P99,
        MetricAttribute.P999);
    public static final Set<MetricAttribute> OVER_ONE_MINUTE_METRICS = Set.of(
        MetricAttribute.MEAN_RATE,
        MetricAttribute.M1_RATE,
        MetricAttribute.M5_RATE,
        MetricAttribute.M15_RATE
    );
    private final long startedAt;
    private final long endedAt;

    private Exception exception;
    private final String iolog;

    public ScenarioResult(String iolog, long startedAt, long endedAt) {
        logger.debug("populating result from iolog");
        if (logger.isDebugEnabled()) {
            StackTraceElement[] st = Thread.currentThread().getStackTrace();
            for (int i = 0; i < st.length; i++) {
                logger.debug(":AT " + st[i].getFileName()+":"+st[i].getLineNumber()+":"+st[i].getMethodName());
                if (i>10) break;
            }
        }
        this.iolog = iolog;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
    }

    public ScenarioResult(Exception e, long startedAt, long endedAt) {
        logger.debug("populating result from exception");
        if (logger.isDebugEnabled()) {
            StackTraceElement[] st = Thread.currentThread().getStackTrace();
            for (int i = 0; i < st.length; i++) {
                logger.debug(":AT " + st[i].getFileName()+":"+st[i].getLineNumber()+":"+st[i].getMethodName());
                if (i>10) break;
            }
        }
        this.iolog = e.getMessage();
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.exception = e;
    }

    public void reportElapsedMillis() {
        logger.info("-- SCENARIO TOOK " + getElapsedMillis() + "ms --");
    }

    public String getSummaryReport() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        ConsoleReporter.Builder builder = ConsoleReporter.forRegistry(ActivityMetrics.getMetricRegistry())
            .convertDurationsTo(TimeUnit.MICROSECONDS)
            .convertRatesTo(TimeUnit.SECONDS)
            .filter(MetricFilter.ALL)
            .outputTo(ps);
        Set<MetricAttribute> disabled = new HashSet<>(INTERVAL_ONLY_METRICS);
        if (this.getElapsedMillis()<60000) {
            disabled.addAll(OVER_ONE_MINUTE_METRICS);
        }
        builder.disabledMetricAttributes(disabled);
        ConsoleReporter consoleReporter = builder.build();
        consoleReporter.report();

        ps.flush();
        String result = os.toString(StandardCharsets.UTF_8);
        return result;
    }

    public void reportToConsole() {
        String summaryReport = getSummaryReport();
        System.out.println(summaryReport);
    }


    public Optional<Exception> getException() {
        return Optional.ofNullable(exception);
    }

    public void rethrowIfError() {
        if (exception != null) {
            if (exception instanceof RuntimeException) {
                throw ((RuntimeException) exception);
            } else {
                throw new RuntimeException(exception);
            }
        }
    }

    public String getIOLog() {
        return this.iolog;
    }

    public long getElapsedMillis() {
        return endedAt - startedAt;
    }

    public void reportTo(PrintStream out) {
        out.println(getSummaryReport());
    }

    public void reportToLog() {
        logger.debug("-- WARNING: Metrics which are taken per-interval (like histograms) will not have --");
        logger.debug("-- active data on this last report. (The workload has already stopped.) Record   --");
        logger.debug("-- metrics to an external format to see values for each reporting interval.      --");
        logger.debug("-- BEGIN METRICS DETAIL --");
        Log4JMetricsReporter reporter = Log4JMetricsReporter.forRegistry(ActivityMetrics.getMetricRegistry())
            .withLoggingLevel(Log4JMetricsReporter.LoggingLevel.DEBUG)
            .convertDurationsTo(TimeUnit.MICROSECONDS)
            .convertRatesTo(TimeUnit.SECONDS)
            .filter(MetricFilter.ALL)
            .outputTo(logger)
            .build();
        reporter.report();
        logger.debug("-- END METRICS DETAIL --");
    }

    public void reportCountsTo(PrintStream printStream) {
        StringBuilder sb = new StringBuilder();

        ActivityMetrics.getMetricRegistry().getMetrics().forEach((k, v) -> {
            if (v instanceof Counting) {
                long count = ((Counting) v).getCount();
                if (count > 0) {
                    NBMetricsSummary.summarize(sb, k, v);
                }
            } else if (v instanceof Gauge) {
                Object value = ((Gauge) v).getValue();
                if (value != null && value instanceof Number) {
                    Number n = (Number) value;
                    if (n.doubleValue() != 0) {
                        NBMetricsSummary.summarize(sb, k, v);
                    }
                }
            }
        });

        printStream.println("-- BEGIN NON-ZERO metric counts (run longer for full report):");
        printStream.print(sb);
        printStream.println("-- END NON-ZERO metric counts:");

    }
}
