package io.nosqlbench.nb.api.engine.metrics;

/*
 * Copyright (c) nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import io.nosqlbench.nb.api.components.core.NBBaseComponent;
import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.api.engine.metrics.instruments.MetricCategory;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricCounter;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricTimer;
import io.nosqlbench.nb.api.engine.metrics.reporters.CsvReporter;
import io.nosqlbench.nb.api.engine.metrics.reporters.MetricInstanceFilter;
import io.nosqlbench.nb.api.engine.metrics.reporters.MetricsSnapshotReporterBase;
import io.nosqlbench.nb.api.engine.metrics.reporters.PromExpositionFormat;
import io.nosqlbench.nb.api.engine.metrics.reporters.SqliteSnapshotReporter;
import io.nosqlbench.nb.api.engine.metrics.view.MetricsView;
import io.nosqlbench.nb.api.labels.NBLabels;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.ZoneId;
import java.util.Base64;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

public class MetricsReporterIntegrationTest {

    static {
        try {
            Path hostsFile = Files.createTempFile("nb-test-hosts", ".txt");
            Files.writeString(hostsFile, "127.0.0.1 localhost\n");
            System.setProperty("jdk.net.hosts.file", hostsFile.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.setProperty("log4j2.hostName", "localhost");
        System.setProperty("log4j2.hostname", "localhost");
        System.setProperty("LOG4J_HOSTNAME", "localhost");
        System.setProperty("Log4jHostName", "localhost");
    }

    @Test
    public void testPrometheusReporterAggregatesCadences() {
        NBComponent root = new NBBaseComponent(null);
        RecordingPromReporter fineReporter = new RecordingPromReporter(root, 100L);
        RecordingPromReporter coarseReporter = new RecordingPromReporter(root, 300L);
        MetricsSnapshotScheduler scheduler = MetricsSnapshotScheduler.lookup(root);

        try {
            emitSnapshots(scheduler, 1, 2, 3, 4, 5, 6);

            List<String> coarseExposures = new ArrayList<>(coarseReporter.exposures());
            assertThat(coarseExposures).hasSize(2);

            double firstWindow = extractPromValue(coarseExposures.getFirst(), "counter_metric_total");
            double secondWindow = extractPromValue(coarseExposures.get(1), "counter_metric_total");

            assertThat(firstWindow).isEqualTo(6.0d);
            assertThat(secondWindow).isEqualTo(15.0d);
        } finally {
            fineReporter.close();
            coarseReporter.close();
            if (scheduler != null) {
                scheduler.teardown();
            }
        }
    }

    @Test
    public void testCsvReporterAggregatesCadences(@TempDir Path tempDir) throws IOException {
        NBComponent root = new NBBaseComponent(null);
        Path fineDir = Files.createDirectory(tempDir.resolve("fine"));
        Path coarseDir = Files.createDirectory(tempDir.resolve("coarse"));

        MetricInstanceFilter filter = new MetricInstanceFilter()
            .addPattern("name=counter_metric");

        CsvReporter fineReporter = new CsvReporter(
            root,
            fineDir,
            100L,
            filter,
            NBLabels.forKV("reporter", "csv", "interval", "100")
        );
        CsvReporter coarseReporter = new CsvReporter(
            root,
            coarseDir,
            300L,
            filter,
            NBLabels.forKV("reporter", "csv", "interval", "300")
        );
        MetricsSnapshotScheduler scheduler = MetricsSnapshotScheduler.lookup(root);

        try {
            emitSnapshots(scheduler, 1, 2, 3, 4, 5, 6);
        } finally {
            fineReporter.close();
            coarseReporter.close();
            if (scheduler != null) {
                scheduler.teardown();
            }
        }

        List<Path> csvFiles;
        try (var paths = Files.list(coarseDir)) {
            csvFiles = paths.filter(p -> p.getFileName().toString().endsWith(".csv")).toList();
        }
        assertThat(csvFiles).hasSize(1);

        List<String> lines = Files.readAllLines(csvFiles.getFirst());
        assertThat(lines).hasSize(3); // header + 2 samples

        double firstWindow = parseCsvValue(lines.get(1));
        double secondWindow = parseCsvValue(lines.get(2));

        assertThat(firstWindow).isEqualTo(6.0d);
        assertThat(secondWindow).isEqualTo(15.0d);
    }

    @Test
    public void testSqliteSnapshotReporterAggregatesCadences(@TempDir Path tempDir) throws SQLException {
        NBComponent root = new NBBaseComponent(null);
        Path fineDb = tempDir.resolve("fine.db");
        Path coarseDb = tempDir.resolve("coarse.db");

        String fineUrl = "jdbc:sqlite:" + fineDb;
        String coarseUrl = "jdbc:sqlite:" + coarseDb;

        MetricInstanceFilter filter = new MetricInstanceFilter()
            .addPattern("name=counter_metric");

        SqliteSnapshotReporter fineReporter = new SqliteSnapshotReporter(
            root,
            fineUrl,
            100L,
            filter,
            NBLabels.forKV("reporter", "sqlite", "interval", "100")
        );
        SqliteSnapshotReporter coarseReporter = new SqliteSnapshotReporter(
            root,
            coarseUrl,
            300L,
            filter,
            NBLabels.forKV("reporter", "sqlite", "interval", "300")
        );
        MetricsSnapshotScheduler scheduler = MetricsSnapshotScheduler.lookup(root);

        try {
            emitSnapshots(scheduler, 1, 2, 3, 4, 5, 6);
        } finally {
            fineReporter.close();
            coarseReporter.close();
            if (scheduler != null) {
                scheduler.teardown();
            }
        }

        try (Connection connection = DriverManager.getConnection(coarseUrl)) {
            List<Double> values = readSqliteValues(connection);
            assertThat(values).containsExactly(6.0d, 15.0d);
        }
    }

    @Test
    public void testSqliteSnapshotReporterOptionallyStoresHistograms(@TempDir Path tempDir) throws SQLException {
        NBComponent disabledRoot = new NBBaseComponent(null);
        NBComponent enabledRoot = new NBBaseComponent(null);
        Path disabledDb = tempDir.resolve("disabled.db");
        Path enabledDb = tempDir.resolve("enabled.db");

        String disabledUrl = "jdbc:sqlite:" + disabledDb;
        String enabledUrl = "jdbc:sqlite:" + enabledDb;

        SqliteSnapshotReporter disabledReporter = new SqliteSnapshotReporter(
            disabledRoot,
            disabledUrl,
            100L,
            new MetricInstanceFilter(),
            false
        );
        SqliteSnapshotReporter enabledReporter = new SqliteSnapshotReporter(
            enabledRoot,
            enabledUrl,
            100L,
            new MetricInstanceFilter(),
            true
        );

        MetricsSnapshotScheduler disabledScheduler = MetricsSnapshotScheduler.lookup(disabledRoot);
        MetricsSnapshotScheduler enabledScheduler = MetricsSnapshotScheduler.lookup(enabledRoot);

        try {
            emitTimerSnapshot(disabledScheduler, 100L, 12L, 18L, 25L);
            emitTimerSnapshot(enabledScheduler, 100L, 12L, 18L, 25L);
        } finally {
            disabledReporter.close();
            enabledReporter.close();
            if (disabledScheduler != null) {
                disabledScheduler.teardown();
            }
            if (enabledScheduler != null) {
                enabledScheduler.teardown();
            }
        }

        try (Connection connection = DriverManager.getConnection(disabledUrl)) {
            assertThat(readHistogramBase64(connection)).isEmpty();
        }

        try (Connection connection = DriverManager.getConnection(enabledUrl)) {
            List<String> histograms = readHistogramBase64(connection);
            assertThat(histograms).isNotEmpty();
            for (String encoded : histograms) {
                assertThat(encoded).isNotBlank();
                Base64.getDecoder().decode(encoded);
            }
        }
    }

    @Test
    public void testSqliteSnapshotReporterStoresHistogramsForAggregatedCadence(@TempDir Path tempDir) throws SQLException {
        NBComponent root = new NBBaseComponent(null);
        Path db = tempDir.resolve("coarse_histo.db");
        String url = "jdbc:sqlite:" + db;

        MetricsSnapshotScheduler.register(root, 100L, view -> {
        });
        SqliteSnapshotReporter reporter = new SqliteSnapshotReporter(
            root,
            url,
            200L,
            new MetricInstanceFilter(),
            true
        );
        MetricsSnapshotScheduler scheduler = MetricsSnapshotScheduler.lookup(root);

        try {
            scheduler.injectSnapshotForTesting(timerView(100L, 12L, 18L, 25L));
            scheduler.injectSnapshotForTesting(timerView(100L, 4L, 9L, 33L));
        } finally {
            reporter.close();
            if (scheduler != null) {
                scheduler.teardown();
            }
        }

        try (Connection connection = DriverManager.getConnection(url)) {
            assertThat(readHistogramBase64(connection)).isNotEmpty();
        }
    }

    private void emitSnapshots(MetricsSnapshotScheduler scheduler, long... values) {
        for (long value : values) {
            scheduler.injectSnapshotForTesting(counterView(value, 100L));
        }
    }

    private MetricsView counterView(long value, long interval) {
        NBMetricCounter counter = new NBMetricCounter(counterLabels(), "counter", "operations", MetricCategory.Core);
        counter.inc(value);
        return MetricsView.capture(List.of(counter), interval);
    }

    private NBLabels counterLabels() {
        return NBLabels.forKV("name", "counter_metric", "scenario", "scenario", "activity", "activity");
    }

    private double extractPromValue(String exposition, String metricName) {
        Pattern pattern = Pattern.compile(metricName + "\\{[^}]*} ([^ ]+)");
        Matcher matcher = pattern.matcher(exposition);
        if (!matcher.find()) {
            throw new AssertionError("Metric " + metricName + " not found in exposition:\n" + exposition);
        }
        return Double.parseDouble(matcher.group(1));
    }

    private double parseCsvValue(String line) {
        String[] parts = line.split(",");
        if (parts.length < 2) {
            throw new AssertionError("Unexpected CSV line: " + line);
        }
        return Double.parseDouble(parts[1]);
    }

    private List<Double> readSqliteValues(Connection connection) throws SQLException {
        List<Double> values = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("""
            SELECT sv.value
            FROM sample_value sv
            INNER JOIN metric_instance mi ON mi.id = sv.metric_instance_id
            INNER JOIN sample_name sn ON sn.id = mi.sample_name_id
            WHERE sn.sample = ?
            ORDER BY sv.timestamp_ms
        """)) {
            ps.setString(1, "counter_metric_total");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    values.add(rs.getDouble("value"));
                }
            }
        }
        return values;
    }

    private List<String> readHistogramBase64(Connection connection) throws SQLException {
        List<String> encoded = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("""
            SELECT histogram_base64
            FROM sample_histogram
            ORDER BY sample_value_id
        """)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    encoded.add(rs.getString(1));
                }
            }
        }
        return encoded;
    }

    private void emitTimerSnapshot(MetricsSnapshotScheduler scheduler, long intervalMillis, long... durationsMillis) {
        scheduler.injectSnapshotForTesting(timerView(intervalMillis, durationsMillis));
    }

    private MetricsView timerView(long intervalMillis, long... durationsMillis) {
        NBLabels labels = timerLabels();
        NBMetricTimer timer = new NBMetricTimer(
            labels,
            new DeltaHdrHistogramReservoir(labels, 3),
            "timer",
            "nanoseconds",
            MetricCategory.Core
        );
        for (long duration : durationsMillis) {
            timer.update(duration, TimeUnit.MILLISECONDS);
        }
        return MetricsView.capture(List.of(timer), intervalMillis);
    }

    private NBLabels timerLabels() {
        return NBLabels.forKV("name", "timer_metric", "scenario", "scenario", "activity", "activity");
    }

    private static final class RecordingPromReporter extends MetricsSnapshotReporterBase {
        private final List<String> exposures = new ArrayList<>();

        private RecordingPromReporter(NBComponent parent, long intervalMillis) {
            super(parent, NBLabels.forKV("reporter", "prom", "interval", Long.toString(intervalMillis)), intervalMillis);
        }

        @Override
        public void onMetricsSnapshot(MetricsView view) {
            Clock clock = Clock.fixed(view.capturedAt(), ZoneId.of("UTC"));
            exposures.add(PromExpositionFormat.format(clock, view));
        }

        public List<String> exposures() {
            return exposures;
        }
    }
}
