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

/// Final cumulative summary for one `op` label value (or the synthesized `total`).
///
/// Latency fields are in milliseconds. `maxMs` is [Double.NaN] when the source cannot supply
/// it (e.g. SQLite without `--sqlite-histograms`); the renderer surfaces this as an `n/a` cell
/// rather than a zero, so the gap is honestly visible in the report.
public record OpSummary(
    String op,
    long opCount,
    long errorCount,
    double meanMs,
    double medianMs,
    double p95Ms,
    double p99Ms,
    double p999Ms,
    double maxMs
) {
}
