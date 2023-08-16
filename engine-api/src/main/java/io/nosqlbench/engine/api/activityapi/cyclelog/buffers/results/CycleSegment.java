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

package io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results;

/**
 * A segment of cycle numbers to iterate over. Usage of an InputSegment
 * is meant to be stack-local, or at least single threaded, so no
 * precautions are needed to make it thread safe.
 */
public interface CycleSegment {

    /**
     * The next cycle, which should be a positive number between 0 and Long.MAX_VALUE.
     * If a negative value is returned, then the caller should disregard the value
     * and assume that any further input segments will be invalid.
     *
     * <p>Implementations of this method should not worry about thread safety.
     * @return a positive and valid long cycle, or a negative indicator of end of input
     */
    long nextCycle();

    /**
     * @return true if the input can provide no further cycles
     */
    boolean isExhausted();

    default long[] nextCycles(int len) {
        long[] values = new long[len];
        for (int i = 0; i <values.length; i++) {
            long c = nextCycle();
            values[i]=c;
        }
        return values;
    }

    /**
     * Return the value of the next cycle which would be returned by {@link #nextCycle()}}
     * without modifying the segment, or a negative number if the cycle range would be
     * outside the valid range for this segment.
     * @return the next cycle that will be returned
     */
    long peekNextCycle();
}
