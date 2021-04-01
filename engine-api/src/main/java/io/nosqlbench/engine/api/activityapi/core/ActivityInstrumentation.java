/*
 *
 *    Copyright 2016 jshook
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * /
 */

package io.nosqlbench.engine.api.activityapi.core;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Timer;

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
     * @return A new or existing Timer
     */
    Timer getOrCreateInputTimer();

    /**
     * The strides service timer measures how long it takes to complete a stride of work.
     * @return A new or existing Timer
     */
    Timer getOrCreateStridesServiceTimer();

    /**
     * The strides response timer measures the total response time from the scheduled
     * time a stride should start to when it completed. Stride scheduling is only defined
     * when it is implied by a stride rate limiter, so this method should return null if
     * there is no strides rate limiter.
     * @return A new or existing Timer if appropriate, else null
     */
    Timer getStridesResponseTimerOrNull();

    /**
     * The cycles service timer measures how long it takes to complete a cycle of work.
     * @return A new or existing Timer
     */
    Timer getOrCreateCyclesServiceTimer();

    /**
     * The cycles response timer measures the total response time from the scheduled
     * time an operation should start to when it is completed. Cycle scheduling is only defined
     * when it is implied by a cycle rate limiter, so this method should return null if
     * there is no cycles rate limiter.
     * @return A new or existing Timer if appropriate, else null
     */
    Timer getCyclesResponseTimerOrNull();

    /**
     * The phases service timer measures how long it takes to complete a phase of work.
     * @return A new or existing Timer
     */
    Timer getOrCreatePhasesServiceTimer();

    /**
     * The phases response timer measures the total response time from the scheduled
     * time a phase should start to when it is completed. Phase scheduling is only defined
     * when it is implied by a phase rate limiter, so this method should return null if
     * there is no phases rate limiter.
     * @return A new or existing Timer if appropriate, else null
     */
    Timer getPhasesResponseTimerOrNull();

    /**
     * The pending ops counter keeps track of how many ops are submitted or in-flight, but
     * which haven't been completed yet.
     * @return A new or existing Counter
     */
    Counter getOrCreatePendingOpCounter();

    Counter getOrCreateOpTrackerBlockedCounter();

    Timer getOrCreateBindTimer();

    Timer getOrCreateExecuteTimer();

    Timer getOrCreateResultTimer();

    Timer getOrCreateResultSuccessTimer();

    Histogram getOrCreateTriesHistogram();
}
