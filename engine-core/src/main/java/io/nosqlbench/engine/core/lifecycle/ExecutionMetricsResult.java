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

import com.codahale.metrics.Counting;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricAttribute;
import com.codahale.metrics.MetricFilter;
import io.nosqlbench.nb.api.engine.metrics.instruments.*;
import io.nosqlbench.nb.api.engine.metrics.reporters.ConsoleReporter;
import io.nosqlbench.nb.api.engine.metrics.reporters.Log4JMetricsReporter;
import io.nosqlbench.nb.api.components.core.NBCreators;
import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.api.components.core.NBComponentTraversal;
import io.nosqlbench.nb.api.components.core.NBFinders;
import io.nosqlbench.engine.core.metrics.NBMetricsSummary;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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

    public String getMetricsSummary(NBComponent component) {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        try (final PrintStream ps = new PrintStream(os)) {
            final NBCreators.ConsoleReporterBuilder builder = new NBCreators.ConsoleReporterBuilder(component, ps);
            final Set<MetricAttribute> disabled = new HashSet<>(ExecutionMetricsResult.INTERVAL_ONLY_METRICS);
            if (60000 > this.getElapsedMillis()) disabled.addAll(ExecutionMetricsResult.OVER_ONE_MINUTE_METRICS);
            builder.disabledMetricAttributes(disabled);
            final ConsoleReporter consoleReporter = builder.build();
            consoleReporter.report();
            consoleReporter.close();
        }
        final String result = os.toString(StandardCharsets.UTF_8);
        return result;
    }

    public void reportToConsole(NBComponent component) {
        final String summaryReport = this.getMetricsSummary(component);
        System.out.println(summaryReport);
    }


    public void reportMetricsSummaryTo(NBComponent component, final PrintStream out) {
        out.println(this.getMetricsSummary(component));
    }

    public void reportMetricsSummaryToLog(NBComponent component) {
        ExecutionResult.logger.debug("-- WARNING: Metrics which are taken per-interval (like histograms) will not have --");
        ExecutionResult.logger.debug("-- active data on this last report. (The workload has already stopped.) Record   --");
        ExecutionResult.logger.debug("-- metrics to an external format to see values for each reporting interval.      --");
        ExecutionResult.logger.debug("-- BEGIN METRICS DETAIL --");
        final Log4JMetricsReporter reporter = new NBCreators.Log4jReporterBuilder(component)
            .withLoggingLevel(Log4JMetricsReporter.LoggingLevel.DEBUG)
            .filter(MetricFilter.ALL)
            .outputTo(ExecutionResult.logger)
            .build();

        reporter.report(NBFinders.allMetricsWithType(NBMetricGauge.class, component),
            NBFinders.allMetricsWithType(NBMetricCounter.class, component),
            NBFinders.allMetricsWithType(NBMetricHistogram.class, component),
            NBFinders.allMetricsWithType(NBMetricMeter.class, component),
            NBFinders.allMetricsWithType(NBMetricTimer.class, component));
        reporter.close();
        ExecutionResult.logger.debug("-- END METRICS DETAIL --");
    }

    public void reportMetricsCountsTo(NBComponent component, final PrintStream printStream) {
        final StringBuilder sb = new StringBuilder();
        Iterator<NBComponent> allMetrics = NBComponentTraversal.traverseBreadth(component);
        allMetrics.forEachRemaining(m -> {
            for (NBMetric metric : m.findComponentMetrics("")) {
                if (metric instanceof Counting counting) {
                    final long count = counting.getCount();
                    if (0 < count) {
                        NBMetricsSummary.summarize(sb, metric.getLabels().linearizeAsMetrics(), metric);
                    }
                } else if (metric instanceof Gauge<?> gauge) {
                    final Object value = gauge.getValue();
                    if (value instanceof Number n) if (0 != n.doubleValue()) {
                        NBMetricsSummary.summarize(sb, metric.getLabels().linearizeAsMetrics(), metric);
                    }
                }
            }
        });
        printStream.println("-- BEGIN NON-ZERO metric counts (run longer for full report):");
        printStream.print(sb);
        printStream.println("-- END NON-ZERO metric counts:");
    }
}
