/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.virtdata.library.curves4.continuous.int_double;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import org.apache.commons.math4.legacy.distribution.EmpiricalDistribution;

/**
 * Always yields the same value.
 *
 * @see <a href="https://commons.apache.org/proper/commons-statistics/commons-statistics-distribution/apidocs/org/apache/commons/statistics/distribution/ConstantContinuousDistribution.html">Commons JavaDoc: ConstantContinuousDistribution</a>
 *
 */
@ThreadSafeMapper
@Categories({Category.distributions})
public class ConstantContinuous extends IntToDoubleContinuousCurve {
    public ConstantContinuous(double value, String... mods) {
        super(EmpiricalDistribution.from(1, new double[]{value}), mods);
    }
}
