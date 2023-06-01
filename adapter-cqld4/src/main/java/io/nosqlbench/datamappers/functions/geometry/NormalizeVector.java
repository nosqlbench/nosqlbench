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
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_vector.NormalizeDoubleVectorList;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_vector.NormalizeFloatVectorList;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Normalize a vector in List<Number> form, calling the appropriate conversion function
 * depending on the component (Class) type of the incoming List values.
 */
@ThreadSafeMapper
@Categories(Category.experimental)
public class NormalizeVector implements Function<com.datastax.oss.driver.api.core.data.CqlVector ,List> {
    private final NormalizeDoubleVectorList ndv = new NormalizeDoubleVectorList();
    private final NormalizeFloatVectorList nfv = new NormalizeFloatVectorList();


    @Override
    public List apply(CqlVector cqlVector) {
        Iterable values = cqlVector.getValues();
        List<Object> list = new ArrayList<>();
        values.forEach(list::add);

        if (list.size()==0) {
            return List.of();
        } else if (list.get(0) instanceof Float) {
            List<Float> floats = new ArrayList<>();
            list.forEach(o -> floats.add((Float)o));
            return nfv.apply(floats);
        } else if (list.get(0) instanceof Double) {
            List<Double> doubles = new ArrayList<>();
            list.forEach(o -> doubles.add((Double) o));
            return ndv.apply(doubles);
        } else {
            throw new RuntimeException("Only Doubles and Floats are recognized.");
        }
    }
}
