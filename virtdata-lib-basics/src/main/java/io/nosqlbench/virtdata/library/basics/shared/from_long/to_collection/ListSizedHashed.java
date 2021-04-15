package io.nosqlbench.virtdata.library.basics.shared.from_long.to_collection;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.api.bindings.VirtDataConversions;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.Hash;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongFunction;
import java.util.function.LongToIntFunction;

/**
 * Create a List from a long input based on a set of provided functions.
 *
 *  As a 'Sized' function, the first argument is a function which determines the size of the resulting list.
 * Additional functions provided are used to generate the elements to add to the collection. If the size
 * is larger than the number of provided functions, the last provided function is used repeatedly as needed.
 *
 *  As a 'Hashed' function, the input value is hashed again before being used by each element function.
 */
@Categories({Category.collections})
@ThreadSafeMapper
public class ListSizedHashed implements LongFunction<List<Object>> {

    private final List<LongFunction> valueFuncs;
    private final Hash hasher = new Hash();
    private final LongToIntFunction sizeFunc;

    @Example({
        "ListSizedHashed(FixedValue(5),long->ToString(),long->WeightedStrings('text:1'),long->ToString())",
        "Create a sized hash list of object values of each function output. List size function will recursively call the last function till" +
            "end of the list size functions",
        "ListSizedHashed output ['2945182322382062539', 'text', '37945690212757860', '287864597160630738', '3299224200079606887']"
    })
    public ListSizedHashed(Object sizeFunc, Object... funcs) {
        if (sizeFunc instanceof Number) {
            int size = ((Number)sizeFunc).intValue();
            this.sizeFunc = s -> size;
        } else {
            this.sizeFunc = VirtDataConversions.adaptFunction(sizeFunc, LongToIntFunction.class);
        }
        this.valueFuncs = VirtDataConversions.adaptFunctionList(funcs, LongFunction.class, Object.class);
    }

    public ListSizedHashed(int size, Object... funcs) {
        this.sizeFunc = s -> size;
        this.valueFuncs = VirtDataConversions.adaptFunctionList(funcs, LongFunction.class, Object.class);
    }

    @Override
    public List<Object> apply(long value) {
        int size = sizeFunc.applyAsInt(value);

        long hashed = value;
        List<Object> list = new ArrayList<>(size);
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
