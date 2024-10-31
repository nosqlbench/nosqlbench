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
public class NormalizeFloatListVector implements Function<List<Float>,List<Float>> {
    @Override
    public List<Float> apply(List<Float> floats) {
        ArrayList<Float> unit = new ArrayList<>(floats.size());
        float accumulator = 0.0f;
        for (float scalar : floats) {
            accumulator+=scalar*scalar;
        }
        float scalarLen = (float) Math.sqrt(accumulator);
        for (float scalarComponent : floats) {
            unit.add(scalarComponent/scalarLen);
        }
        return unit;
    }
}
