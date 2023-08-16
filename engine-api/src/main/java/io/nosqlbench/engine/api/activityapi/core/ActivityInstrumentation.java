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

package io.nosqlbench.engine.api.activityapi.core;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Timer;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.adapters.api.activityimpl.OpDispenser;

import java.util.concurrent.Future;

/**
 * All the accessors of the metrics that will be used for each activity instance.
 * Implementors of this interface should ensure that the methods are synchronized
 * to avoid race conditions during lazy init from callers.
 *
 *
 */
public interface ActivityInstrumentation {

    /**
     * The input timer measures how long it takes to get the cycle value to be used for
     * an operation.
     * @return a new or existing {@link Timer}
     */
    Timer getOrCreateInputTimer();

    /**
     * The strides service timer measures how long it takes to complete a stride of work.
     * @return a new or existing {@link Timer}
     */
    Timer getOrCreateStridesServiceTimer();

    /**
     * The strides response timer measures the total response time from the scheduled
     * time a stride should start to when it completed. Stride scheduling is only defined
     * when it is implied by a stride rate limiter, so this method should return null if
     * there is no strides rate limiter.
     * @return a new or existing {@link Timer} if appropriate, else null
     */
    Timer getStridesResponseTimerOrNull();

    /**
     * The cycles service timer measures how long it takes to complete a cycle of work.
     * @return a new or existing {@link Timer}
     */
    Timer getOrCreateCyclesServiceTimer();

    /**
     * The cycles response timer measures the total response time from the scheduled
     * time an operation should start to when it is completed. Cycle scheduling is only defined
     * when it is implied by a cycle rate limiter, so this method should return null if
     * there is no cycles rate limiter.
     * @return a new or existing {@link Timer} if appropriate, else null
     */
    Timer getCyclesResponseTimerOrNull();

    /**
     * The pending ops counter keeps track of how many ops are submitted or in-flight, but
     * which haven't been completed yet.
     * @return a new or existing {@link Counter}
     */
    Counter getOrCreatePendingOpCounter();

    Counter getOrCreateOpTrackerBlockedCounter();

    /**
     * The bind timer keeps track of how long it takes for NoSQLBench to create an instance
     * of an executable operation, given the cycle. This is usually done by using an
     * {@link OpSequence} in conjunction with
     * an {@link OpDispenser}. This is named for "binding
     * a cycle to an operation".
     * @return a new or existing {@link Timer}
     */
    Timer getOrCreateBindTimer();

    /**
     * The execute timer keeps track of how long it takes to submit an operation to be executed
     * to an underlying native driver. For asynchronous APIs, such as those which return a
     * {@link Future}, this is simply the amount of time it takes to acquire the future.
     *
     * When possible, APIs should be used via their async methods, even if you are implementing
     * a {@link SyncAction}. This allows the execute timer to measure the hand-off to the underlying API,
     * and the result timer to measure the blocking calls to aquire the result.
     * @return a new or existing {@link Timer}
     */
    Timer getOrCreateExecuteTimer();

    /**
     * The result timer keeps track of how long it takes a native driver to service a request once submitted.
     * This timer, in contrast to the result-success timer ({@link #getOrCreateResultSuccessTimer()}),
     * is used to track all operations. That is, no matter
     * whether the operation succeeds or not, it should be tracked with this timer. The scope of this timer should
     * cover each attempt at an operation through a native driver. Retries are not to be combined in this measurement.
     * @return a new or existing {@link Timer}
     */
    Timer getOrCreateResultTimer();

    /**
     * The result-success timer keeps track of operations which had no exception. The measurements for this timer should
     * be exactly the same values as used for the result timer ({@link #getOrCreateResultTimer()}, except that
     * attempts to complete an operation which yield an exception should be excluded from the results. These two metrics
     * together provide a very high level sanity check against the error-specific metrics which can be reported by
     * the error handler logic.
     * @return a new or existing {@link Timer}
     */
    Timer getOrCreateResultSuccessTimer();

    /**
     * The tries histogram tracks how many tries it takes to complete an operation successfully, or not. This histogram
     * does not encode whether operations were successful or not. Ideally, if every attempt to complete an operation succeeds
     * on its first try, the data in this histogram should all be 1. In practice, systems which are running near their
     * capacity will see a few retried operations, and systems that are substantially over-driven will see many retried
     * operations. As the retries value increases the further down the percentile scale you go, you can detect system loading
     * patterns which are in excess of the real-time capability of the target system.
     *
     * This metric should be measured around every retry loop for a native operation.
     * @return a new or existing {@link Histogram}
     */
    Histogram getOrCreateTriesHistogram();
}
