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

package io.nosqlbench.engine.api.activityimpl.input;

import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleSegment;
import io.nosqlbench.engine.api.activityapi.input.Input;

import java.util.concurrent.atomic.AtomicLong;

public class InputInterval implements Input {

    private final long min;
    private final long nextMin;
    private final AtomicLong next;
    private final long recycle;

    public InputInterval(long currentRecycle, long min, long nextMin) {
        this.min = min;
        this.nextMin = nextMin;
        this.next = new AtomicLong(min);
        this.recycle = currentRecycle;
    }

    @Override
    public CycleSegment getInputSegment(int segmentLength) {
        while (true) {
            int current = next.intValue();
            int nextCurrent = current + segmentLength;
            if (nextCurrent > nextMin) {
                return null;
            }
            if (next.compareAndSet(current,nextCurrent)) {
                return new Segment(recycle,current,nextCurrent);
            }
            // in all other cases, there was a CAS race condition, and we want to retry
        }
    }

    public String toString() {
        return "InputInterval[" + min + "," + nextMin + "), next=" + next.intValue();
    }

    public static class Segment implements CycleSegment {

        private final long afterEnd;
        private final long start;
        private final long recycle;
        private long next;

        public Segment(long currentRecycle, long start, long afterEnd) {
            this.start = start;
            this.afterEnd = afterEnd;
            this.next = start;
            this.recycle = currentRecycle;
        }

        @Override
        public long nextCycle() {
            if (next < afterEnd) {
                return next++;
            }
            return -100;
        }

        @Override
        public long nextRecycle() {
            return recycle;
        }

        @Override
        public long peekNextCycle() {
            if (next < afterEnd) {
                return next;
            }
            return -100;
        }


        @Override
        public boolean isExhausted() {
            return next >= afterEnd;
        }


        public String toString() {
            return "InputInterval.Segment(" + start + "," + afterEnd + "]: next=" + next;
        }
    }

    @Override
    public boolean isContiguous() {
        return true;
    }
}
