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

package io.nosqlbench.engine.api.activityapi.core.ops.fluent.opfacets;

import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleMutable;

/**
 * A tracked op is one that has been added to a tracker, and can
 * then be started.
 * @param <D> The payload type of this op.
 */
public interface TrackedOp<D> extends Payload<D>, CycleMutable {

    /**
     * Signify to NB that the associated operation is known to
     * have started processing in some specific way according to the implementing activity type.
     *
     * This should be called after any operational setup work that would not occur
     * in a typical application client scenario. The moment this is called, the
     * start time for the operation is marked.
     *
     * @return a StartedOp of the appropriate generic delegate type
     */
    StartedOp<D> start();

    /**
     * Mark that this operation is being skipped by the activity type for some reason.
     *
     * @param reason An integer code, activity type specific, to track why the operation was skipped
     * @return a SkippedOp of the appropriate generic delegate type
     */
    SkippedOp<D> skip(int reason);

    /**
     * Indicate to this op, how much wait time elapsed between the time it was expected
     * to start and the time it actually started. This is used to calculate the response time
     * once service time is known.
     * @param cycleDelay nanosecond delay
     * @return a TrackedOp for method chaining
     */
    TrackedOp<D> setWaitTime(long cycleDelay);

}
