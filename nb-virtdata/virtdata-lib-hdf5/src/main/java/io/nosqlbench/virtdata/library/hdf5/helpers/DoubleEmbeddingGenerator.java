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

package io.nosqlbench.virtdata.library.hdf5.helpers;

import java.util.List;

public class DoubleEmbeddingGenerator implements EmbeddingGenerator {

        @Override
    public List<Float> generateFloatListEmbeddingFrom(Object o, int[] dims) {
        // in this case o will always be double[1][x]
        double[] vector = ((double[][]) o)[0];
        Float[] vector2 = new Float[vector.length];
        for (int i = 0; i < vector.length; i++) {
            vector2[i] = (float) vector[i];
        }
        return List.of(vector2);
    }

    @Override
    public float[] generateFloatArrayEmbeddingFrom(Object o, int[] dims) {
        double[] vector = ((double[][]) o)[0];
        float[] vector2 = new float[vector.length];
        for (int i = 0; i < vector.length; i++) {
            vector2[i] = (float) vector[i];
        }
        return vector2;
    }

    @Override
    public List<Long> generateLongListEmbeddingFrom(Object o, int[] dims) {
        double[] vector = ((double[][]) o)[0];
        Long[] vector2 = new Long[vector.length];
        for (int i = 0; i < vector.length; i++) {
            vector2[i] = (long) vector[i];
        }
        return List.of(vector2);
    }

    @Override
    public long[] generateLongArrayEmbeddingFrom(Object o, int[] dims) {
        double[] vector = ((double[][]) o)[0];
        long[] vector2 = new long[vector.length];
        for (int i = 0; i < vector.length; i++) {
            vector2[i] = (long) vector[i];
        }
        return vector2;
    }

    @Override
    public List<Integer> generateIntListEmbeddingFrom(Object o, int[] dims) {
        double[] vector = ((double[][]) o)[0];
        Integer[] vector2 = new Integer[vector.length];
        for (int i = 0; i < vector.length; i++) {
            vector2[i] = (int) vector[i];
        }
        return List.of(vector2);
    }

    @Override
    public int[] generateIntArrayEmbeddingFrom(Object o, int[] dims) {
        double[] vector = ((double[][]) o)[0];
        int[] vector2 = new int[vector.length];
        for (int i = 0; i < vector.length; i++) {
            vector2[i] = (int) vector[i];
        }
        return vector2;
    }

}
