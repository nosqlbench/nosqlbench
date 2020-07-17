package io.nosqlbench.virtdata.library.basics.shared.from_long.to_collection;

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
 * As a 'Stepped' function, the input value is incremented before being used by each key or value function.
 */
@Categories({Category.collections})
@ThreadSafeMapper
public class MapSizedStepped implements LongFunction<java.util.Map<Object,Object>> {

    private final List<LongFunction> valueFuncs;
    private final List<LongFunction> keyFuncs;
    private final LongToIntFunction sizeFunc;

    @Example({
            "MapSizedStepped(1, NumberNameToString(),NumberNameToString())",
            "Create a map of object values. Produces values like {'one':'one'1:1}."
    })
    public MapSizedStepped(Object sizeFunc, Object... funcs) {
        this.sizeFunc = VirtDataConversions.adaptFunction(sizeFunc, LongToIntFunction.class);
        this.keyFuncs = VirtDataConversions.getFunctions(2, 0, LongFunction.class, funcs);
        this.valueFuncs = VirtDataConversions.getFunctions(2,1, LongFunction.class, funcs);
    }

    @Override
    public java.util.Map<Object,Object> apply(long value) {
        int size = sizeFunc.applyAsInt(value);
        java.util.Map<Object,Object> map = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            int keySelector = Math.min(i, keyFuncs.size() - 1);
            int valSelector = Math.min(i, valueFuncs.size() -1);

            Object keyObject = keyFuncs.get(keySelector).apply(value+i);
            Object valueObject = valueFuncs.get(valSelector).apply(value+i);
            map.put(keyObject,valueObject);
        }
        return map;
    }
}
