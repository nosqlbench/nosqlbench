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

package io.nosqlbench.virtdata.library.basics.shared.vectors;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.shared.vectors.algorithms.CircleAlgorithm;

import java.util.List;
import java.util.function.LongFunction;

@Categories(Category.general)
@ThreadSafeMapper
public class CircleVectors implements LongFunction<List<Object>> {
    private final int circleCount;
    private final CircleAlgorithm algorithm;

    public CircleVectors(int circleCount, String algorithmClass) throws Exception {
        this.circleCount = circleCount;
        Object algo = Class.forName(algorithmClass).newInstance();
        if (!(algo instanceof CircleAlgorithm)) {
            throw new RuntimeException("The class '" + algorithmClass +
                "' does not implement CircleAlgorithm");
        }
        algorithm = (CircleAlgorithm) algo;
    }

    @Override
    public List<Object> apply(long value) {
        return algorithm.getVector((value % circleCount), circleCount);
    }

    public int getCircleCount() {
        return circleCount;
    }

    public CircleAlgorithm getAlgorithm() {
        return algorithm;
    }

}
