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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.nosqlbench.nb.api.components.core.NBBaseComponent;
import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.api.engine.metrics.DeltaHdrHistogramReservoir;
import io.nosqlbench.nb.api.engine.metrics.instruments.MetricCategory;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricCounter;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricTimer;
import io.nosqlbench.nb.api.engine.metrics.view.MetricsView;
import io.nosqlbench.nb.api.labels.NBLabels;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/// Pins the append-only "only-on-change" semantics of [CsvMetricsFilesManifest] and its
/// integration with [CsvReporter]. The manifest is the canonical map from fully-qualified labels
/// to the per-metric CSV file that holds those samples; downstream automation depends on it
/// rather than parsing filenames.
@Tag("unit")
public class CsvMetricsFilesManifestTest {

    private NBMetricTimer makeTimer(NBLabels labels) {
        return new NBMetricTimer(labels, new DeltaHdrHistogramReservoir(labels, 3), "test", "ns", MetricCategory.Core);
    }

    private NBMetricCounter makeCounter(NBLabels labels) {
        return new NBMetricCounter(labels, "test", "ops", MetricCategory.Core);
    }

    /// Writing one snapshot containing two distinct timer label sets should produce a manifest
    /// with one line per distinct label set, each carrying fully-qualified labels and the
    /// matching CSV filename.
    @Test
    public void writesOneLinePerDistinctLabelSet(@TempDir Path tempDir) throws Exception {
        NBComponent root = new NBBaseComponent(null);
        try {
            Path outDir = tempDir.resolve("csv");
            CsvReporter reporter = new CsvReporter(root, outDir, 60_000L, new MetricInstanceFilter());

            NBLabels readLabels = NBLabels.forKV(
                "scenario", "default", "activity", "cql",
                "op", "read", "name", "cycles_servicetime"
            );
            NBLabels writeLabels = NBLabels.forKV(
                "scenario", "default", "activity", "cql",
                "op", "write", "name", "cycles_servicetime"
            );
            NBMetricTimer readTimer = makeTimer(readLabels);
            NBMetricTimer writeTimer = makeTimer(writeLabels);
            readTimer.update(100, java.util.concurrent.TimeUnit.MILLISECONDS);
            writeTimer.update(200, java.util.concurrent.TimeUnit.MILLISECONDS);

            MetricsView view = MetricsView.capture(List.of(readTimer, writeTimer), 1_000L);
            reporter.onMetricsSnapshot(view);

            Path manifest = outDir.resolve(CsvMetricsFilesManifest.FILE_NAME);
            assertThat(manifest).exists();
            List<String> lines = Files.readAllLines(manifest);
            assertThat(lines).hasSize(2);

            for (String line : lines) {
                JsonObject obj = JsonParser.parseString(line).getAsJsonObject();
                assertThat(obj.has("metric")).isTrue();
                assertThat(obj.get("metric").getAsString()).isEqualTo("cycles_servicetime");
                assertThat(obj.has("labels")).isTrue();
                JsonObject labels = obj.getAsJsonObject("labels");
                assertThat(labels.has("name")).isTrue();
                assertThat(labels.has("op")).isTrue();
                assertThat(labels.has("activity")).isTrue();
                assertThat(obj.has("file")).isTrue();
                assertThat(obj.get("file").getAsString()).endsWith(".csv");
                assertThat(obj.has("first_seen_ms")).isTrue();
                assertThat(obj.has("type")).isTrue();
                assertThat(obj.get("type").getAsString()).isEqualTo("timer");
                String op = labels.get("op").getAsString();
                assertThat(obj.get("file").getAsString()).contains("op_" + op);
            }
        } finally {
            root.close();
        }
    }

    /// Re-emitting a snapshot with the same label sets must NOT append duplicate lines — the
    /// manifest stays at the same size when nothing has changed.
    @Test
    public void doesNotAppendOnUnchangedSnapshot(@TempDir Path tempDir) throws Exception {
        NBComponent root = new NBBaseComponent(null);
        try {
            Path outDir = tempDir.resolve("csv");
            CsvReporter reporter = new CsvReporter(root, outDir, 60_000L, new MetricInstanceFilter());

            NBLabels labels = NBLabels.forKV(
                "scenario", "default", "activity", "cql",
                "op", "read", "name", "cycles_servicetime"
            );
            NBMetricTimer timer = makeTimer(labels);
            timer.update(100, java.util.concurrent.TimeUnit.MILLISECONDS);

            reporter.onMetricsSnapshot(MetricsView.capture(List.of(timer), 1_000L));
            reporter.onMetricsSnapshot(MetricsView.capture(List.of(timer), 1_000L));
            reporter.onMetricsSnapshot(MetricsView.capture(List.of(timer), 1_000L));

            Path manifest = outDir.resolve(CsvMetricsFilesManifest.FILE_NAME);
            assertThat(Files.readAllLines(manifest)).hasSize(1);
        } finally {
            root.close();
        }
    }

