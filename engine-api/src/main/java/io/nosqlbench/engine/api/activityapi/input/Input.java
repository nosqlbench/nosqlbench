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

package io.nosqlbench.engine.api.activityapi.input;

import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleSegment;

public interface Input {

    /**
     * Return the next InputSegment available, or null if
     * none were available. This method is required to be thread safe.
     * <p>All implementations of this method are required to be thread-safe.
     * @param segmentLength The number of cycles (not necessarily contiguous) in the segment.
     * @return a segment, or null if none available.
     */
    CycleSegment getInputSegment(int segmentLength);

    default boolean isContiguous() {
        return false;
    }
}


