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

import java.util.HashMap;
import java.util.List;
import java.util.function.LongFunction;
import java.util.function.LongToIntFunction;

/**
 * Create a Map from a long input based on a set of provided key and value functions.
 * Any duplicate entries produced by the key functions are elided.
 *
 * As a 'Sized' function, the first argument is a function which determines the size of the resulting map.
 * Additional functions provided are used to generate the elements to add to the collection, as in the pair-wise
 * mode of {@link MapFunctions}. If the size is larger than the number of provided functions, the last provided
 * function is used repeatedly as needed. (respectively for key functions as well as value functions)
 *
 * As neither a 'Stepped' nor a 'Hashed' function, the input value used by each key and value function is the same
 * as that provided to the outer function.
 */
@Categories({Category.collections})
@ThreadSafeMapper
public class MapSized implements LongFunction<java.util.Map<Object,Object>> {

    private final List<LongFunction> valueFuncs;
    private final List<LongFunction> keyFuncs;
    private final LongToIntFunction sizeFunc;

    @Example({
            "MapSized(1, NumberNameToString(),NumberNameToString(),ToString(),ToString())",
            "Create a map of object values. Produces values like {'one':'one'1:1}."
    })
    public MapSized(Object sizeFunc, Object... funcs) {
        if (sizeFunc instanceof Number) {
            int size = ((Number) sizeFunc).intValue();
            this.sizeFunc = s -> size;
        } else {
            this.sizeFunc = VirtDataConversions.adaptFunction(sizeFunc, LongToIntFunction.class);
        }
        this.keyFuncs = VirtDataConversions.getFunctions(2, 0, LongFunction.class, funcs);
        this.valueFuncs = VirtDataConversions.getFunctions(2,1,LongFunction.class, funcs);
    }
    public MapSized(int size, Object... funcs) {
        this.sizeFunc = s -> size;
        this.keyFuncs = VirtDataConversions.getFunctions(2, 0, LongFunction.class, funcs);
        this.valueFuncs = VirtDataConversions.getFunctions(2,1,LongFunction.class, funcs);
    }

    @Override
    public java.util.Map<Object,Object> apply(long value) {
        int size = sizeFunc.applyAsInt(value);
        java.util.Map<Object,Object> map = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            int keySelector = Math.min(i, keyFuncs.size() - 1);
            int valSelector = Math.min(i, valueFuncs.size() -1);

            Object keyObject = keyFuncs.get(keySelector).apply(value);
            Object valueObject = valueFuncs.get(valSelector).apply(value);
            map.put(keyObject,valueObject);
        }
        return map;
    }
}
