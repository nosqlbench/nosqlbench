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
 * using {@link CqlVector.Builder#add(Object[])}}. If any numeric value
 * is passed in, then it becomes the only component of a 1D vector.
 * Otherwise, the individual values are added as vector components.
 */
@ThreadSafeMapper
@Categories(Category.experimental)
public class ToCqlVector implements Function<Object, CqlVector> {

    @Override
    public CqlVector apply(Object object) {
        Object[] ary = null;
        if (object instanceof List list) {
            ary = list.toArray();
        } else if (object instanceof Number number) {
            ary = new Object[]{number.floatValue()};
        } else if (object.getClass().isArray()) {
            ary = (Object[]) object;
        } else {
            throw new RuntimeException("Unsupported input type for CqlVector: " + object.getClass().getCanonicalName());
        }
        CqlVector.Builder vbuilder = CqlVector.builder();
        vbuilder.add(ary);
        return vbuilder.build();
    }
}
