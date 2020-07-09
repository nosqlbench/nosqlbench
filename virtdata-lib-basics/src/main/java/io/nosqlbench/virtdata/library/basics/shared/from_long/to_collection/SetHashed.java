package io.nosqlbench.virtdata.library.basics.shared.from_long.to_collection;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.api.bindings.VirtDataConversions;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.Hash;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;
import java.util.function.LongFunction;
import java.util.function.LongUnaryOperator;

/**
 * Create a Set from a long input based on a set of provided functions.
 *
 * As a 'Pair-wise' function, the size of the resulting collection is determined directly by the
 * number of provided element functions, assuming no duplicate values.
 *
 * As a 'Hashed' function, the input value is hashed again before being used by each element function.
 */
@Categories({Category.collections})
@ThreadSafeMapper
public class SetHashed implements LongFunction<java.util.Set<Object>> {

    private final List<LongFunction> valueFuncs;
    private final int size;
    private final Hash hasher = new Hash();

    @Example({
            "SetHashed(ToString(), WeightedStrings('text:1'))",
            "Create a hash list of object values of each function output, like ['2945182322382062539','text']"
    })
    public SetHashed(Object... funcs) {
        this.valueFuncs = VirtDataConversions.adaptList(funcs, LongFunction.class, Object.class);
        this.size = valueFuncs.size();
    }


    @Override
    public java.util.Set<Object> apply(long value) {
        long hash = value;
        java.util.Set<Object> list = new HashSet<>(size);
        for (int i = 0; i < size; i++) {
            int selector = Math.min(i, valueFuncs.size() - 1);
            LongFunction<?> func = valueFuncs.get(selector);
            hash = hasher.applyAsLong(value);
            list.add(func.apply(hash));
        }
        return list;
    }
}
