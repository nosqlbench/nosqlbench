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
 *
 */

package io.nosqlbench.loader.hdf.embedding;

public class FloatEmbeddingGenerator implements EmbeddingGenerator {

        @Override
        public float[][] generateEmbeddingFrom(Object o, int[] dims) {
            return switch (dims.length) {
                case 1 -> new float[][]{(float[]) o};
                case 2 -> (float[][]) o;
                case 3 -> flatten(o, dims);
                default -> throw new RuntimeException("unsupported embedding dimensionality: " + dims.length);
            };
        }

        private float[][] flatten(Object o, int[] dims) {
            float[][][] arr = (float[][][]) o;
            float[][] flat = new float[dims[0]][dims[1] * dims[2]];
            for (int i = 0; i < dims[0]; i++) {
                for (int j = 0; j < dims[1]; j++) {
                    for (int k = 0; k < dims[2]; k++) {
                        flat[i][j * dims[2] + k] = arr[i][j][k];
                    }
                }
            }
            return flat;
        }
}
