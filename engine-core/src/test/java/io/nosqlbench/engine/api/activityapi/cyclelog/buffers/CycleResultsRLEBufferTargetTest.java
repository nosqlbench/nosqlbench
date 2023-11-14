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

import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results_rle.CycleResultsRLEBufferReadable;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results_rle.CycleResultsRLEBufferTarget;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleResult;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;

public class CycleResultsRLEBufferTargetTest {

    @Test
    public void testBasicRLEncoding() {
        CycleResultsRLEBufferTarget tb = new CycleResultsRLEBufferTarget(1024);

        assertThat(tb.getRawBufferCapacity()).isEqualTo(17408);

        tb.onCycleResult(0L,0);
        tb.onCycleResult(1L,1);
        CycleResultsRLEBufferReadable r = tb.toSegmentsReadable();

        ArrayList<CycleResult> cycles = new ArrayList<>();
        Iterable<CycleResult> ci = r.getCycleResultIterable();
        Iterator<CycleResult> cit = ci.iterator();
        while (cit.hasNext()) {
            cycles.add(cit.next());
        }
        long[] cycleValues = cycles.stream().mapToLong(CycleResult::getCycle).toArray();
        assertThat(cycleValues).containsExactly(0L,1L);

        int[] resultValues = cycles.stream().mapToInt(CycleResult::getResult).toArray();
        assertThat(resultValues).containsExactly(0,1);
    }

    @Test
    public void testGappedIntervalRLEEncoding() {
        CycleResultsRLEBufferTarget tb = new CycleResultsRLEBufferTarget(100000);

        assertThat(tb.getRawBufferCapacity()).isEqualTo(1700000);

        tb.onCycleResult(0L,0);
        tb.onCycleResult(13L,1);
        tb.onCycleResult(14L,1);
        tb.onCycleResult(15L,1);
        tb.onCycleResult(28L,2);
        tb.onCycleResult(29L,2);
        tb.onCycleResult(100L,5);
        tb.onCycleResult(101L,6);
        tb.onCycleResult(102L,7);

        CycleResultsRLEBufferReadable r = tb.toSegmentsReadable();

        ArrayList<CycleResult> cycles = new ArrayList<>();
        r.getCycleResultIterable().iterator().forEachRemaining(cycles::add);

        long[] cycleValues = cycles.stream().mapToLong(CycleResult::getCycle).toArray();
        assertThat(cycleValues).containsExactly(0L,13L,14L,15L,28L,29L,100L,101L,102L);

        int[] resultValues = cycles.stream().mapToInt(CycleResult::getResult).toArray();
        assertThat(resultValues).containsExactly(0,1,1,1,2,2,5,6,7);

    }

    @Test
    public void testResize() {
        CycleResultsRLEBufferTarget tb = new CycleResultsRLEBufferTarget(3);

        assertThat(tb.getRawBufferCapacity()).isEqualTo(51);

        tb.onCycleResult(0L,0);
        tb.onCycleResult(13L,1);
        tb.onCycleResult(14L,2);
        tb.onCycleResult(15L,3);
        assertThat(tb.getRecordCapacity()).isEqualTo(3);
        tb.onCycleResult(19L,19);
        assertThat(tb.getRecordCapacity()).isEqualTo(6);
        tb.onCycleResult(20L,10);
        tb.onCycleResult(21L,21);
        tb.onCycleResult(22L,22);
        assertThat(tb.getRecordCapacity()).isEqualTo(12);


    }

}
