/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.nb.api.engine.metrics.reporters;

import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetric;
import io.nosqlbench.nb.api.engine.metrics.view.MetricsView;
import io.nosqlbench.nb.api.engine.metrics.view.MetricsView.MeterSample;
import io.nosqlbench.nb.api.engine.metrics.view.MetricsView.MetricFamily;
import io.nosqlbench.nb.api.engine.metrics.view.MetricsView.MetricType;
import io.nosqlbench.nb.api.engine.metrics.view.MetricsView.PointSample;
import io.nosqlbench.nb.api.engine.metrics.view.MetricsView.RateStatistics;
import io.nosqlbench.nb.api.engine.metrics.view.MetricsView.Sample;
import io.nosqlbench.nb.api.engine.metrics.view.MetricsView.SummarySample;
import io.nosqlbench.nb.api.engine.metrics.view.MetricsView.SummaryStatistics;
import io.nosqlbench.nb.api.labels.NBLabels;

import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * Utility for encoding {@link MetricsView} data structures into the OpenMetrics
 * exposition text format.
 */
public final class PromExpositionFormat {

    private PromExpositionFormat() {
    }

    /**
     * Render the provided metrics in OpenMetrics text format.
     *
     * @param clock
     *     observation time source
     * @param metrics
     *     metrics to render
     * @return formatted exposition
     */
    public static String format(final Clock clock, final NBMetric... metrics) {
        MetricsView view = MetricsView.capture(List.of(metrics), MetricsView.DEFAULT_INTERVAL);
        return format(clock, view);
    }

    /**
     * Render the provided {@link MetricsView} in OpenMetrics text format.
     *
     * @param clock observation time source
     * @param view  immutable metrics view
     * @return formatted exposition
     */
    public static String format(final Clock clock, final MetricsView view) {
        return format(clock, new StringBuilder(), view).toString();
    }

    /**
     * Render the provided {@link MetricsView} in OpenMetrics text format, appending
     * to the supplied {@link StringBuilder}.
     *
     * @param clock observation time source
     * @param builder output accumulator
     * @param view immutable metrics view
     * @return the accumulator provided (for chaining)
     */
    public static StringBuilder format(final Clock clock, final StringBuilder builder, final MetricsView view) {
        Objects.requireNonNull(clock, "clock");
        Objects.requireNonNull(builder, "builder");
        Objects.requireNonNull(view, "view");

        final Instant instant = clock.instant();
        final long epochMillis = instant.toEpochMilli();
        final HeaderRegistry headers = new HeaderRegistry(builder);

        for (MetricFamily family : view.families()) {
            for (Sample sample : family.samples()) {
                switch (family.type()) {
                    case GAUGE -> encodeGaugeSample(builder, headers, family, sample, epochMillis);
                    case COUNTER -> encodeCounterSample(builder, headers, family, sample, epochMillis);
                    case SUMMARY -> encodeSummarySample(builder, headers, family, sample, epochMillis);
                    case HISTOGRAM -> encodeHistogramSample(builder, headers, family, sample, epochMillis);
                    default -> {
                        // ignore unsupported types
                    }
                }
            }
        }

        return builder;
    }

    private static void encodeGaugeSample(StringBuilder buffer,
                                          HeaderRegistry headers,
                                          MetricFamily family,
                                          Sample sample,
                                          long epochMillis) {
        if (sample instanceof MeterSample meterSample) {
            // count as monotonic counter
            String counterName = sample.sampleName() + "_total";
            headers.ensure(counterName, "counter", family.help(), family.unit(), family.categories());
            Map<String, String> labels = baseLabels(sample);
            appendSample(buffer, counterName, labels, meterSample.count(), epochMillis);

            // rate gauges
            emitRateGauge(buffer, headers, family, sample, meterSample.meanRate(), "_mean_rate", epochMillis);
            emitRateGauge(buffer, headers, family, sample, meterSample.oneMinuteRate(), "_m1_rate", epochMillis);
            emitRateGauge(buffer, headers, family, sample, meterSample.fiveMinuteRate(), "_m5_rate", epochMillis);
            emitRateGauge(buffer, headers, family, sample, meterSample.fifteenMinuteRate(), "_m15_rate", epochMillis);
        } else if (sample instanceof PointSample pointSample) {
            headers.ensure(pointSample.sampleName(), toTypeString(MetricType.GAUGE), family.help(), family.unit(), family.categories());
            Map<String, String> labels = baseLabels(pointSample);
            appendSample(buffer, pointSample.sampleName(), labels, pointSample.value(), epochMillis);
        }
    }

