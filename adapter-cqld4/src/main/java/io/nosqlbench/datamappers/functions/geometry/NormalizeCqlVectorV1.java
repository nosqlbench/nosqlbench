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
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_vector.NormalizeDoubleListVector;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_vector.NormalizeFloatListVector;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Normalize a vector in List<Number> form, calling the appropriate conversion function
 * depending on the component (Class) type of the incoming List values.
 */
@ThreadSafeMapper
@Categories(Category.experimental)
public class NormalizeCqlVectorV1 implements Function<CqlVector, CqlVector> {
    private final NormalizeDoubleListVector ndv = new NormalizeDoubleListVector();
    private final NormalizeFloatListVector nfv = new NormalizeFloatListVector();

    @Override
    public CqlVector apply(CqlVector cqlVector) {

        CqlVector.Builder builder = CqlVector.builder();
        Iterable values = cqlVector.getValues();
        List<Object> list = new ArrayList<>();
        for (Object element : list) {
            list.add(element);
        }

        if (list.isEmpty()) {
        } else if (list.get(0) instanceof Float) {
            List<Float> srcfloats = new ArrayList<>(list.size());
            list.forEach(o -> srcfloats.add((Float) o));
            List<Float> floats = nfv.apply(srcfloats);
            for (Float fv : floats) {
                builder.add(fv);
            }
        } else if (list.get(0) instanceof Double) {
            List<Double> srcDoubles = new ArrayList<>();
            list.forEach(o -> srcDoubles.add((Double) o));
            List<Double> doubles = ndv.apply(srcDoubles);
            for (Double dv : doubles) {
                builder.add(dv);
            }
        } else {
            throw new RuntimeException("Only Doubles and Floats are recognized.");
        }
        return builder.build();
    }
}
