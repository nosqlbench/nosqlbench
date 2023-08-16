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

import java.util.Arrays;

public class CycleSegmentBuffer {

    long[] cycleNumbers;
    int pos = 0;

    public CycleSegmentBuffer(int size) {
        cycleNumbers = new long[size];
    }

    public void append(long cycleNumber) {
        cycleNumbers[pos++] = cycleNumber;
    }

    public CycleSegment toReadable() {
        if (pos == cycleNumbers.length) {
            return new CycleArray.ArraySegment(cycleNumbers);
        } else {
            return new CycleArray.ArraySegment(Arrays.copyOfRange(cycleNumbers, 0, pos));
        }
    }

    public int remaining() {
        return cycleNumbers.length - pos;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CycleSegmentBuffer (size=").append(cycleNumbers.length).append(")=>");

        if (cycleNumbers.length > 100) {
            sb.append(Arrays.toString(Arrays.copyOfRange(cycleNumbers, 0, 20)))
                    .append(", ..., ")
                    .append(Arrays.toString(Arrays.copyOfRange(cycleNumbers, cycleNumbers.length - 21, cycleNumbers.length - 1)));
        } else {
            sb.append(Arrays.toString(cycleNumbers));
        }
        return sb.toString();

    }
}
