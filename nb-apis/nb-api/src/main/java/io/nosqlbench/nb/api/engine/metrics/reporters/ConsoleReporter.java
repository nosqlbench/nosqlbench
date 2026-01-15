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

import com.codahale.metrics.MetricAttribute;
import io.nosqlbench.nb.api.components.core.NBComponent;
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

import java.io.PrintStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Console reporter that renders metrics snapshots supplied by the {@link io.nosqlbench.nb.api.engine.metrics.MetricsSnapshotScheduler}.
 */
public class ConsoleReporter extends MetricsSnapshotReporterBase {

    private static final int CONSOLE_WIDTH = 80;

    private final PrintStream output;
    private final Locale locale = Locale.US;
    private final DateFormat dateFormat;
    private final Set<MetricAttribute> disabledMetricAttributes;
    private final String rateUnit;
    private final long rateFactor;
    private final String durationUnit = TimeUnit.NANOSECONDS.toString().toLowerCase(Locale.US);
    private final long durationFactor = TimeUnit.NANOSECONDS.toNanos(1);
    private final boolean oneLastTime;

    private volatile MetricsView lastView;

    public ConsoleReporter(NBComponent node,
                           NBLabels extraLabels,
                           long intervalMillis,
                           boolean oneLastTime,
                           PrintStream output,
                           Set<MetricAttribute> disabledMetricAttributes) {
        super(node, extraLabels, intervalMillis);
        this.output = output;
        this.oneLastTime = oneLastTime;
        this.dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, locale);
        this.dateFormat.setTimeZone(TimeZone.getDefault());
        this.disabledMetricAttributes = disabledMetricAttributes != null ? disabledMetricAttributes : Set.of();
        String s = TimeUnit.SECONDS.toString().toLowerCase(Locale.US);
        this.rateUnit = s.substring(0, s.length() - 1);
        this.rateFactor = TimeUnit.SECONDS.toSeconds(1);
    }

    @Override
    public void onMetricsSnapshot(MetricsView view) {
        this.lastView = view;
        renderSnapshot(view, view.capturedAtEpochMillis());
    }

    public void report(MetricsView view) {
        renderSnapshot(view, System.currentTimeMillis());
    }

    public void reportCounts(MetricsView view) {
        renderCounts(view, view.capturedAtEpochMillis());
    }

    private void renderSnapshot(MetricsView view, long epochMillis) {
        final String dateTime = dateFormat.format(new Date(epochMillis));
        printWithBanner(dateTime, '=');
        output.println();

        Map<String, List<MetricFamily>> grouped = groupFamilies(view);

        if (!grouped.getOrDefault("gauge", List.of()).isEmpty()) {
            printWithBanner("-- Gauges", '-');
            grouped.get("gauge").forEach(this::printGaugeFamily);
            output.println();
        }

        if (!grouped.getOrDefault("counter", List.of()).isEmpty()) {
            printWithBanner("-- Counters", '-');
            grouped.get("counter").forEach(this::printCounterFamily);
            output.println();
        }

        if (!grouped.getOrDefault("meter", List.of()).isEmpty()) {
            printWithBanner("-- Meters", '-');
            grouped.get("meter").forEach(this::printMeterFamily);
            output.println();
        }

        if (!grouped.getOrDefault("summary", List.of()).isEmpty()) {
            printWithBanner("-- Summaries", '-');
            grouped.get("summary").forEach(this::printSummaryFamily);
            output.println();
        }

        if (!grouped.getOrDefault("other", List.of()).isEmpty()) {
            printWithBanner("-- Other Metrics", '-');
            grouped.get("other").forEach(this::printGenericFamily);
            output.println();
        }

    }

    private void renderCounts(MetricsView view, long epochMillis) {
        final String dateTime = dateFormat.format(new Date(epochMillis));
        printWithBanner(dateTime, '=');
        output.println();

        boolean printed = false;
        for (MetricFamily family : view.families()) {
            for (Sample sample : family.samples()) {
                Long count = extractCount(family, sample);
                if (count != null) {
                    if (!printed) {
                        printWithBanner("-- Counts", '-');
                        printed = true;
                    }
                    output.println(formatSampleHeader(family, sample));
                    output.printf(locale, "             count = %d%n", count);
                }
            }
        }
        if (!printed) {
            output.println("no countable metrics available");
        }
        output.println();
        output.flush();
    }

    private Map<String, List<MetricFamily>> groupFamilies(MetricsView view) {
        Map<String, List<MetricFamily>> grouped = new HashMap<>();
        for (MetricFamily family : view.families()) {
            String classification = classifyFamily(family);
            grouped.computeIfAbsent(classification, ignored -> new ArrayList<>()).add(family);
        }
        return grouped;
    }

    private String classifyFamily(MetricFamily family) {
        if (family.type() == MetricType.GAUGE) {
            boolean hasMeterSamples = family.samples().stream().anyMatch(MeterSample.class::isInstance);
            return hasMeterSamples ? "meter" : "gauge";
        }
        if (family.type() == MetricType.SUMMARY) {
            return "summary";
        }
        if (family.type() == MetricType.COUNTER) {
            return "counter";
        }
        return "other";
    }

    private void printGaugeFamily(MetricFamily family) {
        for (Sample sample : family.samples()) {
            if (sample instanceof PointSample pointSample) {
                output.println(formatSampleHeader(family, sample));
                printGauge(pointSample);
            }
        }
    }

    private void printCounterFamily(MetricFamily family) {
        for (Sample sample : family.samples()) {
            if (sample instanceof PointSample pointSample) {
                output.println(formatSampleHeader(family, sample));
                printCounter(pointSample);
            }
        }
    }

    private void printMeterFamily(MetricFamily family) {
        for (Sample sample : family.samples()) {
            if (sample instanceof MeterSample meterSample) {
                output.println(formatSampleHeader(family, sample));
                printMeter(meterSample);
            }
        }
    }

    private void printSummaryFamily(MetricFamily family) {
        for (Sample sample : family.samples()) {
            if (sample instanceof SummarySample summarySample) {
                output.println(formatSampleHeader(family, sample));
                printSummary(summarySample);
            }
        }
    }

    private void printGenericFamily(MetricFamily family) {
        for (Sample sample : family.samples()) {
            if (sample instanceof PointSample pointSample) {
                output.println(formatSampleHeader(family, sample));
                printGauge(pointSample);
            }
        }
    }

    private void printMeter(MeterSample sample) {
        printIfEnabled(MetricAttribute.COUNT, String.format(locale, "             count = %d", sample.count()));
        printIfEnabled(MetricAttribute.MEAN_RATE, String.format(locale, "         mean rate = %2.2f events/%s", convertRate(sample.meanRate()), getRateUnit()));
        printIfEnabled(MetricAttribute.M1_RATE, String.format(locale, "     1-minute rate = %2.2f events/%s", convertRate(sample.oneMinuteRate()), getRateUnit()));
        printIfEnabled(MetricAttribute.M5_RATE, String.format(locale, "     5-minute rate = %2.2f events/%s", convertRate(sample.fiveMinuteRate()), getRateUnit()));
        printIfEnabled(MetricAttribute.M15_RATE, String.format(locale, "    15-minute rate = %2.2f events/%s", convertRate(sample.fifteenMinuteRate()), getRateUnit()));
    }

    private void printCounter(PointSample sample) {
        output.printf(locale, "             count = %d%n", (long) sample.value());
    }

    private void printGauge(PointSample sample) {
        output.printf(locale, "             value = %s%n", sample.value());
    }

    private void printSummary(SummarySample sample) {
        SummaryStatistics stats = sample.statistics();
        printIfEnabled(MetricAttribute.COUNT, String.format(locale, "             count = %d", stats.count()));
        printIfEnabled(MetricAttribute.MIN, String.format(locale, "               min = %2.2f %s", stats.min(), getDurationUnit()));
        printIfEnabled(MetricAttribute.MAX, String.format(locale, "               max = %2.2f %s", stats.max(), getDurationUnit()));
        printIfEnabled(MetricAttribute.MEAN, String.format(locale, "              mean = %2.2f %s", stats.mean(), getDurationUnit()));
        printIfEnabled(MetricAttribute.STDDEV, String.format(locale, "            stddev = %2.2f %s", stats.stddev(), getDurationUnit()));
        printIfEnabled(MetricAttribute.P50, String.format(locale, "            median = %2.2f %s", quantileValue(sample, 0.5d), getDurationUnit()));
        printIfEnabled(MetricAttribute.P75, String.format(locale, "              75%% <= %2.2f %s", quantileValue(sample, 0.75d), getDurationUnit()));
        printIfEnabled(MetricAttribute.P95, String.format(locale, "              95%% <= %2.2f %s", quantileValue(sample, 0.95d), getDurationUnit()));
        printIfEnabled(MetricAttribute.P98, String.format(locale, "              98%% <= %2.2f %s", quantileValue(sample, 0.98d), getDurationUnit()));
        printIfEnabled(MetricAttribute.P99, String.format(locale, "              99%% <= %2.2f %s", quantileValue(sample, 0.99d), getDurationUnit()));
        printIfEnabled(MetricAttribute.P999, String.format(locale, "            99.9%% <= %2.2f %s", quantileValue(sample, 0.999d), getDurationUnit()));

        RateStatistics rates = sample.rates();
        if (rates != null) {
            printIfEnabled(MetricAttribute.MEAN_RATE, String.format(locale, "         mean rate = %2.2f calls/%s", convertRate(rates.mean()), getRateUnit()));
            printIfEnabled(MetricAttribute.M1_RATE, String.format(locale, "     1-minute rate = %2.2f calls/%s", convertRate(rates.oneMinute()), getRateUnit()));
            printIfEnabled(MetricAttribute.M5_RATE, String.format(locale, "     5-minute rate = %2.2f calls/%s", convertRate(rates.fiveMinute()), getRateUnit()));
            printIfEnabled(MetricAttribute.M15_RATE, String.format(locale, "    15-minute rate = %2.2f calls/%s", convertRate(rates.fifteenMinute()), getRateUnit()));
        }
    }

    private void printWithBanner(String text, char bannerChar) {
        output.print(text);
        output.print(' ');
        for (int i = 0; i < (CONSOLE_WIDTH - text.length() - 1); i++) {
            output.print(bannerChar);
        }
        output.println();
    }

    private void printIfEnabled(MetricAttribute attribute, String status) {
        if (!disabledMetricAttributes.contains(attribute)) {
            output.println(status);
        }
    }

    private double convertRate(double rate) {
        return rate * rateFactor;
    }

    private String getRateUnit() {
        return rateUnit;
    }

    private String getDurationUnit() {
        return durationUnit;
    }


    private double convertDuration(double duration) {
        return duration / durationFactor;
    }

    private double quantileValue(SummarySample sample, double quantile) {
        return sample.quantiles().getOrDefault(quantile, Double.NaN);
    }

    private String formatSampleHeader(MetricFamily family, Sample sample) {
        String baseName = (family.originalName() == null || family.originalName().isBlank())
            ? family.familyName()
            : family.originalName();
        if (sample.labels().isEmpty()) {
            return baseName;
        }
        Map<String, String> labelMap = new HashMap<>(sample.labels().asMap());
        labelMap.remove("name");
        labelMap.remove("unit");
        if (labelMap.isEmpty()) {
            return baseName;
        }
        StringBuilder sb = new StringBuilder(baseName);
        sb.append('{');
        labelMap.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> sb.append(entry.getKey())
                .append("=\"")
                .append(entry.getValue())
                .append("\","));
        sb.setLength(sb.length() - 1);
        sb.append('}');
        return sb.toString();
    }

    private Long extractCount(MetricFamily family, Sample sample) {
        if (sample instanceof MeterSample meterSample) {
            return meterSample.count();
        }
        if (sample instanceof SummarySample summarySample) {
            return summarySample.statistics().count();
        }
        if (family.type() == MetricType.COUNTER && sample instanceof PointSample pointSample) {
            return (long) Math.round(pointSample.value());
        }
        return null;
    }

    @Override
    protected void teardown() {
        if (oneLastTime && lastView != null) {
            renderSnapshot(lastView, System.currentTimeMillis());
        }
        output.flush();
        super.teardown();
    }

}
