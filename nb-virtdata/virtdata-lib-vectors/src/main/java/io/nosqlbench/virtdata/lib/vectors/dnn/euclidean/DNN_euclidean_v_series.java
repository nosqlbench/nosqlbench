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

import java.util.Arrays;
import java.util.function.LongFunction;

@ThreadSafeMapper
@Categories(Category.experimental)
public class DNN_euclidean_v_series implements LongFunction<float[][]> {

    private final int dimensions;
    private final long population;
    private final int k;

    public DNN_euclidean_v_series(int dimensions, long population, int k) {
        this.dimensions = dimensions;
        this.population = population;
        this.k = k;
    }

    @Override
    public float[][] apply(long value) {
        long nextInterval = value + k;
        if (nextInterval > population) {
            throw new RuntimeException("You can't generate a vector for ordinal " + value + " when your population is " + this.population);
        }
        int capacity = dimensions + k;
        float[] image = new float[capacity];
        for (int imgidx = 0; imgidx < capacity; imgidx++) {
            image[imgidx]=imgidx+value;
        }
        float[][] vectorSeq = new float[k][dimensions];
        for (int i = 0; i < vectorSeq.length; i++) {
            vectorSeq[i]=Arrays.copyOfRange(image,i,i+dimensions);
        }
        return vectorSeq;
    }
}
