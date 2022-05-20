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
import java.util.function.LongToIntFunction;

/**
 * Create a Set from a long input based on a set of provided functions.
 *
 * As a 'Sized' function, the first argument is a function which determines the size of the resulting set.
 * Additional functions provided are used to generate the elements to add to the collection. If the size
 * is larger than the number of provided functions, the last provided function is used repeatedly as needed.
 *
 * As neither a 'Stepped' nor a 'Hashed' function, the input value used by each element function is the same
 * as that provided to the outer function.
 */
@Categories({Category.collections})
@ThreadSafeMapper
public class SetSized implements LongFunction<java.util.Set<Object>> {

    private final List<LongFunction> valueFuncs;
    private final LongToIntFunction sizeFunc;

    @Example({
            "SetSized(FixedValue(5), NumberNameToString(), WeightedStrings('text:1'))",
            "Create a sized set of object values, like ['one','text'], because 'text' is duplicated 4 times"
    })
    public SetSized(Object sizeFunc, Object... funcs) {
        if (sizeFunc instanceof Number) {
            int size = ((Number)sizeFunc).intValue();
            this.sizeFunc = s -> size;
        } else {
            this.sizeFunc = VirtDataConversions.adaptFunction(sizeFunc, LongToIntFunction.class);
        }
        this.valueFuncs = VirtDataConversions.adaptFunctionList(funcs, LongFunction.class, Object.class);
    }
    public SetSized(int size, Object... funcs) {
        this.sizeFunc = s -> size;
        this.valueFuncs = VirtDataConversions.adaptFunctionList(funcs, LongFunction.class, Object.class);
    }

    @Override
    public java.util.Set<Object> apply(long value) {
        int size = sizeFunc.applyAsInt(value);
        java.util.Set<Object> list = new HashSet<>(size);
        for (int i = 0; i < size; i++) {
            int selector = Math.min(i, valueFuncs.size() - 1);
            LongFunction<?> func = valueFuncs.get(selector);
            list.add(func.apply(value));
        }
        return list;
    }
}
