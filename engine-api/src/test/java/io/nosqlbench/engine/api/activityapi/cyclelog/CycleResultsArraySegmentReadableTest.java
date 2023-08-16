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

package io.nosqlbench.engine.api.activityapi.cyclelog;

import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleResultSegmentBuffer;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleResultsSegment;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleResult;
import org.junit.jupiter.api.Test;

import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

public class CycleResultsArraySegmentReadableTest {

    @Test
    public void testCycleResultSegmentReader() {
        CycleResultSegmentBuffer bb = new CycleResultSegmentBuffer(5);
        bb.append(33L, 0);
        bb.append(34L, 1);
        bb.append(35L, 2);
        bb.append(36L, 1);
        bb.append(39L, 0);
        CycleResultsSegment cycleResults = bb.toReader();

        long[] cycles = StreamSupport.stream(cycleResults.spliterator(), false)
                .mapToLong(CycleResult::getCycle).toArray();
        int[] results = StreamSupport.stream(cycleResults.spliterator(), false)
                .mapToInt(CycleResult::getResult).toArray();
        assertThat(cycles).containsExactly(33L, 34L, 35L, 36L, 39L);
        assertThat(results).containsExactly(0, 1, 2, 1, 0);


    }

}
