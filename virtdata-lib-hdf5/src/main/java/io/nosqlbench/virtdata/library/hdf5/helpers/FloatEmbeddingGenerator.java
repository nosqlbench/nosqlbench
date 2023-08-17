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

public class FloatEmbeddingGenerator implements EmbeddingGenerator {

        @Override
        public List<Float> generateListEmbeddingFrom(Object o, int[] dims) {
            // in this case o will always be float[1][x]
            float[] vector = ((float[][]) o)[0];
            Float[] vector2 = new Float[vector.length];
            for (int i = 0; i < vector.length; i++) {
                vector2[i] = vector[i];
            }
            return List.of(vector2);
        }

    @Override
    public float[] generateArrayEmbeddingFrom(Object o, int[] dims) {
            return ((float[][]) o)[0];
    }

}