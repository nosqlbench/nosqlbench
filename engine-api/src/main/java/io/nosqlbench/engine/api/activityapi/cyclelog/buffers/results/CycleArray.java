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

import io.nosqlbench.engine.api.activityapi.input.Input;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Just cycle numbers in a long array.
 */
public class CycleArray implements Input {

    private final AtomicInteger offset=new AtomicInteger();
    private final long[] cycles;

    public CycleArray(long... values) {
        this.cycles = values;
    }

    @Override
    public CycleSegment getInputSegment(int segmentLength) {
        while (true) {
            int current = offset.get();
            int nextOffset=current+segmentLength;
            if (nextOffset>cycles.length) {
                return null;
            }
            if (offset.compareAndSet(current,nextOffset)) {
                return new ArraySegment(Arrays.copyOfRange(cycles,current,nextOffset));
            }
            // in all other cases, there was a CAS race condition, and we want to retry
        }
    }

    public static class ArraySegment implements CycleSegment {

        private final long[] values;
        private int offset=0;

        public ArraySegment(long[] values) {
            this.values = values;
        }

        @Override
        public long nextCycle() {
            if (offset<values.length) {
                return values[offset++];
            }
            return -100;
        }

        @Override
        public long peekNextCycle() {
            if (offset<values.length) {
                return values[offset];
            }
            return -100;
        }

        @Override
        public boolean isExhausted() {
            return (offset>=values.length);
        }


    }
}
