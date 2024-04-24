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

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * Normalize a vector in List<Number> form, calling the appropriate conversion function
 * depending on the component (Class) type of the incoming List values.
 */
@ThreadSafeMapper
@Categories(Category.experimental)
public class NormalizeCqlVector implements Function<CqlVector, CqlVector> {
    private final NormalizeDoubleListVector ndv = new NormalizeDoubleListVector();
    private final NormalizeFloatListVector nfv = new NormalizeFloatListVector();

    @Override
    public CqlVector apply(CqlVector cqlVector) {
        double[] vals = new double[cqlVector.size()];
        double accumulator= 0.0d;
        for (int i = 0; i < vals.length; i++) {
            vals[i]=cqlVector.get(i).doubleValue();
            accumulator+=vals[i]*vals[i];
        }
        double factor = 1.0d/Math.sqrt(Arrays.stream(vals).map(d -> d * d).sum());

        if (cqlVector.get(0) instanceof Float) {
            List<Float> list = Arrays.stream(vals).mapToObj(d -> Float.valueOf((float) (d * factor))).toList();
            return CqlVector.newInstance(list);
        } else if (cqlVector.get(0) instanceof Double) {
            List<Double> list = Arrays.stream(vals).mapToObj(d -> Double.valueOf((float) (d * factor))).toList();
            return CqlVector.newInstance(list);
        } else {
            throw new RuntimeException(NormalizeCqlVector.class.getCanonicalName()+ " only supports Double and Float type");
        }
    }
}
