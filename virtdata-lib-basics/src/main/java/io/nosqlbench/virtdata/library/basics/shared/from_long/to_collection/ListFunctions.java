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

import java.util.ArrayList;
import java.util.function.LongFunction;

/**
 * Create a List from a long input based on a set of provided functions.
 *
 * As a 'Pair-wise' function, the size of the resulting collection is determined directly by the
 * number of provided element functions.
 *
 *  As neither a 'Stepped' nor a 'Hashed' function, the input value used by each element function is the same
 *  as that provided to the outer function.
 */
@Categories({Category.collections})
@ThreadSafeMapper
public class ListFunctions implements LongFunction<java.util.List<Object>> {

    private final java.util.List<LongFunction> valueFuncs;
    private final int size;

    @Example({
            "ListFunctions(NumberNameToString(),NumberNameToString(),NumberNameToString())",
            "Create a list of object values of each function output. Produces values like ['one','one','one']"
    })
    public ListFunctions(Object... funcs) {
        this.valueFuncs = VirtDataConversions.adaptFunctionList(funcs, LongFunction.class, Object.class);
        this.size = valueFuncs.size();
    }

    @Override
    public java.util.List<Object> apply(long value) {
        java.util.List<Object> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(valueFuncs.get(i).apply(value));
        }
        return list;
    }
}
