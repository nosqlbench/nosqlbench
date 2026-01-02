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

package io.nosqlbench.engine.api.activityimpl.uniform.actions;

import io.nosqlbench.adapters.api.activityimpl.flow.OpFlowContext;
import io.nosqlbench.adapters.api.activityimpl.flow.OpFlowState;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleResultSegmentBuffer;

/// Per-action stride-scoped state holder (sync path).
///
/// Responsibilities:
/// - Owns per-slot flow contexts (ordinal-array backed)
/// - Tracks last cycle for the current flow slot and optional per-cycle space index/name (per-cycle only)
/// - Holds a reusable result buffer sized to the stride
/// - Provides a stride-reset entry point via {@link #beginCycle(long)} to clear per-slot state
///
/// Lifecycle:
/// 1) Constructed once per action with stride length and initial capacities
/// 2) {@link #beginCycle(long)} invoked per cycle to clear the flow slot and return its context
/// 3) {@link #resultBuffer()} reused per stride; caller should {@link #resetResultBuffer()} at stride start
final class StrideContext {

    private final OpFlowState flowState;
    private long currentCycle = -1L;
    private String currentSpaceName = "0";
    private int currentSpaceIndex = 0;
    private OpFlowContext currentFlowContext;
    private java.util.function.LongFunction<String> spaceNameResolver;
    private java.util.function.LongToIntFunction spaceIndexResolver;
    private final CycleResultSegmentBuffer resultBuffer;
    private final int strideLength;

    StrideContext(long strideLength, int initialOrdinalCapacity, int initialSpaceCapacity) {
        this.flowState = new OpFlowState(strideLength, initialOrdinalCapacity, initialSpaceCapacity);
        this.strideLength = (int) Math.max(1, Math.min(Integer.MAX_VALUE, strideLength));
        this.resultBuffer = new CycleResultSegmentBuffer(this.strideLength);
    }

    /// Reset the flow slot for this cycle and return its context.
    OpFlowContext beginCycle(long cycle) {
        this.currentCycle = cycle;
        this.currentSpaceName = "0";
        this.currentSpaceIndex = 0;
        this.currentFlowContext = flowState.begin(cycle);
        return this.currentFlowContext;
    }

    /// Retrieve the flow context for a given cycle without clearing it.
    OpFlowContext flowFor(long cycle) {
        return flowState.contextFor(cycle);
    }

    long getCurrentCycle() {
        return currentCycle;
    }

    OpFlowContext getCurrentFlowContext() {
        return currentFlowContext;
    }

    void setCurrentSpaceName(String spaceName) {
        this.currentSpaceName = spaceName;
    }

    String getCurrentSpaceName() {
        return currentSpaceName;
    }

    void setCurrentSpaceIndex(int spaceIndex) {
        this.currentSpaceIndex = spaceIndex;
    }

    int getCurrentSpaceIndex() {
        return currentSpaceIndex;
    }

    void setSpaceNameResolver(java.util.function.LongFunction<String> nameResolver) {
        this.spaceNameResolver = nameResolver;
    }

    java.util.function.LongFunction<String> getSpaceNameResolver() {
        return spaceNameResolver;
    }

    void setSpaceIndexResolver(java.util.function.LongToIntFunction indexResolver) {
        this.spaceIndexResolver = indexResolver;
    }

    java.util.function.LongToIntFunction getSpaceIndexResolver() {
        return spaceIndexResolver;
    }

    /// Reusable result buffer for this stride; caller should reset at stride start.
    CycleResultSegmentBuffer resultBuffer() {
        return resultBuffer;
    }

    void resetResultBuffer() {
        resultBuffer.reset();
    }
}
