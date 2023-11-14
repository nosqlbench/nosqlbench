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

package io.nosqlbench.engine.api.activityapi.cyclelog.buffers;

import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleResultsSegment;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results_rle.CycleResultsRLEBufferReadable;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results_rle.CycleResultsRLEBufferTarget;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleResult;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

public class CycleResultsRLEBufferReadableTest {

    @Test
    public void testRLESingle3() {
        ByteBuffer bb = ByteBuffer.allocate(3 * (Long.BYTES + Long.BYTES + Byte.BYTES));
        bb.putLong(31L).putLong(32L).put((byte) 127);
        bb.putLong(41L).putLong(43L).put((byte) 53);
        bb.putLong(132L).putLong(135L).put((byte) 27);
        bb.flip();
        CycleResultsRLEBufferReadable crb = new CycleResultsRLEBufferReadable(bb);

        ArrayList<CycleResult> cycles = new ArrayList<>();
        crb.iterator().forEachRemaining(s -> s.forEach(cycles::add));
        long[] cycleValues = cycles.stream().mapToLong(CycleResult::getCycle).toArray();
        int[] resultValues = cycles.stream().mapToInt(CycleResult::getResult).toArray();
        assertThat(cycleValues).containsExactly(31L, 41L, 42L, 132L, 133L, 134L);
        assertThat(resultValues).containsExactly(127, 53, 53, 27, 27, 27);
    }

    @Test
    public void testMultipleRanges() {
        CycleResultsRLEBufferTarget t = new CycleResultsRLEBufferTarget(1000);
        t.onCycleResult(1L, 1);
        t.onCycleResult(2L, 1);
        t.onCycleResult(10L, 2);
        t.onCycleResult(11L, 2);
        t.onCycleResult(13L, 2);
        t.onCycleResult(7L, 7);

//        CycleResultsRLEBufferReadable cr = t.toSegmentsReadable();
        long[] cycleValues = StreamSupport.stream(t.toSegmentsReadable().spliterator(),false).flatMap(r->StreamSupport.stream(r.spliterator(),false))
                .mapToLong(CycleResult::getCycle).toArray();
        int[] resultValues = StreamSupport.stream(t.toSegmentsReadable().spliterator(),false).flatMap(s->StreamSupport.stream(s.spliterator(),false))
                .mapToInt(CycleResult::getResult).toArray();

        assertThat(cycleValues).containsExactly(1L, 2L, 10L, 11L, 13L, 7L);
        assertThat(resultValues).containsExactly(1, 1, 2, 2, 2, 7);
    }

    @Test
    public void testGetCycleResultsSegment() {
        CycleResultsRLEBufferTarget t = new CycleResultsRLEBufferTarget(1000);
        t.onCycleResult(1L, 1);
        t.onCycleResult(2L, 1);
        t.onCycleResult(10L, 2);
        t.onCycleResult(11L, 2);
        t.onCycleResult(13L, 2);
        t.onCycleResult(7L, 7);

        CycleResultsRLEBufferReadable readable = t.toSegmentsReadable();

        CycleResultsSegment s1 = readable.iterator().next();
    }

    @Test
    public void testIteratorExhausted() {
        CycleResultsRLEBufferTarget t = new CycleResultsRLEBufferTarget(1000);
        t.onCycleResult(1L, 5);
        t.onCycleResult(2L, 6);
        t.onCycleResult(4L, 8);

        CycleResultsRLEBufferReadable cr = t.toSegmentsReadable();
        Iterator<CycleResultsSegment> iterator = cr.iterator();
        CycleResultsSegment s1 = iterator.next();
        assertThat(s1.getCount()).isEqualTo(1);
        CycleResultsSegment s2 = iterator.next();
        assertThat(s2.getCount()).isEqualTo(1);
        CycleResultsSegment s3 = iterator.next();
        assertThat(s3.getCount()).isEqualTo(1);
        assertThat(iterator.hasNext()).isFalse();

//        long[] cycleValues = StreamSupport.stream(cr.spliterator(), false)
//                .mapToLong(CycleResult::getCycle).toArray();
//        int[] resultValues = StreamSupport.stream(cr.spliterator(), false)
//                .mapToInt(CycleResult::getResult).toArray();
//
//        assertThat(cycleValues).containsExactly(1L, 2L, 4L);
//        assertThat(resultValues).containsExactly(5, 6, 8);
//
    }

}
