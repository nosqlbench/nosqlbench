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

package io.nosqlbench.nb.api.engine.metrics;

import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.api.components.core.NBBaseComponent;
import io.nosqlbench.nb.api.engine.metrics.view.MetricsView;
import org.HdrHistogram.EncodableHistogram;
import org.HdrHistogram.Histogram;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * HistoStatsLogger consumes immutable {@link MetricsView} snapshots from {@link MetricsSnapshotScheduler}
 * and writes interval histogram stats into a CSV file.
 *
 * <p>This avoids directly snapshotting (consume-and-advance) reservoirs from multiple concurrent consumers.</p>
 */
public class HistoStatsLogger extends NBBaseComponent
        implements MetricsSnapshotScheduler.MetricsSnapshotConsumer, MetricsCloseable  {
    private final static Logger logger = LogManager.getLogger(HistoStatsLogger.class);

    private final String sessionName;
    private final TimeUnit timeUnit;
    private final long intervalMillis;
    private final File logfile;
    private HistoStatsCSVWriter writer;
    private final Pattern pattern;
    private final MetricsSnapshotScheduler scheduler;

    public HistoStatsLogger(NBComponent parent, String sessionName, File file, Pattern pattern, long intervalLength, TimeUnit timeUnit) {
        super(parent);
        this.sessionName = sessionName;
        this.logfile = file;
        this.pattern = pattern;
        this.intervalMillis = intervalLength;
        this.timeUnit = timeUnit;
        startLogging();
        this.scheduler = MetricsSnapshotScheduler.register(this, intervalMillis, this);
    }

    public boolean matches(String metricName) {
        return pattern.matcher(metricName).matches();
    }

    /**
     * By convention, it is typical for the logging application
     * to use a comment to indicate the logging application at the head
     * of the log, followed by the log format version, a startLogging time,
     * and a legend (in that order).
     */
    public void startLogging() {
        writer = new HistoStatsCSVWriter(logfile);
        writer.outputComment("logging stats for session " + sessionName);
        writer.outputLogFormatVersion();
        long currentTimeMillis = System.currentTimeMillis();
        writer.outputStartTime(currentTimeMillis);
        writer.outputTimeUnit(timeUnit);
        writer.setBaseTime(currentTimeMillis);
        writer.outputLegend();
    }

    public String toString() {
        return "HistoLogger:" + this.pattern + ":" + this.logfile.getPath() + ":" + this.intervalMillis;
    }

    public long getInterval() {
        return intervalMillis;
    }

    @Override
    public void closeMetrics() {
        try {
            scheduler.unregisterConsumer(this);
        } catch (Exception e) {
            logger.debug("Error while unregistering histo stats logger consumer.", e);
        }
        if (writer != null) {
            writer.close();
        }
    }

    @Override
    public boolean requiresHdrPayload() {
        return true;
    }

    @Override
    public void onMetricsSnapshot(MetricsView view) {
        if (view == null || view.isEmpty()) {
            return;
        }
        for (MetricsView.MetricFamily family : view.families()) {
            if (family.type() != MetricsView.MetricType.SUMMARY) {
                continue;
            }
            for (MetricsView.Sample sample : family.samples()) {
                if (!(sample instanceof MetricsView.SummarySample summarySample)) {
                    continue;
                }
                if (!matchesAny(family, summarySample)) {
                    continue;
                }
                Optional<EncodableHistogram> histogram = summarySample.snapshot().asEncodableHistogram();
                if (histogram.isEmpty()) {
                    continue;
                }
                EncodableHistogram encodable = histogram.get();
                if (!(encodable instanceof Histogram hdrHistogram)) {
                    continue;
                }
                writer.writeInterval(hdrHistogram);
            }
        }
    }

    private boolean matchesAny(MetricsView.MetricFamily family, MetricsView.SummarySample sample) {
        if (matches(family.familyName())) {
            return true;
        }
        if (matches(family.originalName())) {
            return true;
        }
        if (matches(sample.labels().linearizeAsMetrics())) {
            return true;
        }
        return sample.labels().valueOfOptional("name").map(this::matches).orElse(false);
    }
}
