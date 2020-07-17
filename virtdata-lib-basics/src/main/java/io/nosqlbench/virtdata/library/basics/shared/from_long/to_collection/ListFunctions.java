package io.nosqlbench.virtdata.library.basics.shared.from_long.to_collection;

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