    /// When a new label set appears in a later snapshot, the manifest appends a line for it.
    /// The CsvReporter derives filenames from the *diff* between each sample's labels and the
    /// labels common across all current samples — so adding a new sample that introduces a
    /// previously-common label as a discriminator changes the filename of pre-existing label
    /// sets too. The manifest correctly captures that change rather than papering over it: the
    /// last line for any given label set is always the file that currently holds its samples.
    @Test
    public void appendsOnNewLabelSetAndOnFilenameShift(@TempDir Path tempDir) throws Exception {
        NBComponent root = new NBBaseComponent(null);
        try {
            Path outDir = tempDir.resolve("csv");
            CsvReporter reporter = new CsvReporter(root, outDir, 60_000L, new MetricInstanceFilter());

            NBLabels readLabels = NBLabels.forKV(
                "scenario", "default", "activity", "cql",
                "op", "read", "name", "cycles_servicetime"
            );
            NBLabels writeLabels = NBLabels.forKV(
                "scenario", "default", "activity", "cql",
                "op", "write", "name", "cycles_servicetime"
            );
            NBMetricTimer readTimer = makeTimer(readLabels);
            NBMetricTimer writeTimer = makeTimer(writeLabels);
            readTimer.update(50, java.util.concurrent.TimeUnit.MILLISECONDS);
            writeTimer.update(75, java.util.concurrent.TimeUnit.MILLISECONDS);

            // First snapshot has only the read timer. With a single sample present, every
            // label collapses into "common" → the diff is empty → the file name carries no
            // discriminator. One manifest line is emitted for this initial state.
            reporter.onMetricsSnapshot(MetricsView.capture(List.of(readTimer), 1_000L));
            Path manifest = outDir.resolve(CsvMetricsFilesManifest.FILE_NAME);
            assertThat(Files.readAllLines(manifest)).hasSize(1);

            // Second snapshot adds the write timer. Now `op` varies and is dropped from the
            // common set, so each timer's file name picks up the `op` discriminator. Three
            // appends total: the original read pairing, the new read pairing under the new
            // file name, and the new write pairing.
            reporter.onMetricsSnapshot(MetricsView.capture(List.of(readTimer, writeTimer), 1_000L));
            List<String> after = Files.readAllLines(manifest);
            assertThat(after).hasSize(3);

            // Folding to "last entry wins" yields exactly two effective mappings — one per op.
            List<CsvMetricsFilesManifest.Entry> entries = CsvMetricsFilesManifest.readAll(manifest);
            Map<String, CsvMetricsFilesManifest.Entry> folded = CsvMetricsFilesManifest.foldByLabelKey(entries);
            assertThat(folded).hasSize(2);
            for (CsvMetricsFilesManifest.Entry entry : folded.values()) {
                String op = entry.labels().get("op");
                assertThat(entry.file()).contains("op_" + op);
            }
        } finally {
            root.close();
        }
    }

    /// `type` discriminator should reflect the underlying sample shape. A counter (point/gauge
    /// sample) gets type=gauge; a timer gets type=timer.
    @Test
    public void typeDiscriminatorMatchesSampleShape(@TempDir Path tempDir) throws Exception {
        NBComponent root = new NBBaseComponent(null);
        try {
            Path outDir = tempDir.resolve("csv");
            CsvReporter reporter = new CsvReporter(root, outDir, 60_000L, new MetricInstanceFilter());

            NBLabels counterLabels = NBLabels.forKV(
                "scenario", "default", "activity", "cql",
                "name", "pending_ops"
            );
            NBLabels timerLabels = NBLabels.forKV(
                "scenario", "default", "activity", "cql",
                "op", "read", "name", "cycles_servicetime"
            );
            NBMetricCounter counter = makeCounter(counterLabels);
            counter.inc(5);
            NBMetricTimer timer = makeTimer(timerLabels);
            timer.update(10, java.util.concurrent.TimeUnit.MILLISECONDS);

            reporter.onMetricsSnapshot(MetricsView.capture(List.of(counter, timer), 1_000L));

            List<CsvMetricsFilesManifest.Entry> entries = CsvMetricsFilesManifest.readAll(
                outDir.resolve(CsvMetricsFilesManifest.FILE_NAME)
            );
            assertThat(entries).hasSize(2);
            assertThat(entries.stream().map(CsvMetricsFilesManifest.Entry::type))
                .containsExactlyInAnyOrder("gauge", "timer");
        } finally {
            root.close();
        }
    }

    /// The reader folds the JSONL stream to a single entry per fully-qualified label set, last
    /// occurrence wins. Used by downstream automation (e.g. CsvStressReportSource).
    @Test
    public void readAllAndFoldByLabelKey(@TempDir Path tempDir) throws Exception {
        NBComponent root = new NBBaseComponent(null);
        try {
            Path outDir = tempDir.resolve("csv");
            CsvReporter reporter = new CsvReporter(root, outDir, 60_000L, new MetricInstanceFilter());

            NBLabels labels = NBLabels.forKV(
                "scenario", "default", "activity", "cql",
                "op", "read", "name", "cycles_servicetime"
            );
            NBMetricTimer timer = makeTimer(labels);
            timer.update(10, java.util.concurrent.TimeUnit.MILLISECONDS);
            reporter.onMetricsSnapshot(MetricsView.capture(List.of(timer), 1_000L));

            List<CsvMetricsFilesManifest.Entry> entries = CsvMetricsFilesManifest.readAll(
                outDir.resolve(CsvMetricsFilesManifest.FILE_NAME)
            );
            Map<String, CsvMetricsFilesManifest.Entry> folded = CsvMetricsFilesManifest.foldByLabelKey(entries);
            assertThat(folded).hasSize(1);
            CsvMetricsFilesManifest.Entry only = folded.values().iterator().next();
            assertThat(only.metric()).isEqualTo("cycles_servicetime");
            assertThat(only.labels()).containsEntry("op", "read")
                .containsEntry("name", "cycles_servicetime");
        } finally {
            root.close();
        }
    }
}
