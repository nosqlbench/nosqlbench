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

package io.nosqlbench.virtdata.lib.vectors.dnn.circular;

import io.nosqlbench.virtdata.lib.vectors.util.BitFields;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class CircularPartitionerTest {

    @Test
    public void testMsbPositions() {
        assertThat(BitFields.getMsbPosition(1)).isEqualTo(1);
        assertThat(BitFields.getMsbPosition(2)).isEqualTo(2);
        assertThat(BitFields.getMsbPosition(7)).isEqualTo(3);
        assertThat(BitFields.getMsbPosition(8)).isEqualTo(4);
        assertThat(BitFields.getMsbPosition(Integer.MAX_VALUE)).isEqualTo(31);
    }

    @Test
    public void assertBoundaryErrors() {
        assertThat(BitFields.getMsbPosition(0)).isEqualTo(0);
        assertThrows(RuntimeException.class, () -> BitFields.getMsbPosition(-1));
    }

    private int[] remap(CircularPartitioner cp, int[] inputs) {
        int[] outputs = new int[inputs.length];
        for (int i = 0; i < inputs.length; i++) {
            outputs[i]=cp.ordinalToOffset(inputs[i]);
        }
        return outputs;
    }

//    private double[][] ordinalsToVecs(CircularPartitioner cp, int[] ordinals) {
//        double[][] vecs = new double[ordinals.length][];
//        for (int i = 0; i < ordinals.length; i++) {
//            int intOffset = cp.ordinalToOffset(ordinals[i]);
//            double unitInterval = cp.unitIntervalOf(intOffset);
//            vecs[i]=cp.vecOnCircle(unitInterval);
//        }
//        return vecs;
//    }
    @Test
    public void assertOrdMapping() {
        CircularPartitioner cp16 = new CircularPartitioner(16);
        int[] ordinals = range(0,16);
        int[] remapped = remap(cp16,
            ordinals
        );
        assertThat(remapped).containsExactly(
            new int[]{0,8,4,12,2,6,10,14,1,3,5,7,9,11,13,15}
        );

        double[] unitLens = this.intOffsetsToUnitLen(cp16, remapped);
        double[][] vecs = this.unitLensToVecs(cp16, unitLens);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ordinals.length; i++) {
            sb.append("O:").append(i)
                .append(" R:").append(remapped[i])
                .append(" U:").append(String.format("%3.3f",unitLens[i]))
                .append(" v:").append(Arrays.toString(vecs[i])).append("\n");
        }
        System.out.println(sb);

    }

    private int[] range(int lowestIncluded, int highestExcluded) {
        int[] ints = new int[highestExcluded - lowestIncluded];
        for (int i = 0; i < ints.length; i++) {
            ints[i]=lowestIncluded+i;
        }
        return ints;
    }

    private double[][] unitLensToVecs(CircularPartitioner cp16, double[] unitLens) {
        double[][] vecs = new double[unitLens.length][];
        for (int i = 0; i < unitLens.length; i++) {
            vecs[i]= cp16.vecOnCircle(unitLens[i]);
        }
        return vecs;
    }

    private double[] intOffsetsToUnitLen(CircularPartitioner cp, int[] remapped) {
        double[] unitLens = new double[remapped.length];
        for (int i = 0; i < remapped.length; i++) {
            unitLens[i]=cp.unitIntervalOf(remapped[i]);
        }
        return unitLens;
    }

    @Test
    public void testOrdinalFractions() {
        CircularPartitioner cp16 = new CircularPartitioner(16);
        int[] ordinals = range(0,16);
        int[][] fractions = new int[ordinals.length][];
        for (int i = 0; i <ordinals.length; i++) {
            int ord = ordinals[i];
            fractions[i]=cp16.ordinalToFraction(ord);
        }
        assertThat(fractions[0]).isEqualTo(new int[]{0,16});
        assertThat(fractions[7]).isEqualTo(new int[]{14,16});
        assertThat(fractions[15]).isEqualTo(new int[]{15,16});
    }
}
