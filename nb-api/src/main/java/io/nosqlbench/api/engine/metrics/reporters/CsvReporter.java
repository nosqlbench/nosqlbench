/*
 * Copyright (c) 2023 nosqlbench
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
 *
 */

package io.nosqlbench.api.engine.metrics.reporters;

import com.codahale.metrics.*;
import io.nosqlbench.api.engine.metrics.instruments.NBMetric;
import io.nosqlbench.api.labels.NBLabels;
import io.nosqlbench.components.NBComponent;
import io.nosqlbench.components.PeriodicTaskComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;

public class CsvReporter extends PeriodicTaskComponent {
    private static final Logger logger = LogManager.getLogger(CsvReporter.class);
    private final Path reportTo;
    private final String separator = ",";
    private final MetricInstanceFilter filter;
    private final Locale locale = Locale.US;
    private final Clock clock = Clock.defaultClock();
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
    private final NBComponent component;

    public CsvReporter(NBComponent node, Path reportTo, int interval, MetricInstanceFilter filter,
                       NBLabels extraLabels) {
        super(node, extraLabels, interval, false);
        this.component = node;
        this.reportTo = reportTo;
        this.filter = filter;
        this.durationFactor = durationUnit.toNanos(1);
        this.rateFactor = rateUnit.toSeconds(1);
        this.histogramFormat = String.join(separator, "%d", "%d", "%f", "%d", "%f", "%f", "%f", "%f", "%f", "%f", "%f");
        this.meterFormat = String.join(separator, "%d", "%f", "%f", "%f", "%f", "events/%s");
        this.timerFormat = String.join(separator, "%d", "%f", "%f", "%f", "%f", "%f", "%f", "%f", "%f", "%f", "%f", "%f", "%f", "%f", "%f", "calls/%s", "%s");

        this.timerHeader = String.join(separator, "count", "max", "mean", "min", "stddev", "p50", "p75", "p95", "p98", "p99", "p999", "mean_rate", "m1_rate", "m5_rate", "m15_rate", "rate_unit", "duration_unit");
        this.meterHeader = String.join(separator, "count", "mean_rate", "m1_rate", "m5_rate", "m15_rate", "rate_unit");
        this.histogramHeader = String.join(separator, "count", "max", "mean", "min", "stddev", "p50", "p75", "p95", "p98", "p99", "p999");
    }

    public CsvReporter(NBComponent node, Path reportTo, int interval, MetricInstanceFilter filter) {
        this(node, reportTo, interval, filter, null);
    }

    public void start() {
        List<NBMetric> metrics = component.find().metrics();
        final long timestamp = TimeUnit.MILLISECONDS.toSeconds(clock.getTime());
        for (NBMetric metric : metrics) {
            if (metric instanceof Gauge<?>) {
                reportGauge(timestamp, metric.getLabels().linearizeAsMetrics(), (Gauge<?>) metric);
            } else if (metric instanceof Counter) {
                reportCounter(timestamp, metric.getLabels().linearizeAsMetrics(), (Counter) metric);
            } else if (metric instanceof Histogram) {
                reportHistogram(timestamp, metric.getLabels().linearizeAsMetrics(), (Histogram) metric);
            } else if (metric instanceof Meter) {
                reportMeter(timestamp, metric.getLabels().linearizeAsMetrics(), (Meter) metric);
            } else if (metric instanceof Timer) {
                reportTimer(timestamp, metric.getLabels().linearizeAsMetrics(), (Timer) metric);
            }
        }
    }

    protected double convertDuration(double duration) {
        return duration / durationFactor;
    }

    protected double convertRate(double rate) {
        return rate * rateFactor;
    }

    private void reportTimer(long timestamp, String name, Timer timer) {
        final Snapshot snapshot = timer.getSnapshot();

        report(timestamp,
            name,
            timerHeader,
            timerFormat,
            timer.getCount(),
            convertDuration(snapshot.getMax()),
            convertDuration(snapshot.getMean()),
            convertDuration(snapshot.getMin()),
            convertDuration(snapshot.getStdDev()),
            convertDuration(snapshot.getMedian()),
            convertDuration(snapshot.get75thPercentile()),
            convertDuration(snapshot.get95thPercentile()),
            convertDuration(snapshot.get98thPercentile()),
            convertDuration(snapshot.get99thPercentile()),
            convertDuration(snapshot.get999thPercentile()),
            convertRate(timer.getMeanRate()),
            convertRate(timer.getOneMinuteRate()),
            convertRate(timer.getFiveMinuteRate()),
            convertRate(timer.getFifteenMinuteRate()),
            this.rateUnit,
            this.durationUnit);
    }

    private void reportMeter(long timestamp, String name, Meter meter) {
        report(timestamp,
            name,
            meterHeader,
            meterFormat,
            meter.getCount(),
            convertRate(meter.getMeanRate()),
            convertRate(meter.getOneMinuteRate()),
            convertRate(meter.getFiveMinuteRate()),
            convertRate(meter.getFifteenMinuteRate()),
            this.rateUnit);
    }

    private void reportHistogram(long timestamp, String name, Histogram histogram) {
        final Snapshot snapshot = histogram.getSnapshot();

        report(timestamp,
            name,
            histogramHeader,
            histogramFormat,
            histogram.getCount(),
            snapshot.getMax(),
            snapshot.getMean(),
            snapshot.getMin(),
            snapshot.getStdDev(),
            snapshot.getMedian(),
            snapshot.get75thPercentile(),
            snapshot.get95thPercentile(),
            snapshot.get98thPercentile(),
            snapshot.get99thPercentile(),
            snapshot.get999thPercentile());
    }

    private void reportCounter(long timestamp, String name, Counter counter) {
        report(timestamp, name, "count", "%d", counter.getCount());
    }

    private void reportGauge(long timestamp, String name, Gauge<?> gauge) {
        report(timestamp, name, "value", "%s", gauge.getValue());
    }

    private void report(long timestamp, String name, String header, String line, Object... values) {
        try {
            final File file = new File(reportTo + ".csv");
            final boolean fileAlreadyExists = file.exists();
            if (fileAlreadyExists || file.createNewFile()) {
                try (PrintWriter out = new PrintWriter(new OutputStreamWriter(
                    new FileOutputStream(file, true), UTF_8))) {
                    if (!fileAlreadyExists) {
                        out.println("t" + separator + header);
                    }
                    out.printf(locale, String.format(locale, "%d" + separator + "%s%n", timestamp, line), values);
                }
            }
        } catch (IOException e) {
            logger.warn("Error writing to {}", name, e);
        }
    }

    protected String sanitize(String fileName) {
        //TODO: sanitize file name
        return fileName;
    }

    public void teardown() {
        super.teardown();
    }

    @Override
    public void task() {
        this.start();
    }

}
