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
 * based on two functions, the first to
 * determine the list size, and the second to populate the list with
 * object values. The input fed to the second function is incremented
 * between elements.
 *
 * To directly create Lists of Strings from the String version of the same
 * mapping functions, simply use {@link StringList} instead.
 */
@Categories({Category.collections})
@ThreadSafeMapper
public class ListStepped implements LongFunction<List<Object>> {

    private final List<LongFunction<? extends Object>> valueFuncs;
    private final int size;

    @Example({
        "ListFunctions(NumberNameToString(),NumberNameToString())",
        "Create a list of ['one','one']"
    })
    public ListStepped(LongFunction<? extends Object>... funcs) {
        this.valueFuncs = Arrays.asList(funcs);
        this.size = valueFuncs.size();
    }

    @Example({
        "ListFunctions(NumberNameToString(),NumberNameToString())",
        "Create a list of ['one','one']"
    })
    public ListStepped(LongUnaryOperator... funcs) {
        List<LongFunction<?>> building = new ArrayList<>(funcs.length);
        for (LongUnaryOperator func : funcs) {
            building.add(func::applyAsLong);
        }
        this.valueFuncs = building;
        this.size = building.size();
    }

    @Example({
        "ListFunctions(NumberNameToString(),NumberNameToString())",
        "Create a list of ['one','one']"
    })
    public ListStepped(Function<Long,Object>... funcs) {
        List<LongFunction<?>> building = new ArrayList<>(funcs.length);
        for (Function<Long,Object> func : funcs) {
            building.add(func::apply);
        }
        this.valueFuncs = building;
        this.size = building.size();
    }

    @Override
    public List<Object> apply(long value) {
        List<Object> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            int selector = Math.min(i, valueFuncs.size() - 1);
            LongFunction<?> func = valueFuncs.get(selector);
            list.add(func.apply(value+i));
        }
        return list;
    }
}
