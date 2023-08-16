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

import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleResultsSegment;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleResult;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

public class ByteTrackerExtentTest {

    @Test
    public void testOrdered4() {
        ByteTrackerExtent bt4 = new ByteTrackerExtent(33, 37);
        bt4.markResult(36L,3);
        bt4.markResult(33L,0);
        bt4.markResult(35L,2);
        assertThat(bt4.getMarkerData()[3]).isEqualTo((byte)3);
        assertThat(bt4.getMarkerData()[0]).isEqualTo((byte)0);
        assertThat(bt4.getMarkerData()[2]).isEqualTo((byte)2);
        assertThat(bt4.isFullyFilled()).isFalse();
        bt4.markResult(34L,1);
        assertThat(bt4.getMarkerData()[1]).isEqualTo((byte)1);
        assertThat(bt4.isFullyFilled()).isTrue();

        List<CycleResult> cycleResults = new ArrayList<>();
        List<CycleResultsSegment> segments = StreamSupport.stream(bt4.spliterator(), false)
                .collect(Collectors.toList());
        for (CycleResultsSegment segment : segments) {
            segment.forEach(cycleResults::add);
        }
        long[] cycles = cycleResults.stream().mapToLong(CycleResult::getCycle).toArray();
        int[] results= cycleResults.stream().mapToInt(CycleResult::getResult).toArray();

        assertThat(cycles).containsExactly(33L,34L,35L,36L);
        assertThat(results).containsExactly(0,1,2,3);
//        CycleResultsSegment seg1 = iterator;
//        assertThat(seg1.cycle).isEqualTo(33);
//        assertThat(seg1.codes.length).isEqualTo(2);
//        assertThat(seg1.codes[0]).isEqualTo((byte)0);
//        assertThat(bt4.isFullyServed()).isFalse();
//        CycleResultsIntervalSegment seg2 = bt4.getCycleResultsSegment(2);
//        assertThat(bt4.isFullyServed()).isTrue();
    }


    @Test
    public void testFullCounter() {
        ByteTrackerExtent bt4 = new ByteTrackerExtent(33, 37);
        assertThat(bt4.markResult(36L,3)).isEqualTo(3);
        assertThat(bt4.markResult(33L,0)).isEqualTo(2);
        assertThat(bt4.markResult(35L,2)).isEqualTo(1);
        assertThat(bt4.markResult(35L,2)).isEqualTo(0);

        assertThat(bt4.markResult(33L,9)).isEqualTo(-1);
    }


}
