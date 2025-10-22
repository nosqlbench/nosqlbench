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

package io.nosqlbench.nb.api.engine.metrics.reporters;

import com.codahale.metrics.MetricAttribute;
import com.codahale.metrics.MetricFilter;
import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.api.components.core.NBCreators;
import io.nosqlbench.nb.api.engine.metrics.view.MetricsView;
import io.nosqlbench.nb.api.engine.metrics.view.MetricsView.MeterSample;
import io.nosqlbench.nb.api.engine.metrics.view.MetricsView.MetricFamily;
import io.nosqlbench.nb.api.engine.metrics.view.MetricsView.PointSample;
import io.nosqlbench.nb.api.engine.metrics.view.MetricsView.RateStatistics;
import io.nosqlbench.nb.api.engine.metrics.view.MetricsView.Sample;
import io.nosqlbench.nb.api.engine.metrics.view.MetricsView.SummarySample;
import io.nosqlbench.nb.api.engine.metrics.view.MetricsView.SummaryStatistics;
import io.nosqlbench.nb.api.labels.NBLabels;
import org.apache.logging.log4j.Marker;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Log4J reporter that logs metrics snapshots emitted by the shared scheduler to a specified logger proxy.
 */
public class Log4JMetricsReporter extends MetricsSnapshotReporterBase {

    public enum LoggingLevel { TRACE, DEBUG, INFO, WARN, ERROR }

    private final NBCreators.LoggerProxy loggerProxy;
    private final Marker marker;
    private final TimeUnit rateUnit = TimeUnit.NANOSECONDS;
    private final TimeUnit durationUnit = TimeUnit.NANOSECONDS;
    private final long durationFactor = TimeUnit.NANOSECONDS.toNanos(1);
    private final long rateFactor = TimeUnit.NANOSECONDS.toSeconds(1);

    public Log4JMetricsReporter(final NBComponent component,
                                final NBCreators.LoggerProxy loggerProxy,
                                final Marker marker,
                                final MetricFilter filter,
                                final NBLabels extraLabels,
                                final long intervalMillis,
                                final boolean oneLastTime) {
        super(component, extraLabels, intervalMillis);
        this.loggerProxy = loggerProxy;
        this.marker = marker;
    }

    @Override
    public void onMetricsSnapshot(MetricsView view) {
        report(view);
    }

    public void report(MetricsView view) {
        if (!this.loggerProxy.isEnabled(this.marker)) {
            return;
        }
        for (MetricFamily family : view.families()) {
            switch (family.type()) {
                case GAUGE -> logGaugeFamily(family);
                case COUNTER -> logCounterFamily(family);
                case SUMMARY -> logSummaryFamily(family);
                default -> {
                }
            }
        }
    }

    public void report(List<io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricGauge> gauges,
                       List<io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricCounter> counters,
                       List<io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricHistogram> histograms,
                       List<io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricMeter> meters,
                       List<io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricTimer> timers) {
        ArrayList<io.nosqlbench.nb.api.engine.metrics.instruments.NBMetric> combined = new ArrayList<>(
            gauges.size() + counters.size() + histograms.size() + meters.size() + timers.size()
        );
        combined.addAll(gauges);
        combined.addAll(counters);
        combined.addAll(histograms);
        combined.addAll(meters);
        combined.addAll(timers);
        MetricsView view = MetricsView.capture(combined, getIntervalMillis());
        report(view);
    }

    private void logGaugeFamily(MetricFamily family) {
        for (Sample sample : family.samples()) {
            if (sample instanceof MeterSample meterSample) {
                logMeter(family, meterSample);
            } else if (sample instanceof PointSample pointSample) {
                logGauge(family, pointSample);
            }
        }
    }

    private void logCounterFamily(MetricFamily family) {
        for (Sample sample : family.samples()) {
            if (sample instanceof PointSample pointSample) {
                logCounter(family, pointSample);
            }
        }
    }

    private void logSummaryFamily(MetricFamily family) {
        for (Sample sample : family.samples()) {
            if (sample instanceof SummarySample summarySample) {
                if (summarySample.rates() != null) {
                    logTimer(family, summarySample);
                } else {
                    logHistogram(family, summarySample);
                }
            }
        }
    }

