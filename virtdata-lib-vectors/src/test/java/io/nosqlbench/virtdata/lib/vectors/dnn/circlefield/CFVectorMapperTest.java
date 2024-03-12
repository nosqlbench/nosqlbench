/*
 * Copyright (c) 2024 nosqlbench
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

package io.nosqlbench.virtdata.lib.vectors.dnn.circlefield;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CFVectorMapperTest {

    private final static CFVectorSpace vs32 = new CFVectorSpace(32);
    private final static CFVectorMapper vm32 = new CFVectorMapper(vs32);

    private final static CFVectorSpace vs16bits = new CFVectorSpace(65536);
    private final static CFVectorMapper vm16bits = new CFVectorMapper(vs16bits);


    @Test
    public void testBasicIndex() {
        CFVectorSpace vs32 = new CFVectorSpace(32);
        CFVectorMapper vm32 = new CFVectorMapper(vs32);

        double[] v_1_0 = vm32.vectorForOrdinal(0);
        isCloseTo(v_1_0,new double[]{1.0d,0.0d},Offset.offset(0.000001d));

        double[] v_n1_0 = vm32.vectorForOrdinal(1);
        isCloseTo(v_n1_0,new double[]{-1.0d,0.0d},Offset.offset(0.000001d));
    }

    @Test
    public void testNeighborhoods() {
        int[] neighbors = vm16bits.neighbors(0, 10);
//        assertThat(neighbors).containsExactly(new int[]{32768});
        // TODO continue here



    }


    private void isCloseTo(double[] values, double[] expected, Offset<Double> offset) {
        for (int i = 0; i < expected.length; i++) {
            assertThat(values[i]).isCloseTo(expected[i],offset);
        }
    }

}
