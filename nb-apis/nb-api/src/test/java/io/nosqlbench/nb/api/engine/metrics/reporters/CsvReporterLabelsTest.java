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

import io.nosqlbench.nb.api.components.core.NBBaseComponent;
import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.api.labels.NBLabels;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/// Pins the invariant that attaching a [CsvReporter] under a parent which already carries the
/// standard session-identifying labels (jobname/instance/node/session) does not produce a
/// parent/child label overlap. Regression cover for the bug that surfaced when NBCLI passed
/// its own foundational labels as `extraLabels` to the reporter at construction time.
@Tag("unit")
public class CsvReporterLabelsTest {

    /// When a CsvReporter is attached under a session-like parent and given no extra labels,
    /// resolving its labels (which the component tree does as soon as it is attached, e.g.
    /// for debug logging) must not throw the MapLabels overlap error.
    @Test
    public void csvReporterUnderSessionLabelsResolvesWithoutOverlap(@TempDir Path tempDir) {
        NBBaseComponent root = new NBBaseComponent(null, NBLabels.forKV(
            "jobname", "nosqlbench",
            "instance", "default",
            "node", "10.0.0.1"
        ));
        try {
            NBComponent session = new NBBaseComponent(root, NBLabels.forKV("session", "testSession"));

            Path outDir = tempDir.resolve("csv");
            CsvReporter reporter = new CsvReporter(session, outDir, 60_000L, new MetricInstanceFilter());

            assertThatCode(reporter::getLabels).doesNotThrowAnyException();

            NBLabels resolved = reporter.getLabels();
            assertThat(resolved.asMap())
                .containsEntry("jobname", "nosqlbench")
                .containsEntry("instance", "default")
                .containsEntry("node", "10.0.0.1")
                .containsEntry("session", "testSession");
        } finally {
            root.close();
        }
    }

    /// Sanity check that a CsvReporter with a distinguishing extra label (e.g. `_type=csv`)
    /// still resolves cleanly — the legal way to add per-reporter dimensions when needed.
    @Test
    public void csvReporterAcceptsDistinguishingExtraLabels(@TempDir Path tempDir) {
        NBBaseComponent root = new NBBaseComponent(null, NBLabels.forKV(
            "jobname", "nosqlbench",
            "instance", "default",
            "node", "10.0.0.1"
        ));
        try {
            NBComponent session = new NBBaseComponent(root, NBLabels.forKV("session", "testSession"));

            Path outDir = tempDir.resolve("csv2");
            CsvReporter reporter = new CsvReporter(
                session, outDir, 60_000L, new MetricInstanceFilter(),
                NBLabels.forKV("_type", "csv")
            );

            assertThat(reporter.getLabels().asMap())
                .containsEntry("_type", "csv")
                .containsEntry("session", "testSession");
        } finally {
            root.close();
        }
    }

    /// Direct guard for the historical bug: when a child component is constructed with extra
    /// labels that overlap its parent's, resolving the child's effective labels must fail loudly
    /// rather than silently producing duplicate keys. The resolution happens lazily on the first
    /// call to `getLabels()` (e.g. for debug logging or metric naming), which is exactly how the
    /// production crash surfaced — see the matching stack at MapLabels#and via
    /// NBBaseComponent#attachChild's debug-log lambda.
    @Test
    public void overlappingExtraLabelsAreRejectedOnResolution(@TempDir Path tempDir) {
        NBBaseComponent root = new NBBaseComponent(null, NBLabels.forKV(
            "jobname", "nosqlbench",
            "instance", "default",
            "node", "10.0.0.1"
        ));
        NBComponent session = new NBBaseComponent(root, NBLabels.forKV("session", "testSession"));

        Path outDir = tempDir.resolve("csv3");
        CsvReporter reporter = new CsvReporter(
            session, outDir, 60_000L, new MetricInstanceFilter(),
            NBLabels.forKV("jobname", "nosqlbench")
        );

        try {
            assertThatCode(reporter::getLabels)
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("overlap label keys");
        } finally {
            // detach the deliberately-broken reporter so root teardown doesn't re-trigger the overlap
            session.detachChild(reporter);
            root.close();
        }
    }
}
