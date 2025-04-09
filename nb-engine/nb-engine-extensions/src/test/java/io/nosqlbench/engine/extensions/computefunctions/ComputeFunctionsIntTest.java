/*
 * Copyright (c) nosqlbench
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

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class ComputeFunctionsIntTest {

    private final static Offset<Double> offset=Offset.offset(0.001d);
    private final static int[] allInts =new int[]{0,1,2,3,4,5,6,7,8,9};
    private final static int[] oddInts37195 = new int[]{3,7,1,9,5};
    private final static int[] evenInts86204 = new int[]{8,6,2,0,4};
    private final static int[] lowInts01234 = new int[]{0,1,2,3,4};
    private final static int[] highInts56789 = new int[]{5,6,7,8,9};

    private final static int[] intsBy3_369 = new int[]{3,6,9};
    private final static int[] intsBy3_369_12_15 = new int[]{3,6,9,12,15};
    private final static int[] intsBy3_693 = new int[]{6,9,3};
    private final static int[] midInts45678 = new int[]{4,5,6,7,8};
    private final static int[] ints12390 = new int[]{1,2,3,9,0};

    @Test
    void testRecallIntArrays() {
        assertThat(ComputeFunctions.recall(evenInts86204,oddInts37195, 5))
            .as("finding 0 actual of any should yield recall=0.0")
            .isCloseTo(0.0d, offset);

        assertThat(ComputeFunctions.recall(evenInts86204,intsBy3_369_12_15, 5))
            .as("finding 1 actual of 5 relevant should yield recall=0.2")
            .isCloseTo(0.2d, offset);

        assertThat(ComputeFunctions.recall(evenInts86204,intsBy3_369_12_15,5))
            .as("finding 0 (limited) actual of 5 relevant should yield recall=0.0")
            .isCloseTo(0.2d, offset);

        assertThat(ComputeFunctions.recall(evenInts86204,intsBy3_369_12_15,1))
            .as("finding 1 (limited) actual of 1 (limited) relevant should yield recall=1.0")
            .isCloseTo(1.0d, offset);

        assertThat(ComputeFunctions.recall(evenInts86204,intsBy3_369_12_15,2))
            .as("finding 1 (limited) actual of 2 (limited) relevant should yield recall=0.5")
            .isCloseTo(0.5d, offset);

        assertThat(ComputeFunctions.recall(evenInts86204,intsBy3_369_12_15,3))
            .as("finding 1 (limited) actual of 3 (limited) relevant should yield recall=0.3...")
            .isCloseTo(0.3333d, offset);

    }

    @Test
    void testPrecisionIntArrays() {
        assertThat(ComputeFunctions.precision(evenInts86204,intsBy3_693))
            .as("one of three results being relevant should yield precision=0.333")
            .isCloseTo(0.333,offset);
        assertThat(ComputeFunctions.precision(evenInts86204,lowInts01234))
            .as("three of five results being relevant should yield precision=0.6")
            .isCloseTo(0.6,offset);
        assertThat(ComputeFunctions.precision(evenInts86204,oddInts37195))
            .as("none of the results being relevant should yield precision=0.0")
            .isCloseTo(0.0,offset);
    }

    @Test
    public void sanityCheckRecallAndLimitRatio() {
        int[] hundo = IntStream.range(0,100).toArray();

        for (int i = 1; i < hundo.length; i++) {
            int[] partial=IntStream.range(0,i).toArray();
            int finalI = i;
            assertThat(ComputeFunctions.recall(hundo, partial, i))
                .as(() -> "for subset size " + finalI +", recall should be fractional/100")
                .isCloseTo((double)partial.length/(double)i,offset);

        }

    }

    @Test
    public void testReciprocalRank() {
        assertThat(ComputeFunctions.reciprocal_rank(intsBy3_369,highInts56789))
            .as("relevant results in rank 2 should yield RR=0.5")
            .isCloseTo(0.5d,offset);

        assertThat(ComputeFunctions.reciprocal_rank(highInts56789,lowInts01234))
            .as("no relevant results should yield RR=0.0")
            .isCloseTo(0.0d,offset);
    }

    @Test
    public void testIntegerIntersection() {
        int[] result = Intersections.find(lowInts01234,midInts45678);
        assertThat(result).isEqualTo(new int[]{4});
    }

    @Test
    public void testCountIntIntersection() {
        int result = Intersections.count(oddInts37195, ints12390);
        assertThat(result).isEqualTo(2L);
    }

    @Test
    public void tesTCountIntersectionDepth() {
        assertThat(Intersections.count(oddInts37195, ints12390,0)).isEqualTo(0);;
    }

    @Test
    public void testAP() {
        double ap1 = ComputeFunctions.average_precision(new int[]{1, 2, 3, 4, 5, 6}, new int[]{3, 11, 5, 12, 1});
        assertThat(ap1)
            .as("")
            .isCloseTo(0.755d,offset);

        double ap2 = ComputeFunctions.average_precision(ints12390, intsBy3_369);
        assertThat(ap2)
            .as("")
            .isCloseTo(0.833,offset);
    }


}
