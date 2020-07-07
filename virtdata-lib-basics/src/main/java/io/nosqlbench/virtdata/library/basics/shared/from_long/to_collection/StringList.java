package io.nosqlbench.virtdata.library.basics.shared.from_long.to_collection;

import io.nosqlbench.virtdata.api.annotations.*;

import java.util.ArrayList;
import java.util.function.LongFunction;
import java.util.function.LongToIntFunction;

/**
 * Create a {@code List<String>} from a long value, based on two functions, the first to determine the list size, and
 * the second to populate the list with String values. The input fed to the second function is incremented between
 * elements. Regardless of the object type provided by the second function, {@link java.lang.Object#toString()} is used
 * to get the value to add to the list.
 * <p>
 * To create Lists of any type of object simply use {@link List} with an specific value mapping function.
 */

@Categories({Category.collections})
@ThreadSafeMapper
@DeprecatedFunction("Use ListSizedStepped")
@Deprecated
public class StringList implements LongFunction<java.util.List<String>> {

    private final LongToIntFunction sizeFunc;
    private final LongFunction<Object> valueFunc;

    @Example({"StringList(HashRange(3,7),Add(15L))", "create a list between 3 and 7 elements of String representations of Long values"})
    public StringList(LongToIntFunction sizeFunc,
                      LongFunction<Object> valueFunc) {
        this.sizeFunc = sizeFunc;
        this.valueFunc = valueFunc;
    }

    @Override
    public java.util.List<String> apply(long value) {
        int size = sizeFunc.applyAsInt(value);
        java.util.List<String> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(valueFunc.apply(value + i).toString());
        }
        return list;
    }
}
