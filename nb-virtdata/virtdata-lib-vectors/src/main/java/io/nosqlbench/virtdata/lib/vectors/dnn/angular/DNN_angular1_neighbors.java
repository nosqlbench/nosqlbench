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

import java.util.function.IntFunction;

/**
 * Compute the indices of the neighbors of a given v using DNN mapping.
 * To avoid ambiguity on equidistant neighbors, odd neighborhood sizes are preferred.
 */
@ThreadSafeMapper
@Categories(Category.experimental)
public class DNN_angular1_neighbors implements IntFunction<int[]> {

    private final int N;
    private final int k;
    private final int modulus;

    /**
     * @param k
     *     The size of neighborhood
     * @param N
     *     The number of total vectors, necessary for boundary conditions of defined vector
     * @param modulus
     *     The modulus used during training of angular1 data; this corresponds to how periodically we cycle back
     *     to vectors with the same angle (hence have angular distance zero between them)
     */
    public DNN_angular1_neighbors(int k, int N, int modulus) {
        if (modulus <= 0) {
            throw new IllegalArgumentException(
                String.format(
                    "Invalid parameters: modulus=%d. modulus is required to be positive.",
                    modulus
                )
            );
        }
        // need to ensure each of the modulus clusters has size >= k, so that top-k nearest neighbors don't
        // spill to another cluster with non-zero angle
        if (k * modulus > N) {
            throw new IllegalArgumentException(
                String.format(
                    "Invalid parameters: N=%d, k=%d, modulus=%d. Vectors in a cluster = N / modulus >= k.",
                    N, k, modulus
                )
            );
        }
        this.N = N;
        this.k = k;
        this.modulus = modulus;
    }

    /**
     * @param value
     *     the function argument, or the index of the query vector for the DNN addressing scheme
     * @return A ranked neighborhood of vector indices, using the DNN addressing scheme
     */
    @Override
    public int[] apply(int value) {
        // we created modulus clusters of our N vectors, of size N/modulus or N/modulus + 1
        // (the latter case when modulus does not evenly divide N, and we get remainder)
        int div = N / modulus;
        int mod = N % modulus;
        int cycleResidueClass = value % modulus;
        // handle case of extra neighbor in the same cluster
        if (cycleResidueClass < mod) {
            div += 1;
        }
        int[] indices = new int[div];
        int currIdx = cycleResidueClass;
        for (int i = 0; i < div; i++) {
            indices[i] = currIdx;
            currIdx += modulus;
        }
        return indices;
    }
}
