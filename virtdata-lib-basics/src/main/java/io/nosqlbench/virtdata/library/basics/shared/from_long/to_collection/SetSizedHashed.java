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
import java.util.function.LongToIntFunction;
import java.util.function.LongUnaryOperator;

/**
 * Create a Set from a long input based on a set of provided functions.
 *
 * As a 'Sized' function, the first argument is a function which determines the size of the resulting set.
 * Additional functions provided are used to generate the elements to add to the collection. If the size
 * is larger than the number of provided functions, the last provided function is used repeatedly as needed.
 *
 *  As a 'Hashed' function, the input value is hashed again before being used by each element function.
 */
@Categories({Category.collections})
@ThreadSafeMapper
public class SetSizedHashed implements LongFunction<java.util.Set<Object>> {

    private final List<LongFunction> valueFuncs;
    private final Hash hasher = new Hash();
    private final LongToIntFunction sizeFunc;

    @Example({
        "SetSizedHashed(FixedValue(5),long->ToString(),long->WeightedStrings('text:1'),long->ToString())",
        "Create a sized set of values like ['2945182322382062539', 'text', '37945690212757860', '287864597160630738', '3299224200079606887']"
    })
    public SetSizedHashed(Object sizeFunc, Object... funcs) {
        this.sizeFunc = VirtDataConversions.adapt(sizeFunc, LongToIntFunction.class);
        this.valueFuncs = VirtDataConversions.adaptList(funcs, LongFunction.class, Object.class);
    }

    @Override
    public java.util.Set<Object> apply(long value) {
        int size = sizeFunc.applyAsInt(value);

        long hashed = value;
        java.util.Set<Object> list = new HashSet<>(size);
        for (int i = 0; i < size; i++) {
            hashed = hasher.applyAsLong(hashed);
            // Get the pair-wise function to the list index (0 based)
            // if the list is longer than the functions, use the last function

            int selector = Math.min(i, valueFuncs.size() - 1);
            LongFunction<?> func = valueFuncs.get(selector);
            list.add(func.apply(hashed));
        }
        return list;
    }
}
