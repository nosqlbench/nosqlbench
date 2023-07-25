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

package io.nosqlbench.virtdata.library.basics.shared.vectors.primitive;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

/**
 * Precompute the interior double[] values to use as a LUT.
 */
@ThreadSafeMapper
@Categories(Category.experimental)
public class DoubleArrayCache extends VectorSequence {

    private final VectorSequence function;
    private final double[][] cache;

    public DoubleArrayCache(VectorSequence function) {
        super(function.getCardinality());

        this.function=function;
        if (function.getCardinality()>1E10) {
            throw new RuntimeException("you are trying to pre-compute and cache " + function.getCardinality() + " elements. Too many! Compute instead without caching.");
        }
        int size = (int)function.getCardinality();
        this.cache = new double[size][];
        for (int idx = 0; idx < cache.length; idx++) {
            cache[idx]=function.apply(idx);
        }
    }

    @Override
    public long getDimensions() {
        return function.getDimensions();
    }

    @Override
    public double[] apply(long value) {
        return cache[(int)(value % cache.length)];
    }
}
