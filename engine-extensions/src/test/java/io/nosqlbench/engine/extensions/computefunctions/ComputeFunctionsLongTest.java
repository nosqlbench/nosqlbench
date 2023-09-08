/*
 * Copyright (c) 2023 nosqlbench
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

package io.nosqlbench.engine.extensions.computefunctions;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;

class ComputeFunctionsLongTest {

    private final static Offset<Double> offset=Offset.offset(0.001d);
    private final static long[] longs_0to9 =new long[]{0,1,2,3,4,5,6,7,8,9};
    private final static long[] longs_37195 = new long[]{3,7,1,9,5};
    private final static long[] longs_86204 = new long[]{8,6,2,0,4};
    private final static long[] longs_01234 = new long[]{0,1,2,3,4};
    private final static long[] longs_56789 = new long[]{5,6,7,8,9};

    private final static long[] longs_369 = new long[]{3,6,9};
    private final static long[] longs_693 = new long[]{6,9,3};
    private final static long[] longs_45678 = new long[]{4,5,6,7,8};
    private final static long[] longs_12390 = new long[]{1,2,3,9,0};

    @Test
    void testRecallLongArrays() {
        assertThat(ComputeFunctions.recall(longs_86204, longs_37195))
            .as("finding 0 actual of any should yield recall=0.0")
            .isCloseTo(0.0d, offset);

        assertThat(ComputeFunctions.recall(longs_86204, longs_369))
            .as("finding 1 actual of 5 relevant should yield recall=0.2")
            .isCloseTo(0.2d, offset);

        assertThat(ComputeFunctions.recall(longs_86204, longs_369,1))
            .as("finding 0 (limited) actual of 5 relevant should yield recall=0.0")
            .isCloseTo(0.0d, offset);
    }

    @Test
    void testPrecisionLongArrays() {
        assertThat(ComputeFunctions.precision(longs_86204, longs_693))
            .as("one of three results being relevant should yield precision=0.333")
            .isCloseTo(0.333,offset);
        assertThat(ComputeFunctions.precision(longs_86204, longs_01234))
            .as("three of five results being relevant should yield precision=0.6")
            .isCloseTo(0.6,offset);
        assertThat(ComputeFunctions.precision(longs_86204, longs_37195))
            .as("none of the results being relevant should yield precision=0.0")
            .isCloseTo(0.0,offset);
    }

    @Test
    public void sanityCheckRecallAndLimitRatioLongs() {
        long[] hundo = LongStream.range(0,100).toArray();

        for (int i = 0; i < hundo.length; i++) {
            long[] partial=LongStream.range(0,i).toArray();
            int finalI = i;
            assertThat(ComputeFunctions.recall(hundo, partial))
                .as(() -> "for subset size " + finalI +", recall should be fractional/100")
                .isCloseTo((double)partial.length/(double)hundo.length,offset);
            assertThat(ComputeFunctions.recall(hundo, hundo, i))
                .as(() -> "for full intersection, limit " + finalI +" (K) recall should be fractional/100")
                .isCloseTo((double)partial.length/(double)hundo.length,offset);
        }
    }

    @Test
    public void testReciprocalRankLongs() {
        assertThat(ComputeFunctions.reciprocal_rank(longs_369, longs_56789))
            .as("relevant results in rank 2 should yield RR=0.5")
            .isCloseTo(0.5d,offset);

        assertThat(ComputeFunctions.reciprocal_rank(longs_56789, longs_01234))
            .as("no relevant results should yield RR=0.0")
            .isCloseTo(0.0d,offset);
    }

//    @Test
//    public void testLongIntersection() {
//        int[] result = Intersections.find(lowInts01234,midInts45678);
//        assertThat(result).isEqualTo(new int[]{4,5});
//    }

    @Test
    public void testCountLongIntersection() {
        long result = Intersections.count(longs_37195, longs_12390);
        assertThat(result).isEqualTo(3L);
    }

}
