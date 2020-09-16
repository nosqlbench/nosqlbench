package io.nosqlbench.virtdata.library.basics.shared.from_long.to_collection;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.api.bindings.VirtDataConversions;
import io.nosqlbench.virtdata.api.bindings.VirtDataFunctions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.function.LongFunction;

/**
 * Create a Map from a long input based on a set of provided key and value functions.
 * Any duplicate entries produced by the key functions are elided.
 *
 * As a 'Pair-wise' function, the size of the resulting collection is determined directly by the
 * number of provided element functions. Since this is a map, the functions come in pairs, each
 * even numbered function is a key function and each odd numbered function is the corresponding value function.
 *
 * As neither a 'Stepped' nor a 'Hashed' function, the input value used by each key and value function is the same
 * as that provided to the outer function.
 */
@Categories({Category.collections})
@ThreadSafeMapper
public class MapFunctions implements LongFunction<java.util.Map<Object,Object>> {

    private final List<LongFunction> valueFuncs;
    private final List<LongFunction> keyFuncs;
    private final int size;

    @Example({
            "MapFunctions(NumberNameToString(),NumberNameToString(),ToString(),ToString())",
            "Create a map of object values. Produces values like {'one':'one'1:1}."
    })
    public MapFunctions(Object... funcs) {
        this.keyFuncs = VirtDataConversions.getFunctions(2, 0, LongFunction.class, funcs);
        this.valueFuncs = VirtDataConversions.getFunctions(2,1,LongFunction.class, funcs);
        this.size = valueFuncs.size();
    }

    @Override
    public java.util.Map<Object,Object> apply(long value) {
        java.util.Map<Object,Object> map = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            Object keyObject = keyFuncs.get(i).apply(value);
            Object valueObject = valueFuncs.get(i).apply(value);
            map.put(keyObject,valueObject);
        }
        return map;
    }
}
