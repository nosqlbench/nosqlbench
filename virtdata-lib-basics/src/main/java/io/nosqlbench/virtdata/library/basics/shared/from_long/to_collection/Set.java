package io.nosqlbench.virtdata.library.basics.shared.from_long.to_collection;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.HashSet;
import java.util.function.LongFunction;
import java.util.function.LongToIntFunction;

/**
 * Create a {@code Set} from a long input based on two functions,
 * the first to determine the set size, and the second to populate
 * the set with object values. The input fed to the second function
 * is incremented between elements.
 *
 * To create Sets of Strings from the String version of the same
 * mapping functions, simply use {@link StringSet} instead.
 */
@Categories({Category.collections})
@ThreadSafeMapper
public class Set implements LongFunction<java.util.Set<Object>> {

    private final LongToIntFunction sizeFunc;
    private final LongFunction<Object> valueFunc;

    @Example({"Set(HashRange(3,7),Add(15L))", "create a set between 3 and 7 elements of Long values"})
    public Set(LongToIntFunction sizeFunc,
               LongFunction<Object> valueFunc) {
        this.sizeFunc = sizeFunc;
        this.valueFunc = valueFunc;
    }

    @Override
    public java.util.Set<Object> apply(long value) {
        int size = sizeFunc.applyAsInt(value);
        java.util.Set<Object> set = new HashSet<>(size);
        for (int i = 0; i < size; i++) {
            set.add(valueFunc.apply(value+i));
        }
        return set;
    }
}
