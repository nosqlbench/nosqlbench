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

import io.nosqlbench.nb.api.labels.NBLabels;
import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.api.engine.metrics.view.MetricsView;
import io.nosqlbench.nb.api.engine.metrics.view.MetricsView.MeterSample;
import io.nosqlbench.nb.api.engine.metrics.view.MetricsView.MetricFamily;
import io.nosqlbench.nb.api.engine.metrics.view.MetricsView.PointSample;
import io.nosqlbench.nb.api.engine.metrics.view.MetricsView.Sample;
import io.nosqlbench.nb.api.engine.metrics.view.MetricsView.SummarySample;
import io.nosqlbench.nb.api.engine.metrics.view.MetricsView.SummaryStatistics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class CsvReporter extends MetricsSnapshotReporterBase {
    private static final Logger logger = LogManager.getLogger(CsvReporter.class);
    private final Path reportTo;
    private final String separator = ",";
    private final MetricInstanceFilter filter;
    private final Locale locale = Locale.US;
    private final String histogramFormat;
    private final String meterFormat;
    private final String timerFormat;

    private final String timerHeader;
    private final String meterHeader;
    private final String histogramHeader;
    private final TimeUnit rateUnit = TimeUnit.SECONDS;
    private final TimeUnit durationUnit = TimeUnit.NANOSECONDS;
    private final long durationFactor;
    private final long rateFactor;
    private Map<Path, PrintWriter> outstreams = new HashMap<>();

    public CsvReporter(NBComponent node, Path reportTo, long intervalMs, MetricInstanceFilter filter,
                       NBLabels extraLabels) {
        super(node, extraLabels, intervalMs);
        this.reportTo = reportTo;
        this.filter = (filter != null) ? filter : new MetricInstanceFilter();
        this.durationFactor = durationUnit.toNanos(1);
        this.rateFactor = rateUnit.toSeconds(1);
        this.histogramFormat = String.join(separator, "%d", "%d", "%f", "%d", "%f", "%f", "%f", "%f", "%f", "%f", "%f");
        this.meterFormat = String.join(separator, "%d", "%f", "%f", "%f", "%f", "events/%s");
        this.timerFormat = String.join(separator, "%d", "%f", "%f", "%f", "%f", "%f", "%f", "%f", "%f", "%f", "%f", "%f", "%f", "%f", "%f", "calls/%s", "%s");

        this.timerHeader = String.join(separator, "count", "max", "mean", "min", "stddev", "p50", "p75", "p95", "p98", "p99", "p999", "mean_rate", "m1_rate", "m5_rate", "m15_rate", "rate_unit", "duration_unit");
        this.meterHeader = String.join(separator, "count", "mean_rate", "m1_rate", "m5_rate", "m15_rate", "rate_unit");
        this.histogramHeader = String.join(separator, "count", "max", "mean", "min", "stddev", "p50", "p75", "p95", "p98", "p99", "p999");

        if (Files.exists(reportTo) && !Files.isDirectory(reportTo)) {
            throw new RuntimeException(reportTo.toString() + " already exists and is not a directory.");
        }
        if (!Files.exists(reportTo)) {
            try {
                Files.createDirectories(reportTo, PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxrwx---")));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public CsvReporter(NBComponent node, Path reportTo, long intervalMs, MetricInstanceFilter filter) {
        this(node, reportTo, intervalMs, filter, null);
    }

    @Override
    public void onMetricsSnapshot(MetricsView view) {
        long timestampSeconds = TimeUnit.MILLISECONDS.toSeconds(view.capturedAtEpochMillis());
        NBLabels commonLabels = computeCommonLabels(view);
        logger.info("Factoring out common labels for CSV metrics logging: " + commonLabels.linearizeAsMetrics());

        for (MetricFamily family : view.families()) {
            for (Sample sample : family.samples()) {
                NBLabels diff = sample.labels().difference(commonLabels);
                String name = diff.linearize_bare("scenario", "activity", "name");
                String handle = sample.labels().linearizeAsMetrics();
                if (!filter.matches(handle, sample.labels())) {
                    continue;
                }
                switch (sample) {
                    case PointSample pointSample -> reportGauge(timestampSeconds, name, pointSample);
                    case MeterSample meterSample -> reportMeter(timestampSeconds, name, meterSample);
                    case SummarySample summarySample -> {
                        if (summarySample.rates() != null) {
                            reportTimer(timestampSeconds, name, summarySample);
                        } else {
                            reportHistogram(timestampSeconds, name, summarySample);
                        }
                    }
                    default -> throw new RuntimeException("Unrecognized metric sample type to report '" + sample.getClass().getSimpleName() + "'");
                }
            }
        }
    }

    protected double convertRate(double rate) {
        return rate * rateFactor;
    }

    private void reportTimer(long timestamp, String name, SummarySample sample) {
        SummaryStatistics stats = sample.statistics();
        report(timestamp,
            name,
            timerHeader,
            timerFormat,
            stats.count(),
            convertDuration(stats.max()),
            convertDuration(stats.mean()),
            convertDuration(stats.min()),
            convertDuration(stats.stddev()),
            convertDuration(sample.quantiles().getOrDefault(0.5d, Double.NaN)),
            convertDuration(sample.quantiles().getOrDefault(0.75d, Double.NaN)),
            convertDuration(sample.quantiles().getOrDefault(0.95d, Double.NaN)),
            convertDuration(sample.quantiles().getOrDefault(0.98d, Double.NaN)),
            convertDuration(sample.quantiles().getOrDefault(0.99d, Double.NaN)),
            convertDuration(sample.quantiles().getOrDefault(0.999d, Double.NaN)),
            convertRate(sample.rates() != null ? sample.rates().mean() : 0.0d),
            convertRate(sample.rates() != null ? sample.rates().oneMinute() : 0.0d),
            convertRate(sample.rates() != null ? sample.rates().fiveMinute() : 0.0d),
            convertRate(sample.rates() != null ? sample.rates().fifteenMinute() : 0.0d),
            this.rateUnit,
            this.durationUnit);
    }

    private void reportMeter(long timestamp, String name, MeterSample meter) {
        report(timestamp,
            name,
            meterHeader,
            meterFormat,
            meter.count(),
            convertRate(meter.meanRate()),
            convertRate(meter.oneMinuteRate()),
            convertRate(meter.fiveMinuteRate()),
            convertRate(meter.fifteenMinuteRate()),
            this.rateUnit);
    }

    private void reportHistogram(long timestamp, String name, SummarySample sample) {
        SummaryStatistics stats = sample.statistics();
        report(timestamp,
            name,
            histogramHeader,
            histogramFormat,
            stats.count(),
            stats.max(),
            stats.mean(),
            stats.min(),
            stats.stddev(),
            sample.quantiles().getOrDefault(0.5d, Double.NaN),
            sample.quantiles().getOrDefault(0.75d, Double.NaN),
            sample.quantiles().getOrDefault(0.95d, Double.NaN),
            sample.quantiles().getOrDefault(0.98d, Double.NaN),
            sample.quantiles().getOrDefault(0.99d, Double.NaN),
            sample.quantiles().getOrDefault(0.999d, Double.NaN));
    }

    private void reportGauge(long timestamp, String name, PointSample sample) {
        report(timestamp, name, "value", "%s", sample.value());
    }

    private void report(long timestamp, String name, String header, String line, Object... values) {
        Path pathname = reportTo.resolve(Path.of(name + ".csv")).normalize();
        PrintWriter out = outstreams.computeIfAbsent(pathname, p -> createWriter(p, "t" + separator + header));
        out.printf(locale, String.format(locale, "%d" + separator + "%s%n", timestamp, line), values);
        out.flush();
    }

    private PrintWriter createWriter(Path path, String firstline) {
        try {
            boolean addHeader = !Files.exists(path);
            if (!Files.exists(path)) {
                addHeader = true;
                Files.createFile(path, PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxrwxr--")));
            }
            BufferedWriter buf = Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            PrintWriter out = new PrintWriter(buf);
            if (addHeader) {
                out.println(firstline);
            }
            return out;
        } catch (IOException e) {
            logger.warn("Error writing to {}", path, e);
            throw new RuntimeException(e);
        }
    }

    private double convertDuration(double duration) {
        return duration / durationFactor;
    }

    @Override
    protected void teardown() {
        outstreams.values().forEach(PrintWriter::close);
        outstreams.clear();
        super.teardown();
    }

    private NBLabels computeCommonLabels(MetricsView view) {
        Map<String, String> common = null;
        for (MetricFamily family : view.families()) {
            for (Sample sample : family.samples()) {
                Map<String, String> labels = new LinkedHashMap<>(sample.labels().asMap());
                if (common == null) {
                    common = new LinkedHashMap<>(labels);
                } else {
                    Set<String> keys = new HashSet<>(common.keySet());
                    for (String key : keys) {
                        String current = labels.get(key);
                        if (!Objects.equals(common.get(key), current)) {
                            common.remove(key);
                        }
                    }
                }
            }
        }
        if (common == null || common.isEmpty()) {
            return NBLabels.forMap(Map.of());
        }
        return NBLabels.forMap(common);
    }

}
