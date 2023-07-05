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

package io.nosqlbench.datamappers.functions.to_cqlvector;

import com.datastax.oss.driver.api.core.data.CqlVector;
import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.Function;
import java.util.List;

/**
 * Convert the incoming object List, Number, or Array to a CqlVector
 * using {@link CqlVector#newInstance(Number...)}}. If any numeric value
 * is passed in, then it becomes the only component of a 1D vector.
 * Otherwise, the individual values are added as vector components.
 */
@ThreadSafeMapper
@Categories(Category.experimental)
public class ToCqlVector implements Function<Object, CqlVector> {
    @Override
    public CqlVector apply(Object object) {
        if (object instanceof List list) {
            if (list.size()==0) {
                return CqlVector.newInstance();
            }
            Class<?> componentType = list.get(0).getClass();
            if (componentType.equals(Float.TYPE)) {
                return CqlVector.newInstance(((List<Float>) list).toArray(new Float[list.size()]));
            } else if (componentType.equals(Double.TYPE)) {
                return CqlVector.newInstance(((List<Double>)list).toArray(new Double[list.size()]));
            } else if (componentType.equals(Long.TYPE)) {
                return CqlVector.newInstance(((List<Long>)list).toArray(new Long[list.size()]));
            } else if (componentType.equals(Integer.TYPE)) {
                return CqlVector.newInstance(((List<Integer>)list).toArray(new Integer[list.size()]));
            } else {
                throw new RuntimeException("Unable to convert List of " + componentType.getSimpleName() + " to a CqlVector");
            }
        } else {
            throw new RuntimeException("Unsupported input type for CqlVector: " + object.getClass().getCanonicalName());
        }
    }
}