    private static void emitRateGauge(StringBuilder buffer,
                                      HeaderRegistry headers,
                                      MetricFamily family,
                                      Sample sample,
                                      double value,
                                      String suffix,
                                      long epochMillis) {
        String metricName = sample.sampleName() + suffix;
        headers.ensure(metricName, "gauge", family.help(), family.unit(), family.categories());
        appendSample(buffer, metricName, baseLabels(sample), value, epochMillis);
    }

    private static void encodeCounterSample(StringBuilder buffer,
                                            HeaderRegistry headers,
                                            MetricFamily family,
                                            Sample sample,
                                            long epochMillis) {
        if (!(sample instanceof PointSample pointSample)) {
            return;
        }
        headers.ensure(pointSample.sampleName(), "counter", family.help(), family.unit(), family.categories());
        appendSample(buffer, pointSample.sampleName(), baseLabels(pointSample), pointSample.value(), epochMillis);
    }

    private static void encodeSummarySample(StringBuilder buffer,
                                            HeaderRegistry headers,
                                            MetricFamily family,
                                            Sample sample,
                                            long epochMillis) {
        if (!(sample instanceof SummarySample summarySample)) {
            return;
        }
        String baseName = summarySample.sampleName();
        Map<String, String> labels = baseLabels(summarySample);

        headers.ensure(baseName, "summary", family.help(), family.unit(), family.categories());

        for (Map.Entry<Double, Double> entry : summarySample.quantiles().entrySet()) {
            Map<String, String> quantileLabels = new LinkedHashMap<>(labels);
            quantileLabels.put("quantile", trimQuantile(entry.getKey()));
            appendSample(buffer, baseName, quantileLabels, entry.getValue(), epochMillis);
        }

        SummaryStatistics stats = summarySample.statistics();
        headers.ensure(baseName + "_count", "counter", null, null, null);
        appendSample(buffer, baseName + "_count", labels, stats.count(), epochMillis);

        headers.ensure(baseName + "_sum", "counter", null, null, null);
        appendSample(buffer, baseName + "_sum", labels, summarySample.sum(), epochMillis);

        headers.ensure(baseName + "_min", "gauge", null, family.unit(), null);
        appendSample(buffer, baseName + "_min", labels, stats.min(), epochMillis);

        headers.ensure(baseName + "_max", "gauge", null, family.unit(), null);
        appendSample(buffer, baseName + "_max", labels, stats.max(), epochMillis);

        RateStatistics rates = summarySample.rates();
        if (rates != null) {
            emitRateGauge(buffer, headers, family, summarySample, rates.mean(), "_mean_rate", epochMillis);
            emitRateGauge(buffer, headers, family, summarySample, rates.oneMinute(), "_m1_rate", epochMillis);
            emitRateGauge(buffer, headers, family, summarySample, rates.fiveMinute(), "_m5_rate", epochMillis);
            emitRateGauge(buffer, headers, family, summarySample, rates.fifteenMinute(), "_m15_rate", epochMillis);
        }
    }

    private static void encodeHistogramSample(StringBuilder buffer,
                                              HeaderRegistry headers,
                                              MetricFamily family,
                                              Sample sample,
                                              long epochMillis) {
        // At present histograms are expressed via SummarySample
        encodeSummarySample(buffer, headers, family, sample, epochMillis);
    }

