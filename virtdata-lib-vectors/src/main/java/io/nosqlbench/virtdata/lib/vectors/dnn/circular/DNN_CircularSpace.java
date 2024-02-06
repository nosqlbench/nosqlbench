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

import io.nosqlbench.virtdata.lib.vectors.dnn.types.DNN_IntDoubleVectorSpace;

public class DNN_CircularSpace implements DNN_IntDoubleVectorSpace {

    private final int dimensions;
    private final int count;

    public DNN_CircularSpace(int dimensions, int count) {
        this.dimensions = dimensions;
        this.count = count;
    }

    @Override
    public double[] vectorOfOrdinal(int ordinal) {
        return new double[0];
    }

    @Override
    public int ordinalOfVector(double[] vector) {
        return 0;
    }

    @Override
    public int[] neighborsOfOrdinal(int ordinal) {
        return new int[0];
    }
}
