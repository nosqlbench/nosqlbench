package io.nosqlbench.virtdata.library.basics.shared.from_long.to_collection;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.HashSet;
import java.util.function.LongFunction;
import java.util.function.LongToIntFunction;

/**
 * Create a {@code Set<String>} from a long
 * based on two functions, the first to
 * determine the set size, and the second to populate the set with
 * String values. The input fed to the second function is incremented
 * between elements. Regardless of the object type provided by the
 * second function, {@link java.lang.Object#toString()} is used to get
 * the value to add to the list.
 *
 * To create Sets of any type of object simply use {@link Set} with
 * a specific value mapping function.
 */
@Categories({Category.collections})
@ThreadSafeMapper
public class StringSet implements LongFunction<java.util.Set<String>> {

    private final LongToIntFunction sizeFunc;
    private final LongFunction<Object> valueFunc;

    @Example({"StringSet(HashRange(3,7),Add(15L))", "create a set between 3 and 7 elements of String representations of Long values"})
    public StringSet(LongToIntFunction sizeFunc,
                     LongFunction<Object> valueFunc) {
        this.sizeFunc = sizeFunc;
        this.valueFunc = valueFunc;
    }

    @Override
    public java.util.Set<String> apply(long value) {
        int size = sizeFunc.applyAsInt(value);
        java.util.Set<String> set = new HashSet<>(size);
        for (int i = 0; i < size; i++) {
            set.add(valueFunc.apply(value + i).toString());
        }
        return set;
    }
}
