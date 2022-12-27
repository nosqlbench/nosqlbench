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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ExecutionMetricsResult extends ExecutionResult {

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

    public ExecutionMetricsResult(long startedAt, long endedAt, String iolog, Exception error) {
        super(startedAt, endedAt, iolog, error);
    }

    public String getMetricsSummary() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try (PrintStream ps = new PrintStream(os)) {
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
            consoleReporter.close();
        }
        String result = os.toString(StandardCharsets.UTF_8);
        return result;
    }

    public void reportToConsole() {
        String summaryReport = getMetricsSummary();
        System.out.println(summaryReport);
    }


    public void reportMetricsSummaryTo(PrintStream out) {
        out.println(getMetricsSummary());
    }

    public void reportMetricsSummaryToLog() {
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
        reporter.close();
        logger.debug("-- END METRICS DETAIL --");
    }

    public void reportMetricsCountsTo(PrintStream printStream) {
        StringBuilder sb = new StringBuilder();

        ActivityMetrics.getMetricRegistry().getMetrics().forEach((k, v) -> {
            if (v instanceof Counting counting) {
                long count = counting.getCount();
                if (count > 0) {
                    NBMetricsSummary.summarize(sb, k, v);
                }
            } else if (v instanceof Gauge<?> gauge) {
                Object value = gauge.getValue();
                if (value instanceof Number n) {
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
