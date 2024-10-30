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

package io.nosqlbench.virtdata.library.basics.shared.from_long.to_vector;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Normalize a vector.
 */
@ThreadSafeMapper
@Categories(Category.experimental)
public class NormalizeDoubleListVector implements Function<List<Double>,List<Double>> {
    @Override
    public List<Double> apply(List<Double> doubles) {
        ArrayList<Double> unit = new ArrayList<>(doubles.size());
        double accumulator = 0.0d;
        for (Double scalar : doubles) {
            accumulator+=scalar*scalar;
        }
        double scalarLen = Math.sqrt(accumulator);
        for (double scalarComponent : doubles) {
            unit.add(scalarComponent/scalarLen);
        }
        return unit;
    }
}
