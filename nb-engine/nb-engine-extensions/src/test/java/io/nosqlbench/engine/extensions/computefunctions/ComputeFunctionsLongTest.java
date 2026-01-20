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
import org.junit.jupiter.api.Tag;

import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
class ComputeFunctionsLongTest {

    private final static Offset<Double> offset=Offset.offset(0.001d);
    private final static long[] longs_0to9 =new long[]{0,1,2,3,4,5,6,7,8,9};
    private final static long[] longs_3_7_1_9_5 = new long[]{3, 7, 1, 9, 5};
    private final static long[] longs_8_6_2_0_4 = new long[]{8, 6, 2, 0, 4};
    private final static long[] longs_0_1_2_3_4 = new long[]{0, 1, 2, 3, 4};
    private final static long[] longs_5_6_7_8_9 = new long[]{5, 6, 7, 8, 9};

    private final static long[] longs_3_6_9 = new long[]{3, 6, 9};
    private final static long[] longs_3_6_9_12_15 = new long[]{3, 6, 9, 12, 15};
    private final static long[] longs_6_9_3 = new long[]{6, 9, 3};
    private final static long[] longs_4_5_6_7_8 = new long[]{4, 5, 6, 7, 8};
    private final static long[] longs_1_2_3_9_0 = new long[]{1, 2, 3, 9, 0};

    @Test
    public void testNoIntersectionAtAnyK() {
        for (int k = 1; k < 5; k++) {
            assertThat(ComputeFunctions.recall(longs_8_6_2_0_4, longs_3_7_1_9_5, k)).as(
                "When no elements intersect, recall can never be > 0.0").isCloseTo(0.0d, offset);
        }
    }

    @Test
    public void testFullIdentityIntersectionAtAnyK() {
        for (int k = 1; k < 5; k++) {
            assertThat(ComputeFunctions.recall(longs_8_6_2_0_4, longs_8_6_2_0_4, k)).as(
                "When all elements intersect, recall can never be < 1.0").isCloseTo(1.0d, offset);
        }
    }

    @Test
    public void testProportionalIntersectionAtSomeK() {
        long[] image = new long[]{101,102,103,104,105};
        long[] gt = new long[]{34,30,12,9,37};
        for (int k = 1; k < 5; k++) {
            image[k] = gt[k];
            assertThat(ComputeFunctions.recall(image, gt, 5)).as(
                    "When some elements intersect ("+k+"/"+image.length+"), recall should be proportional")
                .isCloseTo((double) k/5, offset);
        }
    }


    @Test
    void testRecallLongArrays() {
        assertThat(ComputeFunctions.recall(longs_8_6_2_0_4, longs_3_7_1_9_5, 5))
            .as("finding 0 actual of any should yield recall=0.0")
            .isCloseTo(0.0d, offset);

        assertThat(ComputeFunctions.recall(longs_8_6_2_0_4, longs_3_6_9_12_15, 5))
            .as("finding 1 actual of 5 relevant should yield recall=0.2")
            .isCloseTo(0.2d, offset);

        assertThat(ComputeFunctions.recall(longs_8_6_2_0_4, longs_3_6_9_12_15,5))
            .as("finding 1 (limited) actual of 5 relevant should yield recall=0.0")
            .isCloseTo(0.2d, offset);
    }

    @Test
    void testPrecisionLongArrays() {
        assertThat(ComputeFunctions.precision(longs_8_6_2_0_4, longs_6_9_3))
            .as("one of three results being relevant should yield precision=0.333")
            .isCloseTo(0.333,offset);
        assertThat(ComputeFunctions.precision(longs_8_6_2_0_4, longs_0_1_2_3_4))
            .as("three of five results being relevant should yield precision=0.6")
            .isCloseTo(0.6,offset);
        assertThat(ComputeFunctions.precision(longs_8_6_2_0_4, longs_3_7_1_9_5))
            .as("none of the results being relevant should yield precision=0.0")
            .isCloseTo(0.0,offset);
    }

    @Test
    public void sanityCheckRecallAndLimitRatioLongs() {
        long[] hundo = LongStream.range(0,100).toArray();

        for (int i = 1; i < hundo.length; i++) {
            long[] partial=LongStream.range(0,i).toArray();
            int finalI = i;
            assertThat(ComputeFunctions.recall(hundo, partial, i))
                .as(() -> "for subset size " + finalI +", recall should be fractional/100")
                .isCloseTo((double)partial.length/(double)i,offset);
        }
    }

    @Test
    public void testReciprocalRankLongs() {
        assertThat(ComputeFunctions.reciprocal_rank(longs_3_6_9, longs_5_6_7_8_9))
            .as("relevant results in rank 2 should yield RR=0.5")
            .isCloseTo(0.5d,offset);

        assertThat(ComputeFunctions.reciprocal_rank(longs_5_6_7_8_9, longs_0_1_2_3_4))
            .as("no relevant results should yield RR=0.0")
            .isCloseTo(0.0d,offset);
    }

    @Test
    public void testCountLongIntersection() {
        long result = Intersections.count(longs_3_7_1_9_5, longs_1_2_3_9_0);
        assertThat(result).isEqualTo(3L);
    }

}
