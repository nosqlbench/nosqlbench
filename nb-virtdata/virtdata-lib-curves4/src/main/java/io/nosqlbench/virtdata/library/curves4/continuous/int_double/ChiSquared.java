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
import org.apache.commons.statistics.distribution.ChiSquaredDistribution;

/**
 * @see <a href="https://en.wikipedia.org/wiki/Chi-squared_distribution">Wikipedia: Chi-squared distribution</a>
 *
 * @see <a href="https://commons.apache.org/proper/commons-statistics/commons-statistics-distribution/apidocs/org/apache/commons/statistics/distribution/ChiSquaredDistribution.html">Commons JavaDoc: ChiSquaredDistribution</a>
 */
@ThreadSafeMapper
@Categories({Category.distributions})
public class ChiSquared extends IntToDoubleContinuousCurve {
    public ChiSquared(double degreesOfFreedom, String... mods) {
        super(ChiSquaredDistribution.of(degreesOfFreedom), mods);
    }
}
