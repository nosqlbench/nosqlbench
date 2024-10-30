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

package io.nosqlbench.virtdata.library.hdf5.helpers;

import java.util.List;

public interface EmbeddingGenerator {
    List<Float> generateFloatListEmbeddingFrom(Object o, int[] dims);

    float[] generateFloatArrayEmbeddingFrom(Object o, int[] dims);

    List<Long> generateLongListEmbeddingFrom(Object data, int[] dims);

    long[] generateLongArrayEmbeddingFrom(Object data, int[] dims);

    List<Integer> generateIntListEmbeddingFrom(Object data, int[] dims);

    int[] generateIntArrayEmbeddingFrom(Object data, int[] dims);
}