    private static Map<String, String> baseLabels(Sample sample) {
        Map<String, String> labels = new LinkedHashMap<>(sample.labels().asMap());
        labels.remove("name");
        labels.remove("unit");
        return labels;
    }

    private static void appendSample(StringBuilder buffer,
                                     String metricName,
                                     Map<String, String> labels,
                                     Number value,
                                     long epochMillis) {
        buffer.append(metricName);
        if (!labels.isEmpty()) {
            buffer.append('{');
            StringJoiner joiner = new StringJoiner(",");
            labels.forEach((k, v) -> joiner.add(k + "=\"" + escapeLabelValue(v) + "\""));
            buffer.append(joiner);
            buffer.append('}');
        }
        buffer.append(' ');
        buffer.append(formatNumber(value));
        buffer.append(' ');
        buffer.append(epochMillis);
        buffer.append('\n');
    }

    private static String formatNumber(Number value) {
        if (value == null) {
            return "NaN";
        }
        if (value instanceof Long || value instanceof Integer || value instanceof Short || value instanceof Byte) {
            return Long.toString(value.longValue());
        }
        double dbl = value.doubleValue();
        if (Double.isFinite(dbl) && dbl == Math.rint(dbl)) {
            return Long.toString((long) dbl);
        }
        return Double.toString(dbl);
    }

    private static String escapeLabelValue(String value) {
        if (value == null) {
            return "";
        }
        return value
            .replace("\\", "\\\\")
            .replace("\n", "\\n")
            .replace("\"", "\\\"");
    }

    private static String escapeHelp(String value) {
        if (value == null) {
            return "";
        }
        return value
            .replace("\\", "\\\\")
            .replace("\n", "\\n");
    }

    private static String toTypeString(MetricType type) {
        return switch (type) {
            case COUNTER -> "counter";
            case GAUGE -> "gauge";
            case SUMMARY -> "summary";
            case HISTOGRAM -> "histogram";
            default -> "unknown";
        };
    }

    private static String trimQuantile(double quantile) {
        if (quantile == Math.rint(quantile)) {
            return Long.toString((long) quantile);
        }
        String formatted = Double.toString(quantile);
        if (formatted.contains("E") || formatted.contains("e")) {
            return formatted;
        }
        // trim trailing zeros
        while (formatted.contains(".") && formatted.endsWith("0")) {
            formatted = formatted.substring(0, formatted.length() - 1);
        }
        if (formatted.endsWith(".")) {
            formatted = formatted.substring(0, formatted.length() - 1);
        }
        return formatted;
    }

    private static final class HeaderRegistry {
        private final StringBuilder buffer;
        private final Map<String, Boolean> declared = new HashMap<>();

        private HeaderRegistry(StringBuilder buffer) {
            this.buffer = buffer;
        }

        private void ensure(String metricName,
                            String type,
                            String help,
                            String unit,
                            List<?> categories) {
            if (declared.putIfAbsent(metricName, Boolean.TRUE) != null) {
                return;
            }
            if (help != null && !help.isBlank()) {
                buffer.append("# HELP ")
                    .append(metricName)
                    .append(' ')
                    .append(escapeHelp(help))
                    .append('\n');
            }
            buffer.append("# TYPE ")
                .append(metricName)
                .append(' ')
                .append(type)
                .append('\n');
            if (unit != null && !unit.isBlank()) {
                buffer.append("# UNIT ")
                    .append(metricName)
                    .append(' ')
                    .append(unit)
                    .append('\n');
            }
            if (categories != null && !categories.isEmpty()) {
                String rendered = categories.stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(","));
                buffer.append("# CATEGORIES ")
                    .append(metricName)
                    .append(' ')
                    .append(rendered)
                    .append('\n');
            }
        }
    }
}
