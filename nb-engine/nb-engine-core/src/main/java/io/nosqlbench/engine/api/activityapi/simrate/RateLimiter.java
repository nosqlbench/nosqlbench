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

package io.nosqlbench.engine.api.activityapi.simrate;

import io.nosqlbench.engine.api.activityapi.simrate.SimRateSpec;

import java.time.Duration;

public interface RateLimiter {

    /**
     * <P>Block until it is time for the next operation, according to the
     * nanoseconds per op as set by {@link #applyRateSpec(SimRateSpec)}
     * @return the waittime as nanos behind schedule when this op returns.
     * The returned value is required to be greater than or equal to zero.</P>
     *
     * <P>Note that accuracy of the returned value is limited by timing
     * precision and calling overhead of the real time clock. It will not
     * generally be better than microseconds. Also, some rate limiting
     * algorithms are unable to efficiently track per-op waittime at speed
     * due to bulk allocation mechanisms necessary to support higher rates.</P>
     */
    long block();

    /**
     * Return the total number of nanoseconds behind schedule
     * that this rate limiter is, including the full history across all
     * rates. When the rate is changed, this value is check-pointed to
     * an accumulator and also included in any subsequent measurement.
     * @return nanoseconds behind schedule since the rate limiter was started
     */
    Duration getTotalWaitTimeDuration();

    /**
     * Return the total number of nanoseconds behind schedule
     * that this rate limiter is, but only since the last time the rate spec
     * has changed. When the rate is changed, this value is check-pointed to
     * an accumulator and also included in any subsequent measurement.
     * @return nanoseconds behind schedule since the rate limiter was started
     */

    Duration getWaitTimeDuration();

    double getWaitTimeSeconds();

    /**
     * Modify the rate of a running rate limiter.
     * @param spec The rate and burstRatio specification
     */
    void applyRateSpec(SimRateSpec spec);


    /**
     * Return the system nanoseconds at the time when the last rate change
     * was made active by a starting or restarting rate spec.
     * @return long nanoseconds
     */
    long getStartTime();

    /**
     * Get the rate spec that this rate limiter was created from.
     * @return a RateSpec that describes this rate limiter
     */
    SimRateSpec getSpec();

}
