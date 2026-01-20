/*
 * Copyright (c) 2024 nosqlbench
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

package io.nosqlbench.engine.api.activityapi.core;

import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleSegment;

/// Stride-oriented action API: executes a stride worth of cycles given a {@link CycleSegment}.
/// This makes the stride the primary executable unit while preserving existing user-visible
/// behavior (rate limiting, metrics, error handling) for callers that still delegate per-cycle
/// with {@link #runCycle(long)}.
public interface StrideAction extends Action {

    /// Execute a single cycle and return its status code.
    int runCycle(long cycle);

    /// Execute all cycles in the provided segment, returning the last status code observed.
    /// Implementations should treat the segment as single-threaded and may reuse stride-local
    /// state across cycles.
    default int runStride(CycleSegment segment) {
        return runStride(segment, () -> {}, (c, r) -> {});
    }

    /// Execute all cycles in the provided segment, invoking hooks before each cycle and after each
    /// cycle. The last status code is returned.
    default int runStride(CycleSegment segment, Runnable beforeCycle, java.util.function.BiConsumer<Long,Integer> afterCycle) {
        int code = 0;
        while (!segment.isExhausted()) {
            long cycle = segment.nextCycle();
            if (cycle < 0) {
                break;
            }
            beforeCycle.run();
            code = runCycle(cycle);
            afterCycle.accept(cycle, code);
        }
        return code;
    }
}
