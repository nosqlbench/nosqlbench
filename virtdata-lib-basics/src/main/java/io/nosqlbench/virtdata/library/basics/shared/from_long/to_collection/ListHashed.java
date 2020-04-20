package io.nosqlbench.virtdata.library.basics.shared.from_long.to_collection;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.Hash;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.LongFunction;
import java.util.function.LongToIntFunction;
import java.util.function.LongUnaryOperator;

/**
 * Create a {@code List} from a long input
 * based on pseudo-randomly hash list of functions without any size boundaries,
 * each function in the list functions populate the hash list with
 * object value.
 */
@Categories({Category.collections})
@ThreadSafeMapper
public class ListHashed implements LongFunction<List<Object>> {

    private final List<LongFunction<? extends Object>> valueFuncs;
    private final int size;
    private final Hash hasher = new Hash();

    @Example({
        "ListHashed(long->ToString(), long->WeightedStrings('text:1'))",
        "Create a hash list of object values of each function output. ListHashed output ['2945182322382062539','text']"
    })
    public ListHashed(LongFunction<? extends Object>... funcs) {
        this.valueFuncs = Arrays.asList(funcs);
        this.size = valueFuncs.size();
    }

    public ListHashed(LongUnaryOperator... funcs) {
        List<LongFunction<?>> building = new ArrayList<>(funcs.length);
        for (LongUnaryOperator func : funcs) {
            building.add(func::applyAsLong);
        }
        this.valueFuncs = building;
        this.size = building.size();
    }

    public ListHashed(Function<Long,Object>... funcs) {
        List<LongFunction<?>> building = new ArrayList<>(funcs.length);
        for (Function<Long,Object> func : funcs) {
            building.add(func::apply);
        }
        this.valueFuncs = building;
        this.size = building.size();
    }

    @Override
    public List<Object> apply(long value) {
        long hash = value;
        List<Object> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            int selector = Math.min(i, valueFuncs.size() - 1);
            LongFunction<?> func = valueFuncs.get(selector);
            hash = hasher.applyAsLong(value);
            list.add(func.apply(hash));
        }
        return list;
    }
}
