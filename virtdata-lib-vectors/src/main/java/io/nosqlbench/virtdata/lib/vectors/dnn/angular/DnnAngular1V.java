/*
 * Copyright (c) 2023-2024 nosqlbench
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

package io.nosqlbench.virtdata.lib.vectors.dnn.angular;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.Arrays;
import java.util.function.LongFunction;

@ThreadSafeMapper
@Categories(Category.experimental)
public class DnnAngular1V implements LongFunction<float[]> {

    private final int d;
    private final long n;
    private final long m;

    /**
     * @param D
     *     Dimensions in each vector
     * @param N
     *     The number of vectors in the training set
     * @param M
     *     The modulo which is used to construct equivalence classes
     */
    public DnnAngular1V(int D, long N, long M) {
        d = D;
        n = N;
        m = M;
    }

    @Override
    public float[] apply(long i) {
        float[] vector = new float[d];
        Arrays.fill(vector, i + 1);
        vector[vector.length - 1] = (i + 1) * (i % m);
        return vector;
    }
}
