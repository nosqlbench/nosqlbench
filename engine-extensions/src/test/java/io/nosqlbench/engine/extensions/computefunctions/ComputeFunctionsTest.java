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

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class ComputeFunctionsTest {

    private final static Offset<Double> offset=Offset.offset(0.001d);
    private final static int[] allInts =new int[]{0,1,2,3,4,5,6,7,8,9};
    private final static int[] oddInts37195 = new int[]{3,7,1,9,5};
    private final static int[] evenInts86204 = new int[]{8,6,2,0,4};
    private final static int[] lowInts01234 = new int[]{0,1,2,3,4};
    private final static int[] highInts56789 = new int[]{5,6,7,8,9};

    private final static int[] intsBy3_369 = new int[]{3,6,9};
    private final static int[] intsBy3_693 = new int[]{3,6,9};
    @Test
    void testRecallIntArrays() {
        assertThat(ComputeFunctions.recall(evenInts86204,oddInts37195))
            .as("finding 0 actual of any should yield recall=0.0")
            .isCloseTo(0.0d, offset);

        assertThat(ComputeFunctions.recall(evenInts86204,intsBy3_369))
            .as("finding 1 actual of 5 relevant should yield recall=0.2")
            .isCloseTo(0.2d, offset);

        assertThat(ComputeFunctions.recall(evenInts86204,intsBy3_369,1))
            .as("finding 0 (limited) actual of 5 relevant should yield recall=0.0")
            .isCloseTo(0.0d, offset);
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

        for (int i = 0; i < hundo.length; i++) {
            int[] partial=IntStream.range(0,i).toArray();
            int finalI = i;
            assertThat(ComputeFunctions.recall(hundo, partial))
                .as(() -> "for subset size " + finalI +", recall should be fractional/100")
                .isCloseTo((double)partial.length/(double)hundo.length,offset);
            assertThat(ComputeFunctions.recall(hundo, hundo, i))
                .as(() -> "for full intersection, limit " + finalI +" (K) recall should be fractional/100")
                .isCloseTo((double)partial.length/(double)hundo.length,offset);
        }

    }
}
