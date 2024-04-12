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

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.api.bindings.VirtDataConversions;

import java.util.List;
import java.util.function.LongFunction;

/**
 * Create a new CqlVector from a composed function, where the inner function
 * is a list generation function that must take a long (cycle) input.
 */
@ThreadSafeMapper
@Categories(Category.HOF)
public class CqlVector implements LongFunction<com.datastax.oss.driver.api.core.data.CqlVector> {

    private final LongFunction<List<?>> func;

    @Example({"CqlVector(ListSized(2,HashedRange(0.2f, 5.0f)", "Create a 2-component vector with the given range of values."})
    public CqlVector(Object func) {
        this.func = VirtDataConversions.adaptFunction(func, LongFunction.class, List.class);
    }

    @Override
    public com.datastax.oss.driver.api.core.data.CqlVector apply(long cycle) {
        List components = func.apply(cycle);
        com.datastax.oss.driver.api.core.data.CqlVector vector = com.datastax.oss.driver.api.core.data.CqlVector.newInstance(components);
        return vector;
    }
}
