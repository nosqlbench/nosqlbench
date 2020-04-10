package io.nosqlbench.virtdata.library.basics.shared.from_long.to_collection;

import io.nosqlbench.virtdata.api.annotations.*;

import java.util.ArrayList;
import java.util.function.LongFunction;
import java.util.function.LongToIntFunction;

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
@DeprecatedFunction("Use ListSizedStepped")
public class List implements LongFunction<java.util.List<Object>> {

    private final LongToIntFunction sizeFunc;
    private final LongFunction<Object> valueFunc;

    @Example({"List(HashRange(3,7),Add(15L))", "create a list between 3 and 7 elements of Long values"})
    public List(LongToIntFunction sizeFunc,
                LongFunction<Object> valueFunc) {
        this.sizeFunc = sizeFunc;
        this.valueFunc = valueFunc;
    }

    @Override
    public java.util.List<Object> apply(long value) {
        int size = sizeFunc.applyAsInt(value);
        java.util.List<Object> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(valueFunc.apply(value+i));
        }
        return list;
    }
}
