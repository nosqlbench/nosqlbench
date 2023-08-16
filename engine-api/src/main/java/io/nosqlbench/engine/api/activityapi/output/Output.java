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

package io.nosqlbench.engine.api.activityapi.output;

import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleResultsSegment;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleResult;

/**
 * A cycle output is simply a type that knows how to do something
 * useful with the result of a particular cycle.
 * Outputs are required to be thread-safe.
 */
public interface Output extends AutoCloseable {

    /**
     * Mark the result of the numbered cycle with an integer value.
     * The meaning of the value provided is contextual to the way it is used.
     * (Each process will have its own status tables, etc.)
     *
     * @param completedCycle The cycle number being marked.
     * @param result the result ordinal
     * @return true on success, false if the marking should be retried differently
     */
    boolean onCycleResult(long completedCycle, int result);

    default boolean onCycleResult(CycleResult result) {
        return this.onCycleResult(result.getCycle(),result.getResult());
    }

    default void onCycleResultSegment(CycleResultsSegment segment) {
        segment.forEach(s -> onCycleResult(s.getCycle(),s.getResult()));
    }

    default void close() throws Exception {
    }
}
