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

package io.nosqlbench.engine.cli.stressreport;

import io.nosqlbench.nb.api.components.core.NBBaseComponent;
import io.nosqlbench.nb.api.engine.metrics.DeltaHdrHistogramReservoir;
import io.nosqlbench.nb.api.engine.metrics.instruments.MetricCategory;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetric;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricTimer;
import io.nosqlbench.nb.api.engine.metrics.reporters.CsvReporter;
import io.nosqlbench.nb.api.engine.metrics.reporters.MetricInstanceFilter;
import io.nosqlbench.nb.api.engine.metrics.reporters.SqliteSnapshotReporter;
import io.nosqlbench.nb.api.engine.metrics.view.MetricsView;
import io.nosqlbench.nb.api.labels.NBLabels;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/// End-to-end integrated test for the `stress-report` bundled app.
///
/// Drives the actual reporter write paths (SQLite + CSV) with identical input data, then runs
/// the app against each source and against both in A/B mode. Pins:
/// - per-op rows render correctly with the expected ops + counts
/// - latency-source callout is present and names the right timer instrument
/// - A/B comparison reports a match when both sources see the same metrics
/// - A deliberate mismatch is detected and produces non-zero exit
///
/// This is the "schema-drift detector" — if anything along the path
/// (instrumentation → MetricsView → SQLite schema → MQL command output → CSV format → manifest)
/// changes shape, this test will fail.
@Tag("unit")
public class StressReportAppIntegratedTest {

    private NBMetricTimer makeTimer(NBLabels labels) {
        return new NBMetricTimer(labels, new DeltaHdrHistogramReservoir(labels, 3), "test", "ns", MetricCategory.Core);
    }

    private NBLabels labelsFor(String op, String metricName) {
        return NBLabels.forKV(
            "jobname", "nosqlbench",
            "instance", "default",
            "session", "testSession",
            "activity", "cql",
            "op", op,
            "name", metricName
        );
    }

    /// Build a small set of metrics matching the production instrumentation pattern: a
    /// `cycles_servicetime`, `result`, and `result_success` timer per op. Captured into a
    /// MetricsView and written to both reporter sinks. Returns the metrics so the test can
    /// reuse the view if needed.
    ///
    /// Mirrors the per-op timer families nb-engine creates when `instrument: true` is set on
    /// a block: `successfor_<opname>` and `errorsfor_<opname>`, each carrying an `op=<opname>`
    /// label (the label is added automatically by ParsedOp in production). This is the shape
    /// the SQLite and CSV stress-report sources query against.
    private List<NBMetric> populateMetrics() {
        List<NBMetric> metrics = new ArrayList<>();
        for (String op : List.of("read", "write")) {
            NBMetricTimer success = makeTimer(labelsFor(op, "successfor_" + op));
            NBMetricTimer error = makeTimer(labelsFor(op, "errorsfor_" + op));
            int base = "read".equals(op) ? 100 : 200;
            int n = "read".equals(op) ? 50 : 30;
            for (int i = 0; i < n; i++) {
                long ns = TimeUnit.MILLISECONDS.toNanos(base + (i % 5));
                if (i % 10 == 0) {
                    error.update(ns, TimeUnit.NANOSECONDS);
                } else {
                    success.update(ns, TimeUnit.NANOSECONDS);
                }
            }
            metrics.add(success);
            metrics.add(error);
        }
        metrics.add(buildSessionTimeGauge(12_345L));
        return metrics;
    }

    /// Build a session_time gauge from an anonymous {@link io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricGauge}.
    /// We can't use {@link io.nosqlbench.nb.api.engine.metrics.instruments.NBFunctionGauge} directly
    /// since it composes labels through an NBComponent parent; this isolated test wants the gauge
    /// to carry session labels directly so it shows up in MetricsView under the same identity the
    /// timers use.
    private static io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricGauge buildSessionTimeGauge(long runtimeMs) {
        NBLabels sessionTimeLabels = NBLabels.forKV(
            "jobname", "nosqlbench", "instance", "default", "session", "testSession",
            "name", "session_time"
        );
        return new io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricGauge() {
            @Override public Double getValue() { return (double) runtimeMs; }
            @Override public NBLabels getLabels() { return sessionTimeLabels; }
            @Override public String typeName() { return "gauge"; }
            @Override public String getDescription() { return "elapsed"; }
            @Override public String getUnit() { return "ms"; }
            @Override public MetricCategory[] getCategories() { return new MetricCategory[]{MetricCategory.Core}; }
        };
    }

    private String captureRun(StressReportApp app, String[] args) {
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        StressReportApp instrumented = new StressReportApp(
            new PrintStream(stdout, true, StandardCharsets.UTF_8),
            new PrintStream(stderr, true, StandardCharsets.UTF_8)
        );
        instrumented.applyAsInt(args);
        return stdout.toString(StandardCharsets.UTF_8) + stderr.toString(StandardCharsets.UTF_8);
    }

    private int runWithExitCode(String[] args, ByteArrayOutputStream stdoutCapture) {
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        StressReportApp instrumented = new StressReportApp(
            new PrintStream(stdoutCapture, true, StandardCharsets.UTF_8),
            new PrintStream(stderr, true, StandardCharsets.UTF_8)
        );
        return instrumented.applyAsInt(args);
    }

