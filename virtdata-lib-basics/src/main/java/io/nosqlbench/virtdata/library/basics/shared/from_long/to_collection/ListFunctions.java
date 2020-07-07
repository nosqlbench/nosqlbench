package io.nosqlbench.virtdata.library.basics.shared.from_long.to_collection;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.LongFunction;
import java.util.function.LongUnaryOperator;

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

    private final java.util.List<LongFunction<? extends Object>> valueFuncs;
    private final int size;

    @Example({
            "ListFunctions(NumberNameToString(),NumberNameToString(),NumberNameToString())",
            "Create a list of object values of each function output. Produces values like ['one','one','one']"
    })
    public ListFunctions(LongFunction<? extends Object>... funcs) {
        this.valueFuncs = Arrays.asList(funcs);
        this.size = valueFuncs.size();
    }

    public ListFunctions(LongUnaryOperator... funcs) {
        List<LongFunction<?>> building = new ArrayList<>(funcs.length);
        for (LongUnaryOperator func : funcs) {
            building.add(func::applyAsLong);
        }
        this.valueFuncs = building;
        this.size = building.size();
    }

    public ListFunctions(Function<Long, Object>... funcs) {
        List<LongFunction<?>> building = new ArrayList<>(funcs.length);
        for (Function<Long, Object> func : funcs) {
            building.add(func::apply);
        }
        this.valueFuncs = building;
        this.size = building.size();
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
