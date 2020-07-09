package io.nosqlbench.virtdata.library.basics.shared.from_long.to_collection;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.api.bindings.VirtDataConversions;

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
 * As a 'Stepped' function, the input value is incremented before being used by each element function.
 */
@Categories({Category.collections})
@ThreadSafeMapper
public class SetStepped implements LongFunction<java.util.Set<Object>> {

    private final List<LongFunction> valueFuncs;
    private final int size;

    @Example({
        "SetStepped(NumberNameToString(),NumberNameToString())",
        "Create a list of ['one','two']"
    })
    public SetStepped(Object... funcs) {
        this.valueFuncs = VirtDataConversions.adaptList(funcs, LongFunction.class, Object.class);
        this.size = valueFuncs.size();
    }

    @Override
    public java.util.Set<Object> apply(long value) {
        java.util.Set<Object> list = new HashSet<>(size);
        for (int i = 0; i < size; i++) {
            int selector = Math.min(i, valueFuncs.size() - 1);
            LongFunction<?> func = valueFuncs.get(selector);
            list.add(func.apply(value+i));
        }
        return list;
    }
}
