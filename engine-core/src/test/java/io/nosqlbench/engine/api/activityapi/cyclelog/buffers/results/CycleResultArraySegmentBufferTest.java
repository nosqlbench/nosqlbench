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

import org.junit.jupiter.api.Test;

import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

public class CycleResultArraySegmentBufferTest {

    @Test
    public void testBasicBuffering() {
        CycleResultSegmentBuffer buf = new CycleResultSegmentBuffer(3);
        buf.append(302,3);
        buf.append(305,2);
        buf.append(23L,1);
        CycleResultsSegment cycleResults = buf.toReader();
        long[] cycles = StreamSupport.stream(cycleResults.spliterator(), false)
                .mapToLong(CycleResult::getCycle).toArray();
        int[] results = StreamSupport.stream(cycleResults.spliterator(), false)
                .mapToInt(CycleResult::getResult).toArray();

        assertThat(cycles).containsExactly(302L,305L,23L);
        assertThat(results).containsExactly(3,2,1);

    }

}
