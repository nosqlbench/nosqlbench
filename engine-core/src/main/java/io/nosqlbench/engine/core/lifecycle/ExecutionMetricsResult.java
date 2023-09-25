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

package io.nosqlbench.engine.core.lifecycle;

import com.codahale.metrics.*;
import io.nosqlbench.api.engine.metrics.ActivityMetrics;
import io.nosqlbench.api.engine.metrics.MetricsRegistry;
import io.nosqlbench.api.engine.metrics.NBMetricsRegistry;
import io.nosqlbench.api.engine.metrics.reporters.Log4JMetricsReporter;
import io.nosqlbench.api.engine.metrics.reporters.Log4JMetricsReporter.LoggingLevel;
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

    public ExecutionMetricsResult(final long startedAt, final long endedAt, final String iolog, final Exception error) {
        super(startedAt, endedAt, iolog, error);
    }

    public String getMetricsSummary() {
        MetricsRegistry registry = ActivityMetrics.getMetricRegistry();
        if (registry instanceof NBMetricsRegistry) {
            final ByteArrayOutputStream os = new ByteArrayOutputStream();
            try (final PrintStream ps = new PrintStream(os)) {
                final ConsoleReporter.Builder builder = ConsoleReporter.forRegistry((NBMetricsRegistry) registry)
                    .convertDurationsTo(TimeUnit.MICROSECONDS)
                    .convertRatesTo(TimeUnit.SECONDS)
                    .filter(MetricFilter.ALL)
                    .outputTo(ps);
                final Set<MetricAttribute> disabled = new HashSet<>(ExecutionMetricsResult.INTERVAL_ONLY_METRICS);
                if (60000 > this.getElapsedMillis()) disabled.addAll(ExecutionMetricsResult.OVER_ONE_MINUTE_METRICS);
                builder.disabledMetricAttributes(disabled);
                final ConsoleReporter consoleReporter = builder.build();
                consoleReporter.report();
                consoleReporter.close();
            }
            final String result = os.toString(StandardCharsets.UTF_8);
            return result;
        } else {
            throw new RuntimeException("MetricsRegistry type " + registry.getClass().getCanonicalName() + " is not supported.");
        }
    }

    public void reportToConsole() {
        final String summaryReport = this.getMetricsSummary();
        System.out.println(summaryReport);
    }


    public void reportMetricsSummaryTo(final PrintStream out) {
        out.println(this.getMetricsSummary());
    }

    public void reportMetricsSummaryToLog() {
        ExecutionResult.logger.debug("-- WARNING: Metrics which are taken per-interval (like histograms) will not have --");
        ExecutionResult.logger.debug("-- active data on this last report. (The workload has already stopped.) Record   --");
        ExecutionResult.logger.debug("-- metrics to an external format to see values for each reporting interval.      --");
        ExecutionResult.logger.debug("-- BEGIN METRICS DETAIL --");
        final Log4JMetricsReporter reporter = Log4JMetricsReporter.forRegistry(ActivityMetrics.getMetricRegistry())
            .withLoggingLevel(LoggingLevel.DEBUG)
            .convertDurationsTo(TimeUnit.MICROSECONDS)
            .convertRatesTo(TimeUnit.SECONDS)
            .filter(MetricFilter.ALL)
            .outputTo(ExecutionResult.logger)
            .build();
        reporter.report();
        reporter.close();
        ExecutionResult.logger.debug("-- END METRICS DETAIL --");
    }

    public void reportMetricsCountsTo(final PrintStream printStream) {
        final StringBuilder sb = new StringBuilder();

        ActivityMetrics.getMetricRegistry().getMetrics().forEach((k, v) -> {
            if (v instanceof Counting counting) {
                final long count = counting.getCount();
                if (0 < count) NBMetricsSummary.summarize(sb, k, v);
            } else if (v instanceof Gauge<?> gauge) {
                final Object value = gauge.getValue();
                if (value instanceof Number n) if (0 != n.doubleValue()) NBMetricsSummary.summarize(sb, k, v);
            }
        });

        printStream.println("-- BEGIN NON-ZERO metric counts (run longer for full report):");
        printStream.print(sb);
        printStream.println("-- END NON-ZERO metric counts:");

    }
}
