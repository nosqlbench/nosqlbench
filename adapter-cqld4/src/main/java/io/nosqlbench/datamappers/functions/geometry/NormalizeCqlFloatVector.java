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

package io.nosqlbench.datamappers.functions.geometry;

import com.datastax.oss.driver.api.core.data.CqlVector;
import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_vector.NormalizeFloatListVector;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Normalize a vector in {@link CqlVector<Float>} form. This presumes that the input type is
 * Float, since we lose the type bounds on what is contained in the CQL type. If this doesn't match,
 * then you will arbitrarily increase your storage cost, or otherwise haven truncation errors
 * in your values.
 */
@ThreadSafeMapper
@Categories(Category.experimental)
public class NormalizeCqlFloatVector implements Function<CqlVector<? extends Number>, CqlVector<? extends Number>> {
    private final NormalizeFloatListVector nfv = new NormalizeFloatListVector();

    @Override
    public CqlVector apply(CqlVector<? extends Number> cqlVector) {
        int size = cqlVector.size();
        final List<Float> newVector = new ArrayList<>(size);
        cqlVector.forEach(v -> newVector.add(v.floatValue()));
        List<Float> normalized = nfv.apply(newVector);
        return CqlVector.newInstance(normalized);
    }
}
