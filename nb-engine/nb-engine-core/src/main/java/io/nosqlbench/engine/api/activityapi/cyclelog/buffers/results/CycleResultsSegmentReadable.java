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



import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

/**
 * Implements a cycle result segment in a basic buffer
 * that contains the cycle and the result in long, byte format.
 */
public class CycleResultsSegmentReadable implements CycleResultsSegment {

    private final static int BYTES = Long.BYTES + Byte.BYTES;
    private final ByteBuffer buf;

    public CycleResultsSegmentReadable(ByteBuffer buf) {
        this.buf = buf;
    }

    public static CycleResultsSegment forCycleResult(long completedCycle, int result) {
        ByteBuffer single = ByteBuffer.allocate(BYTES);
        single.putLong(completedCycle).put((byte) result);
        single.flip();
        return new CycleResultsSegmentReadable(single);
    }


    @Override
    public Iterator<CycleResult> iterator() {
        return new Iter();
    }

    @Override
    public String toString() {
        ByteBuffer bb = ByteBuffer.wrap(buf.array());
        StringBuilder sb = new StringBuilder();
        while (bb.remaining() > 0) {
            long cycle = bb.getLong();
            byte value = bb.get();
            sb.append(cycle).append("=>").append(value).append("\n");
        }
        return sb.toString();
    }

    @Override
    public long getCount() {
        return (buf.limit())/ BYTES;
    }

    @Override
    public long getMinCycle() {
        if (buf != null && buf.limit() > 0) {
            return buf.getLong(0);
        }
        return Long.MIN_VALUE;
    }

    // TODO: Make this work with RLE segments
    @Override
    public CycleResultsSegment filter(Predicate<ResultReadable> filter) {
        CycleResult[] filteredResults = StreamSupport.stream(spliterator(), false).filter(filter).toArray(CycleResult[]::new);
        return new CycleResultArray(filteredResults);
    }

    private class Iter implements Iterator<CycleResult> {
        private int offset = 0;

        @Override
        public boolean hasNext() {
            return (offset + BYTES <= buf.limit());
        }

        @Override
        public CycleResult next() {
            BBCycleResult cycleResult = new BBCycleResult(offset);
            offset += BYTES;
            return cycleResult;

        }
    }

    private class BBCycleResult implements CycleResult {

        private final int offset;

        BBCycleResult(int offset) {
            this.offset = offset;
        }

        @Override
        public long getCycle() {
            return buf.getLong(offset);
        }

        @Override
        public int getResult() {
            return buf.get(offset + Long.BYTES);
        }
    }

}
