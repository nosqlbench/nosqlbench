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
import java.util.function.LongToIntFunction;
import java.util.function.LongUnaryOperator;

/**
 * Create a {@code List} from a long input
 * based on at least two functions, the first function to
 * determine the list functions size, and the remaining functions onwards to populate
 * the list with object values till the end of the list size.
 */
@Categories({Category.collections})
@ThreadSafeMapper
public class ListSized implements LongFunction<List<Object>> {

    private final List<LongFunction<? extends Object>> valueFuncs;
    private final LongToIntFunction sizeFunc;

    @Example({
        "ListSized(FixedValue(5), NumberNameToString(),NumberNameToString(), WeightedStrings('text:1'))",
        "Create a sized list of object values of each function output. List size function will recursively call the last function till" +
            "end of the list size functions",
        "ListSized output ['one','one','text','text','text']"
    })
    public ListSized(LongToIntFunction sizeFunc, LongFunction<? extends Object>... funcs) {
        this.sizeFunc = sizeFunc;
        this.valueFuncs = Arrays.asList(funcs);
    }

    public ListSized(LongToIntFunction sizeFunc, LongUnaryOperator... funcs) {
        List<LongFunction<?>> building = new ArrayList<>(funcs.length);
        for (LongUnaryOperator func : funcs) {
            building.add(func::applyAsLong);
        }
        this.sizeFunc = sizeFunc;
        this.valueFuncs = building;
    }

    public ListSized(LongToIntFunction sizeFunc, Function<Long,Object>... funcs) {
        List<LongFunction<?>> building = new ArrayList<>(funcs.length);
        for (Function<Long,Object> func : funcs) {
            building.add(func::apply);
        }
        this.sizeFunc = sizeFunc;
        this.valueFuncs = building;
    }

    @Override
    public List<Object> apply(long value) {
        int size = sizeFunc.applyAsInt(value);
        List<Object> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            int selector = Math.min(i, valueFuncs.size() - 1);
            LongFunction<?> func = valueFuncs.get(selector);
            list.add(func.apply(value));
        }
        return list;
    }
}