    private void logGauge(MetricFamily family, PointSample sample) {
        this.loggerProxy.log(
            this.marker,
            "type={}, name={}, value={}",
            "GAUGE",
            renderMetricIdentity(family, sample),
            sample.value()
        );
    }

    private void logCounter(MetricFamily family, PointSample sample) {
        this.loggerProxy.log(
            this.marker,
            "type={}, name={}, count={}",
            "COUNTER",
            renderMetricIdentity(family, sample),
            (long) sample.value()
        );
    }

    private void logMeter(MetricFamily family, MeterSample sample) {
        this.loggerProxy.log(
            this.marker,
            "type={}, name={}, count={}, mean_rate={}, m1={}, m5={}, m15={}, rate_unit={}",
            "METER",
            renderMetricIdentity(family, sample),
            sample.count(),
            convertRate(sample.meanRate()),
            convertRate(sample.oneMinuteRate()),
            convertRate(sample.fiveMinuteRate()),
            convertRate(sample.fifteenMinuteRate()),
            getRateUnit()
        );
    }

    private void logHistogram(MetricFamily family, SummarySample sample) {
        SummaryStatistics stats = sample.statistics();
        this.loggerProxy.log(
            this.marker,
            "type={}, name={}, count={}, min={}, max={}, mean={}, stddev={}, median={}, p75={}, p95={}, p98={}, p99={}, p999={}",
            "HISTOGRAM",
            renderMetricIdentity(family, sample),
            stats.count(),
            (long) stats.min(),
            (long) stats.max(),
            stats.mean(),
            stats.stddev(),
            sample.quantiles().getOrDefault(0.5d, Double.NaN),
            sample.quantiles().getOrDefault(0.75d, Double.NaN),
            sample.quantiles().getOrDefault(0.95d, Double.NaN),
            sample.quantiles().getOrDefault(0.98d, Double.NaN),
            sample.quantiles().getOrDefault(0.99d, Double.NaN),
            sample.quantiles().getOrDefault(0.999d, Double.NaN)
        );
    }

    private void logTimer(MetricFamily family, SummarySample sample) {
        SummaryStatistics stats = sample.statistics();
        RateStatistics rates = sample.rates();
        this.loggerProxy.log(
            this.marker,
            "type={}, name={}, count={}, min={}, max={}, mean={}, stddev={}, median={}, p75={}, p95={}, p98={}, p99={}, p999={}, mean_rate={}, m1={}, m5={}, m15={}, rate_unit={}, duration_unit={}",
            "TIMER",
            renderMetricIdentity(family, sample),
            stats.count(),
            convertDuration(stats.min()),
            convertDuration(stats.max()),
            convertDuration(stats.mean()),
            convertDuration(stats.stddev()),
            convertDuration(sample.quantiles().getOrDefault(0.5d, Double.NaN)),
            convertDuration(sample.quantiles().getOrDefault(0.75d, Double.NaN)),
            convertDuration(sample.quantiles().getOrDefault(0.95d, Double.NaN)),
            convertDuration(sample.quantiles().getOrDefault(0.98d, Double.NaN)),
            convertDuration(sample.quantiles().getOrDefault(0.99d, Double.NaN)),
            convertDuration(sample.quantiles().getOrDefault(0.999d, Double.NaN)),
            (rates != null) ? convertRate(rates.mean()) : Double.NaN,
            (rates != null) ? convertRate(rates.oneMinute()) : Double.NaN,
            (rates != null) ? convertRate(rates.fiveMinute()) : Double.NaN,
            (rates != null) ? convertRate(rates.fifteenMinute()) : Double.NaN,
            getRateUnit(),
            durationUnit
        );
    }

    protected double convertDuration(double duration) {
        return duration / durationFactor;
    }

    protected double convertRate(double rate) {
        return rate * rateFactor;
    }

    protected String getRateUnit() {
        return "events/" + rateUnit;
    }

    private String renderMetricIdentity(MetricFamily family, Sample sample) {
        String baseName = (family.originalName() == null || family.originalName().isBlank())
            ? family.familyName()
            : family.originalName();
        NBLabels labels = sample.labels();
        if (labels == null || labels.isEmpty()) {
            return baseName;
        }
        Map<String, String> labelMap = new LinkedHashMap<>(labels.asMap());
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
}
