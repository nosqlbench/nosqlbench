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

/// Reads a NoSQLBench session's final metric state and returns it as a [SessionSummary].
///
/// Two implementations exist: one over the session SQLite database written by
/// [io.nosqlbench.nb.api.engine.metrics.reporters.SqliteSnapshotReporter] and one over the
/// per-metric CSV files written by [io.nosqlbench.nb.api.engine.metrics.reporters.CsvReporter].
/// Both must produce equivalent [SessionSummary] values for the same session, which is what
/// the A/B mode of the stress-report app verifies.
public interface StressReportSource {

    /// Human-readable description of where this source reads from. Used in the rendered output
    /// header and in A/B mismatch diagnostics.
    String sourceDescription();

    /// Read the session's final state. Implementations should pick the latest snapshot
    /// available in the source and the latency instrument appropriate for the run
    /// (servicetime when no cycle rate limiter is detected, responsetime when one is).
    SessionSummary readSummary() throws Exception;
}
