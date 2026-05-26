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

import java.util.Map;

/// Final cumulative state of one session, normalised into the shape the c-stress-style
/// renderer expects. One [OpSummary] per `op` label value; the renderer synthesises a
/// `total` row by aggregating across them.
///
/// @param sessionLabel        the `session` label value from the metric source
/// @param runtimeMs           wall-clock duration of the session, from the `session_time` gauge
/// @param rateLimited         true if `cycles_responsetime` instances were present in the source —
///                            i.e. a cycle rate limiter was active. Drives the latency-source
///                            auto-switch (servicetime when free-rate, responsetime when limited)
///                            and the corresponding callout in the rendered report.
/// @param latencyInstrument   short identifier of the timer instrument used for the latencies
///                            in [OpSummary] (e.g. `cycles_servicetime`, `cycles_responsetime`)
/// @param maxAvailable        true if the source could supply `maxMs`; when false the renderer
///                            shows `n/a` and prints a callout about histograms being disabled
/// @param byOp                per-`op` cumulative summary, keyed by op label value
public record SessionSummary(
    String sessionLabel,
    long runtimeMs,
    boolean rateLimited,
    String latencyInstrument,
    boolean maxAvailable,
    Map<String, OpSummary> byOp
) {
    public SessionSummary {
        byOp = Map.copyOf(byOp);
    }
}
