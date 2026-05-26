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

package io.nosqlbench.engine.core.lifecycle.session;

import io.nosqlbench.nb.api.components.core.NBBaseComponent;
import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.api.engine.metrics.reporters.CsvReporter;
import io.nosqlbench.nb.api.engine.metrics.reporters.MetricInstanceFilter;
import io.nosqlbench.nb.api.engine.metrics.reporters.SqliteSnapshotReporter;
import io.nosqlbench.nb.api.labels.NBLabeledElement;
import io.nosqlbench.nb.api.labels.NBLabels;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/// Integrated coverage of the multi-reporter configuration path on an NBSession.
///
/// The user-facing crash this test pins came from `--report-csv-to` being configured alongside
/// the session's default SQLite reporter and other concurrent reporter outputs. Each reporter
/// is constructed as a child of the session; the session root already carries the standard
/// identifying labels (`jobname`, `instance`, `node`, `session`). Any reporter that re-introduces
/// those keys as its own extra labels causes [io.nosqlbench.nb.api.labels.MapLabels#and] to
/// throw on label resolution. This test wires up the same shape and verifies labels resolve
/// cleanly for every attached reporter.
@Tag("unit")
public class NBSessionReportersTest {

    /// Mirrors what NBCLI hands to the NBSession constructor: a root NBBaseComponent seeded
    /// with the foundational identity labels (jobname/instance/node).
    private static NBLabeledElement foundationalLabels() {
        NBLabels labels = NBLabels.forKV(
            "jobname", "nosqlbench",
            "instance", "default",
            "node", "10.0.0.1"
        );
        return () -> labels;
    }

    @Test
    public void multipleReportersAttachToSessionWithoutLabelOverlap(@TempDir Path tempDir) {
        Path logsDir = tempDir.resolve("logs");
        Map<String, String> props = Map.of(
            "logsdir", logsDir.toString(),
            "metrics.sqlite.histograms", "false"
        );

        try (NBSession session = new NBSession(foundationalLabels(), "testSession", props)) {

            // Mirror NBCLI.applyDirect's CSV reporter wiring (post-fix: no extra labels).
            Path csvDir = tempDir.resolve("csv");
            CsvReporter csv = new CsvReporter(session, csvDir, 60_000L, new MetricInstanceFilter());

            // Mirror NBCLI.applyDirect's --report-sqlite-to wiring: a second SQLite reporter,
            // independent of the session's default one, with a distinguishing JDBC URL.
            Path extraDbPath = tempDir.resolve("extra.db");
            SqliteSnapshotReporter extraSqlite = session.create().sqliteSnapshotReporter(
                session,
                "jdbc:sqlite:" + extraDbPath.toAbsolutePath(),
                60_000L,
                new MetricInstanceFilter(),
                false
            );

            // Every attached reporter's effective labels must resolve without overlapping the
            // session root labels — this is the exact merge that blows up when extraLabels
            // duplicate parent keys.
            assertThatCode(csv::getLabels).doesNotThrowAnyException();
            assertThatCode(extraSqlite::getLabels).doesNotThrowAnyException();
            assertThatCode(session::getLabels).doesNotThrowAnyException();

            // The session root retains sole ownership of the foundational identity keys.
            assertThat(session.getLabels().asMap())
                .containsEntry("jobname", "nosqlbench")
                .containsEntry("instance", "default")
                .containsEntry("node", "10.0.0.1")
                .containsEntry("session", "testSession");

            // Each reporter inherits those labels through the parent chain; the reporter itself
            // adds no overlapping keys.
            assertThat(csv.getLabels().asMap())
                .containsEntry("jobname", "nosqlbench")
                .containsEntry("session", "testSession");
        }
    }

    /// Touching the debug-log path on attachChild was the original site that surfaced the
    /// production crash. This test forces the same lambda to fire by triggering a debug-level
    /// log message and confirms it no longer raises.
    @Test
    public void attachChildDebugLogDoesNotThrowUnderMultiReporterConfig(@TempDir Path tempDir) {
        Path logsDir = tempDir.resolve("logs");
        Map<String, String> props = Map.of(
            "logsdir", logsDir.toString(),
            "metrics.sqlite.histograms", "false"
        );

        try (NBSession session = new NBSession(foundationalLabels(), "anotherSession", props)) {
            CsvReporter csv = new CsvReporter(
                session, tempDir.resolve("csv2"), 60_000L, new MetricInstanceFilter()
            );

            // Forcing description() exercises the same getLabels() resolution that the
            // attachChild debug-log lambda exercises in production.
            assertThatCode(() -> csv.description()).doesNotThrowAnyException();

            // Attaching a further child of the reporter also exercises the same path.
            assertThatCode(() ->
                new NBBaseComponent((NBComponent) csv, NBLabels.forKV("_type", "leaf"))
            ).doesNotThrowAnyException();
        }
    }
}
