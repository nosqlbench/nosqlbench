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

package io.nosqlbench.engine.api.activityimpl.marker;

import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleResultSegmentBuffer;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleResultsSegment;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleResult;
import io.nosqlbench.engine.api.activityapi.output.Output;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

public class CoreOutputTest {

    @Test
    public void testCoreSimple0to4() {
        ContiguousOutputChunker ct4 = new ContiguousOutputChunker(0,3,4,1);
        TestReader r = new TestReader();
        ct4.addExtentReader(r);
        ct4.onCycleResult(0,0);
        ct4.onCycleResult(1,1);
        ct4.onCycleResult(2,2);
        ct4.onCycleResult(3,3);
        Assertions.assertThat(r.segments).hasSize(1);

        long[] cycles = StreamSupport.stream(r.segments.get(0).spliterator(), false)
                .mapToLong(CycleResult::getCycle).toArray();
        int[] results = StreamSupport.stream(r.segments.get(0).spliterator(), false)
                .mapToInt(CycleResult::getResult).toArray();
        assertThat(cycles).containsExactly(0L,1L,2L,3L);
        assertThat(results).containsExactly(0,1,2,3);
    }

    private static class TestReader implements Output {
        List<CycleResultsSegment> segments = new ArrayList<>();

        @Override
        public boolean onCycleResult(long completedCycle, int result) {
            CycleResultSegmentBuffer b = new CycleResultSegmentBuffer(1);
            b.append(completedCycle,result);
            onCycleResultSegment(b.toReader());
            return true;
        }

        @Override
        public void onCycleResultSegment(CycleResultsSegment segment) {
            this.segments.add(segment);
        }
    }
}