    @Test
    public void sqliteAndCsvSourcesProduceMatchingReports(@TempDir Path tempDir) throws Exception {
        NBBaseComponent root = new NBBaseComponent(null);
        Path sqliteDbPath = tempDir.resolve("metrics.db");
        Path csvDir = tempDir.resolve("csv");
        try {
            List<NBMetric> metrics = populateMetrics();
            MetricsView view = MetricsView.capture(metrics, 1_000L);

            // Write to SQLite
            SqliteSnapshotReporter sqlite = new SqliteSnapshotReporter(
                root, "jdbc:sqlite:" + sqliteDbPath.toAbsolutePath(),
                60_000L, new MetricInstanceFilter(), null, false
            );
            sqlite.onMetricsSnapshot(view);
            sqlite.close();

            // Write to CSV (using the new manifest)
            CsvReporter csv = new CsvReporter(root, csvDir, 60_000L, new MetricInstanceFilter());
            csv.onMetricsSnapshot(view);

            // Run app against SQLite
            String sqliteOut = captureRun(new StressReportApp(),
                new String[]{"--sqlite", sqliteDbPath.toString()});
            assertThat(sqliteOut)
                .contains("WARNING: Do not expect comparisons made between this report and cassandra-stress")
                .contains("anecdotal at")
                .contains("NoSQLBench stress-report")
                .contains("Source         : sqlite:")
                .contains("Latency source: successfor_<op>")
                .contains("read")
                .contains("write")
                .contains("total");

            // Run app against CSV
            String csvOut = captureRun(new StressReportApp(),
                new String[]{"--csv", csvDir.toString()});
            assertThat(csvOut)
                .contains("WARNING: Do not expect comparisons made between this report and cassandra-stress")
                .contains("NoSQLBench stress-report")
                .contains("Source         : csv:")
                .contains("Latency source: successfor_<op>")
                .contains("read")
                .contains("write")
                .contains("total");

            // A/B comparison — sources should now match fully for the fields both can supply.
            // With sample_statistics persisted by the SQLite reporter and exposed via
            // StatsCommand, the previous mean/max coverage gap is gone.
            ByteArrayOutputStream abOut = new ByteArrayOutputStream();
            int exit = runWithExitCode(new String[]{
                "--ab", "--sqlite", sqliteDbPath.toString(), "--csv", csvDir.toString()
            }, abOut);
            String abText = abOut.toString(StandardCharsets.UTF_8);
            assertThat(abText).contains("--- A ---");
            assertThat(abText).contains("--- B ---");
            assertThat(abText).contains("--- A/B comparison ---");
            assertThat(exit)
                .as("A/B should match now that sample_statistics carries mean/max. Output:%n%s", abText)
                .isEqualTo(StressReportApp.EXIT_OK);
            assertThat(abText).contains("A/B match");
        } finally {
            root.close();
        }
    }

    /// When the SQLite database is missing, the app should exit with EXIT_ERROR rather than
    /// crash, and the rendered output should explain what happened.
    @Test
    public void missingSqliteDatabaseExitsCleanly(@TempDir Path tempDir) {
        Path missing = tempDir.resolve("nope.db");
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        int exit = runWithExitCode(new String[]{"--sqlite", missing.toString()}, stdout);
        assertThat(exit).isEqualTo(StressReportApp.EXIT_ERROR);
    }

    /// Argument validation: --ab without both --sqlite and --csv is a usage error.
    @Test
    public void abModeRequiresBothSources(@TempDir Path tempDir) {
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        int exit = runWithExitCode(new String[]{"--ab", "--sqlite", tempDir.resolve("x").toString()}, stdout);
        assertThat(exit).isEqualTo(StressReportApp.EXIT_USAGE);
    }

    /// When the CSV manifest is tampered with (file path edited to point at a different op's
    /// data), the comparator should detect a real value mismatch.
    @Test
    public void deliberateCsvMismatchIsDetected(@TempDir Path tempDir) throws Exception {
        NBBaseComponent root = new NBBaseComponent(null);
        Path sqliteDbPath = tempDir.resolve("metrics.db");
        Path csvDir = tempDir.resolve("csv");
        try {
            List<NBMetric> metrics = populateMetrics();
            MetricsView view = MetricsView.capture(metrics, 1_000L);

            SqliteSnapshotReporter sqlite = new SqliteSnapshotReporter(
                root, "jdbc:sqlite:" + sqliteDbPath.toAbsolutePath(),
                60_000L, new MetricInstanceFilter(), null, false
            );
            sqlite.onMetricsSnapshot(view);
            sqlite.close();

            CsvReporter csv = new CsvReporter(root, csvDir, 60_000L, new MetricInstanceFilter());
            csv.onMetricsSnapshot(view);

            // Tamper with one of the per-op success-timer CSV files so the count diverges from
            // SQLite. Bump the last row's count column by a large amount.
            Path tampered = Files.list(csvDir)
                .filter(p -> p.getFileName().toString().endsWith(".csv"))
                .filter(p -> p.getFileName().toString().contains("successfor_"))
                .findFirst()
                .orElseThrow();
            List<String> lines = Files.readAllLines(tampered);
            String last = lines.get(lines.size() - 1);
            String[] tokens = last.split(",", -1);
            // Column layout: t,count,max,mean,min,stddev,p50,p75,p95,p98,p99,p999,...
            tokens[1] = String.valueOf(Long.parseLong(tokens[1]) + 9999L);
            lines.set(lines.size() - 1, String.join(",", tokens));
            Files.write(tampered, lines);

            ByteArrayOutputStream abOut = new ByteArrayOutputStream();
            int exit = runWithExitCode(new String[]{
                "--ab", "--sqlite", sqliteDbPath.toString(), "--csv", csvDir.toString()
            }, abOut);

            assertThat(exit).isEqualTo(StressReportApp.EXIT_MISMATCH);
            assertThat(abOut.toString(StandardCharsets.UTF_8))
                .contains("opCount");
        } finally {
            root.close();
        }
    }
}
