package io.nosqlbench.virtdata.library.basics.shared.from_long.to_collection;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.api.bindings.VirtDataConversions;

import java.util.HashSet;
import java.util.List;
import java.util.function.LongFunction;

/**
 * Create a Set from a long input based on a set of provided functions. Any duplicate values are elided.
 *
 * As a 'Pair-wise' function, the size of the resulting collection is determined directly by the
 * number of provided element functions.
 *
 * As neither a 'Stepped' nor a 'Hashed' function, the input value used by each element function is the same
 * as that provided to the outer function.
 */
@Categories({Category.collections})
@ThreadSafeMapper
public class SetFunctions implements LongFunction<java.util.Set<Object>> {

    private final List<LongFunction> valueFuncs;
    private final int size;

    @Example({
            "SetFunctions(NumberNameToString(),NumberNameToString(),NumberNameToString())",
            "Create a list of object values of each function output. Produces values like ['one'], as each function produces the same value."
    })
    public SetFunctions(Object... funcs) {
        this.valueFuncs = VirtDataConversions.adaptFunctionList(funcs, LongFunction.class, Object.class);
        this.size = valueFuncs.size();
    }

    @Override
    public java.util.Set<Object> apply(long value) {
        java.util.Set<Object> set = new HashSet<>(size);
        for (int i = 0; i < size; i++) {
            set.add(valueFuncs.get(i).apply(value));
        }
        return set;
    }
}
