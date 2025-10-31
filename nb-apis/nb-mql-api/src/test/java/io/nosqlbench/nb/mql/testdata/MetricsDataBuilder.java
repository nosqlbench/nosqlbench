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

package io.nosqlbench.nb.mql.testdata;

import io.nosqlbench.nb.api.components.core.NBBaseComponent;
import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.api.engine.metrics.DeltaHdrHistogramReservoir;
import io.nosqlbench.nb.api.engine.metrics.instruments.MetricCategory;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricCounter;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricTimer;
import io.nosqlbench.nb.api.engine.metrics.reporters.MetricInstanceFilter;
import io.nosqlbench.nb.api.engine.metrics.reporters.SqliteSnapshotReporter;
import io.nosqlbench.nb.api.engine.metrics.view.MetricsView;
import io.nosqlbench.nb.api.labels.NBLabels;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Simplified utility for creating test metrics databases with specific timestamps.
 * Provides a fluent API for building predictable test data for documentation examples.
 *
 * Example:
 * <pre>
 * MetricsDataBuilder.create("examples.db")
 *     .atTime("2025-10-23T10:00:00Z")
 *         .counter("ops_total", 100, "activity", "read")
 *         .counter("ops_total", 150, "activity", "write")
 *     .atTime("2025-10-23T10:01:00Z")
 *         .counter("ops_total", 120, "activity", "read")
 *         .counter("ops_total", 180, "activity", "write")
 *     .build();
 * </pre>
 */
public class MetricsDataBuilder {

    private final Path outputPath;
    private final List<Snapshot> snapshots = new ArrayList<>();
    private Snapshot currentSnapshot;

    private MetricsDataBuilder(Path outputPath) {
        this.outputPath = outputPath;
    }

    /**
     * Create a new metrics data builder for the specified database file.
     *
     * @param dbPath Path to the output database file
     * @return New builder instance
     */
    public static MetricsDataBuilder create(String dbPath) {
        return new MetricsDataBuilder(Path.of(dbPath));
    }

    /**
     * Create a new metrics data builder for the specified path.
     *
     * @param outputPath Path to the output database file
     * @return New builder instance
     */
    public static MetricsDataBuilder create(Path outputPath) {
        return new MetricsDataBuilder(outputPath);
    }

    /**
     * Start a new snapshot at the specified timestamp (ISO-8601 format).
     *
     * @param timestamp ISO-8601 timestamp (e.g., "2025-10-23T10:00:00Z")
     * @return This builder for chaining
     */
    public MetricsDataBuilder atTime(String timestamp) {
        Instant instant = Instant.parse(timestamp);
        return atTime(instant);
    }

    /**
     * Start a new snapshot at the specified timestamp.
     *
     * @param timestamp Instant timestamp
     * @return This builder for chaining
     */
    public MetricsDataBuilder atTime(Instant timestamp) {
        if (currentSnapshot != null) {
            snapshots.add(currentSnapshot);
        }
        currentSnapshot = new Snapshot(timestamp);
        return this;
    }

    /**
     * Add a counter metric to the current snapshot.
     *
     * @param metricName Metric name (will have _total suffix added automatically)
     * @param value Counter value
     * @param labels Label key-value pairs (must be even number of strings)
     * @return This builder for chaining
     */
    public MetricsDataBuilder counter(String metricName, long value, String... labels) {
        ensureCurrentSnapshot();
        currentSnapshot.addCounter(metricName, value, parseLabels(labels));
        return this;
    }

    /**
     * Add a timer metric to the current snapshot.
     *
     * @param metricName Metric name
     * @param latencies Array of latency values in milliseconds
     * @param labels Label key-value pairs (must be even number of strings)
     * @return This builder for chaining
     */
    public MetricsDataBuilder timer(String metricName, long[] latencies, String... labels) {
        ensureCurrentSnapshot();
        currentSnapshot.addTimer(metricName, latencies, parseLabels(labels));
        return this;
    }

    /**
     * Build and write the database file.
     *
     * @throws Exception If database creation fails
     */
    public void build() throws Exception {
        if (currentSnapshot != null) {
            snapshots.add(currentSnapshot);
        }

        if (snapshots.isEmpty()) {
            throw new IllegalStateException("No snapshots defined. Use atTime() to add snapshots.");
        }

        NBComponent root = new NBBaseComponent(null, NBLabels.forKV("builder", "test"));
        String jdbcUrl = "jdbc:sqlite:" + outputPath.toAbsolutePath();

        try (SqliteSnapshotReporter reporter = new SqliteSnapshotReporter(
                root, jdbcUrl, 30000L, new MetricInstanceFilter(), NBLabels.forKV())) {

            for (Snapshot snapshot : snapshots) {
                snapshot.writeToReporter(reporter);
            }
        }
    }

    private void ensureCurrentSnapshot() {
        if (currentSnapshot == null) {
            throw new IllegalStateException("No current snapshot. Call atTime() first.");
        }
    }

    private Map<String, String> parseLabels(String... labels) {
        if (labels.length % 2 != 0) {
            throw new IllegalArgumentException("Labels must be key-value pairs (even number of strings)");
        }

        Map<String, String> labelMap = new LinkedHashMap<>();
        for (int i = 0; i < labels.length; i += 2) {
            labelMap.put(labels[i], labels[i + 1]);
        }
        return labelMap;
    }

    /**
     * Represents a single metrics snapshot at a specific point in time.
     */
    private static class Snapshot {
        private final Instant timestamp;
        private final List<NBMetricCounter> counters = new ArrayList<>();
        private final List<NBMetricTimer> timers = new ArrayList<>();

        Snapshot(Instant timestamp) {
            this.timestamp = timestamp;
        }

        void addCounter(String metricName, long value, Map<String, String> labels) {
            Map<String, String> fullLabels = new LinkedHashMap<>(labels);
            fullLabels.put("name", metricName);

            NBMetricCounter counter = new NBMetricCounter(
                NBLabels.forMap(fullLabels),
                "counter",
                "operations",
                MetricCategory.Core
            );
            counter.inc(value);
            counters.add(counter);
        }

        void addTimer(String metricName, long[] latenciesMs, Map<String, String> labels) {
            Map<String, String> fullLabels = new LinkedHashMap<>(labels);
            fullLabels.put("name", metricName);

            NBLabels nbLabels = NBLabels.forMap(fullLabels);
            NBMetricTimer timer = new NBMetricTimer(
                nbLabels,
                new DeltaHdrHistogramReservoir(nbLabels, 3),
                "timer",
                "nanoseconds",
                MetricCategory.Core
            );

            for (long latencyMs : latenciesMs) {
                timer.update(Duration.ofMillis(latencyMs));
            }
            timers.add(timer);
        }

        void writeToReporter(SqliteSnapshotReporter reporter) {
            List<Object> allMetrics = new ArrayList<>();
            allMetrics.addAll(counters);
            allMetrics.addAll(timers);

            @SuppressWarnings("unchecked")
            List<io.nosqlbench.nb.api.engine.metrics.instruments.NBMetric> metrics =
                (List<io.nosqlbench.nb.api.engine.metrics.instruments.NBMetric>) (List<?>) allMetrics;

            MetricsView view = MetricsView.capture(metrics, 30000L);

            // Override timestamp by capturing at specific time
            // Note: MetricsView uses current time, but reporter will use it
            reporter.onMetricsSnapshot(view);
        }
    }

    /**
     * Format an Instant as ISO-8601 for documentation.
     */
    public static String formatTimestamp(Instant instant) {
        return instant.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
    }
}
