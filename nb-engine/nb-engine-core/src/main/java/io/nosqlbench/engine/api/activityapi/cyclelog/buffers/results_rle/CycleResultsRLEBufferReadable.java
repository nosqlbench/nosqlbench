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

package io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results_rle;

import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.ResultReadable;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.CycleResultSegmentsReadable;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleResultsSegment;


import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.function.Predicate;

/**
 * Implements a cycle result segment in a run-length encoded buffer
 * that contains the cycle interval and the result in long, long, byte format,
 * where the last value (the second long value) is *not* included in the
 * cycle inteval. (closed-open interval)
 * <p>This is <em>not</em> a threadsafe iterator. It references buffer data
 * that is presumed to be access by only one reader for the sake of efficiency.
 */
public class CycleResultsRLEBufferReadable implements CycleResultSegmentsReadable {

    public final static int BYTES = Long.BYTES + Long.BYTES + Byte.BYTES;
    private final ByteBuffer buf;
//    private long count = Long.MIN_VALUE;
//    private long min = Long.MAX_VALUE;

    public CycleResultsRLEBufferReadable(ByteBuffer buf) {
        this.buf = buf;
    }

    public CycleResultsRLEBufferReadable(int readSizeInSpans, ByteBuffer src) {
        readSizeInSpans = readSizeInSpans * BYTES;
        int bufsize = Math.min(readSizeInSpans, src.remaining());
        byte[] bbuf = new byte[bufsize];
        src.get(bbuf);
        this.buf = ByteBuffer.wrap(bbuf);
    }

    public Iterator<CycleResultsSegment> iterator(Predicate<ResultReadable> filter) {
        return new ResultSpanIterator(buf,filter);
    }

//    public long getMin() {
//        if (min==Long.MAX_VALUE) {
//            long min=Long.MAX_VALUE;
//            Optional<CycleResultsSegment> minSeg = StreamSupport.stream(spliterator(), false).min(CycleResultsSegment::compareTo);
//            min=minSeg.map(CycleResultsSegment::getMinCycle).orElse(Long.MAX_VALUE);
//        }
//        return min;
//    }
//
//    public long getCount() {
//        if (count<=0) {
//            LongAccumulator acc = new LongAccumulator((l1,l2)->l1+l2,0);
//            iterator().forEachRemaining(crs -> acc.accumulate(crs.getCount()));
//            count = acc.get();
//        }
//        return count;
//    }
//

    @Override
    public Iterator<CycleResultsSegment> iterator() {
        return new ResultSpanIterator(buf,null);
    }

    private class ResultSpanIterator implements Iterator<CycleResultsSegment> {
        private final ByteBuffer iterbuf;
        private final Predicate<ResultReadable> filter;
        private CycleResultsSegment next;

        public ResultSpanIterator(ByteBuffer buf, Predicate<ResultReadable> filter) {
            this.iterbuf = buf;
            this.filter = filter;
        }

        @Override
        public boolean hasNext() {
            while (next==null && iterbuf.remaining()>0) {
                CycleSpanResults csr = read(iterbuf);
                if (filter==null || filter.test(csr)) {
                    next = csr;
                }
            }
            return next!=null;
        }

        @Override
        public CycleResultsSegment next() {
            if (next == null && !hasNext()) {
                throw new RuntimeException("Call to next() but there was no remaining unfiltered data.");
            }
            CycleResultsSegment wasNext = this.next;
            next=null;
            return wasNext;
        }

        private CycleSpanResults read(ByteBuffer iterbuf) {
            long min = iterbuf.getLong();
            long nextMin = iterbuf.getLong();
            int result = iterbuf.get();
            return new CycleSpanResults(min, nextMin, result);
        }

        public String toString() {
            return "ResultSpanIterator (" + iterbuf.toString() + ")";
        }

    }

}
