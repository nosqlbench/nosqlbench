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

import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleReadable;

/**
 * A StartedOp is a type that represents that an operation has been sent to some
 * specific type of protocol or logic.
 *
 * <H2>Correctness</H2>
 * It is essential that developers close a StartedOp in one of the appropriate ways.
 * Creating a StartedOp and not calling either {@link #succeed(int)}, or {@link #fail(int)}
 * will cause deadlocks in concurrency management. This is because the logic which tracks
 * operations relies on these signals to know when to close out an operation and allow
 * another to be started according to concurrency controls.
 *
 * {@link #retry()} can be called as many times as an activity allows for, but this is
 * not sufficient to retire the operation. After calling {@link #retry()}, one of the
 * end states above must be set.
 *
 * @param <D> The delegate type that is need for the implementing activity type
 */
public interface StartedOp<D> extends Payload<D>, CycleReadable {
    /**
     * Reset the service timer on this op, and increment the tries counter
     * @return A StartedOp
     */
    StartedOp<D> retry();

    /**
     * Mark this op as successful as of the time it is called, and record the resulting status code.
     * @param status The status for this op, determined by individual activity types
     * @return a SucceededOp
     */
    SucceededOp<D> succeed(int status);

    /**
     * Mark this op as failed as of the time it is called, and record the resulting status code.
     * @param status The status for this op, determined by individual activity types
     * @return A FailedOp
     */
    FailedOp<D> fail(int status);

    /**
     * Get the nanosecond instant which was recorded for this operation to be started.
     * Specifically, this is when the operation was known to enter a native protocol or
     * activity type section of logic which is more than just preparatory work by the
     * client before execution.
     * @return nanoseconds started instant
     */
    long getStartedAtNanos();

    /**
     * Return the nanos that have elapsed since the op was started at the time of this call.
     * @return nanosecond service time duration
     */
    long getCurrentServiceTimeNanos();

    /**
     * Return the nanos that have elapsed since this op was started at the time of this call,
     * plus any prior waittime.
     * @return nanosecond response time duration
     */
    long getCurrentResponseTimeNanos();

}
