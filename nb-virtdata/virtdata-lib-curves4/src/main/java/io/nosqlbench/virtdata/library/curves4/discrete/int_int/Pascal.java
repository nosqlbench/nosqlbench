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

package io.nosqlbench.virtdata.library.curves4.discrete.int_int;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.PascalDistribution;

/**
 * @see <a href="https://commons.apache.org/proper/commons-statistics/commons-statistics-distribution/apidocs/org/apache/commons/statistics/distribution/PascalDistribution.html">Commons JavaDoc: PascalDistribution</a>
 *
 * @see <a href="https://en.wikipedia.org/wiki/Negative_binomial_distribution">Wikipedia: Negative binomial distribution</a>
 *
 */
@ThreadSafeMapper
@Categories({Category.distributions})
public class Pascal extends IntToIntDiscreteCurve {
    public Pascal(int r, double p, String... modslist) {
        super(PascalDistribution.of(r, p), modslist);
    }
}
