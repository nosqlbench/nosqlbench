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

package io.nosqlbench.adapters.api.activityimpl;

import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.CycleOp;
import io.nosqlbench.adapters.api.evalctx.CycleFunction;
import io.nosqlbench.adapters.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.adapters.api.templating.ParsedOp;

import java.util.Map;
import java.util.function.LongFunction;

/// ## Role
///
/// An OpDispenser converts a cycle number into an executable op. It sits between
/// op mapping (interpreting templates) and execution (running ops) and remains
/// immutable and fast on hot paths.
///
/// ```text
/// Activity
/// └─ Stride (size S)
///    └─ Cycle n (selects stride slot and space)
///       ├─ Space resolved per cycle (name→index)
///       ├─ OpTemplate → ParsedOp → OpMapper (one per template)
///       ├─ OpDispenser (one per ParsedOp; reused across cycles)
///       └─ CycleOp executable (one per cycle invocation)
/// ```
///
/// Cardinal relationships
/// - One activity owns many strides; each stride walks many cycles.
/// - Every cycle resolves at most one space for its ops; resolution happens per cycle, not cached per stride.
/// - One op template → one mapper → one dispenser → many CycleOp instances over time.
///
/// ## BaseOpDispenser
///
/// Shared behaviors (labels, verifier wiring, optional space naming, metrics) live in
/// [BaseOpDispenser]. Adapter dispensers should extend it to keep synthesis concerns
/// consistent and leave per-cycle state in execution contexts.
///
/// ## Implementation notes
/// - Provide a deterministic `cycle -> op` function; avoid storing mutable execution state.
/// - Keep construction lightweight: pre-bind lambdas, avoid reflection, and prefer ordinal/handle lookups.
/// - Dispensers may expose space naming functions for diagnostics but must not retain
///   per-cycle space objects; callers resolve per cycle and cache locally if needed.
/// @param <OPTYPE> The executable op type produced by this dispenser (implements [CycleOp]).

public interface OpDispenser<OPTYPE extends CycleOp<?>> extends LongFunction<OPTYPE>, OpResultTracker {

    /// This method should do all the work of
    /// creating an operation that is executable by some other caller.
    /// The value produced by the apply method should not require
    /// additional processing if a caller wants to execute the operation
    /// multiple times, as for retries.
    /// @param cycle The cycle number which seeds any generated fields for binding.
    /// @return an executable operation
    OPTYPE getOp(long cycle);

    CycleFunction<Boolean> getVerifier();

    String getOpName();

    /// Get the error context for a specific cycle value. Implementations should include a
    /// space name when one is configured; default is the unnamed ("0") space.
    /// @param cycleValue The cycle value to get the error context for
    /// @return A map containing the space name for the given cycle value
    Map<String, String> getErrorContextForCycle(long cycleValue);

    /// Modify an exception by prepending the space name when available. Used for richer context.
    /// @param error The original exception to modify
    /// @param cycleValue The cycle value associated with the exception
    /// @return The modified message with the space name prepended if applicable, or the original message
    RuntimeException modifyExceptionMessage(Exception error, long cycleValue);
}
