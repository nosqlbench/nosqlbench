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

package io.nosqlbench.virtdata.lib.vectors.dnn.euclidean;

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
public class DNN_euclidean_neighbors implements IntFunction<int[]> {

    private final int D;
    private final int N;
    private final int k;

    /**
     * @param k
     *     The size of neighborhood
     * @param N
     *     The number of total vectors, necessary for boundary conditions of defined vector
     * @param D
     *     Number of dimensions in each vector
     */
    public DNN_euclidean_neighbors(int k, int N, int D) {
        this.D = D;
        this.N = N;
        this.k = k;
    }

    /**
     * <P>Compute neighbor indices with a (hopefully) fast implementation. There are surely some simplifications to be
     * made in the functions below, but even in the current form it avoids a significant number of branches.</P>
     *
     * <P>This code is not as simple as it could be. It was built more for speed than simplicity since it will be a hot
     * spot for testing. The unit tests for this are essential.</P>
     *
     * <P>The method is thus:
     * <OL>
     * <LI>Determine the sections of the neighborhood which aren't subject to boundary conditions,
     * starting at the central vector (the index of the query vector).</LI>
     * <LI>Layer these in rank order using closed-form index functions.</LI>
     * <LI>Layer in any zero-boundary values which were deferred from above.</LI>
     * <LI>Layer in an N-boundary values which were deferred from above.</LI>
     * </OL>
     * </P>
     *
     * <P>The boundary conditions for zero and N are mutually exclusive. Even though there is some amount of
     * ranging and book keeping in this approach, it should make the general case more stable, especially
     * when there are many dimensions and many neighbors.
     * </P>
     *
     * @param value
     *     the function argument, or the index of the query vector for the DNN addressing scheme
     * @return A ranked neighborhood of vector indices, using the DNN addressing scheme
     */
    @Override
    public int[] apply(int value) {
        value = Math.min(Math.max(0,value),N-1);
        int[] indices = new int[k];

        int leftBoundary = (value << 1) + 1;
        int rightBoundary = ((N - (value + 1)) << 1) + 1;
        int insideNeighbors = Math.min(k, Math.min(leftBoundary, rightBoundary));
        for (int i = 0; i < insideNeighbors; i++) {
            // Leave this here as an explainer, please
            // int sign = ((((i + 1) & 1) << 1) - 1); // this gives us -1 or +1 depending on odd or even, and is inverted
            // int offset = ((i + 1)>>1); // half rounded down, shifted by 1
            // offset *= sign;
            // int v = value + (((((i + 1) & 1) << 1) - 1) * ((i + 1) >> 1));
            indices[i] = value + (((((i + 1) & 1) << 1) - 1) * ((i + 1) >> 1));
        }
        int leftFill = Math.max(0, k - leftBoundary);
        // TODO: Evaluate optimization from Dave2Wave for reducing additions
        for (int i = 0; i < leftFill; i++) {
            indices[insideNeighbors + i] = insideNeighbors + i;
        }
        int rightFill = Math.max(0, k - rightBoundary);
        for (int i = 0; i < rightFill; i++) {
            indices[insideNeighbors + i] = (N - 1) - (insideNeighbors + i);
        }
        return indices;
    }
}
