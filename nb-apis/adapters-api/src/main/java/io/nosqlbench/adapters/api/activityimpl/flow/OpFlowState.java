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

package io.nosqlbench.adapters.api.activityimpl.flow;

import java.util.Arrays;

/// Maintains a bounded set of {@link OpFlowContext} instances keyed by flow id (usually stride slot).
/// Contexts are reused and cleared on {@link #begin(long)} to avoid per-cycle churn while remaining
/// safe for concurrent use across virtual threads. This is a fixed-size holder based on stride, not
/// a general-purpose object pool.
///
/// ```text
/// flow-id: 0..N-1 (N = stride)
///   └─ OpFlowContext (space-indexed Object[] per flow, reset each flow)
/// ```
///
/// Usage:
/// 1. {@link #begin(long)} at the start of a cycle/flow to clear the slot and retrieve the context
/// 2. Hand the context to capture/inject paths
/// 3. Optionally call {@link #clearAll()} when tearing down an activity
///
/// Reset cost is limited to clearing existing maps in each slot; no heavy reinitialization.
public final class OpFlowState {

    private final OpFlowContext[] contexts;
    private final int flowCount;
    private final int initialOrdinalCapacity;
    private final int initialSpaceCapacity;

    public OpFlowState(long flowCount) {
        this(flowCount, 16, 4);
    }

    public OpFlowState(long flowCount, int initialOrdinalCapacity, int initialSpaceCapacity) {
        int boundedCount = (int) Math.max(1, Math.min(Integer.MAX_VALUE, flowCount));
        this.flowCount = boundedCount;
        this.initialOrdinalCapacity = Math.max(1, initialOrdinalCapacity);
        this.initialSpaceCapacity = Math.max(1, initialSpaceCapacity);
        this.contexts = new OpFlowContext[boundedCount];
        for (int i = 0; i < boundedCount; i++) {
            contexts[i] = new OpFlowContext(i, this.initialOrdinalCapacity, this.initialSpaceCapacity);
        }
    }

    public int flowCount() {
        return flowCount;
    }

    /**
     * Clear and return the context associated with the cycle's flow id.
     */
    public OpFlowContext begin(long cycle) {
        OpFlowContext ctx = contextFor(cycle);
        ctx.reset(cycle);
        return ctx;
    }

    /**
     * Retrieve a context without clearing it.
     */
    public OpFlowContext contextFor(long cycle) {
        int slot = Math.floorMod(cycle, flowCount);
        return contexts[slot];
    }

    public void clearAll() {
        Arrays.stream(contexts).forEach(c -> c.reset(-1L));
    }
}
