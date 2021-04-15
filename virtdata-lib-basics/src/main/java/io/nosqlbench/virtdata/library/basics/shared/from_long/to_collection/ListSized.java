package io.nosqlbench.virtdata.library.basics.shared.from_long.to_collection;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.api.bindings.VirtDataConversions;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongFunction;
import java.util.function.LongToIntFunction;

/**
 * Create a List from a long input based on a set of provided functions.
 *
 * As a 'Sized' function, the first argument is a function which determines the size of the resulting list.
 * Additional functions provided are used to generate the elements to add to the collection. If the size
 * is larger than the number of provided functions, the last provided function is used repeatedly as needed.
 *
 *  As neither a 'Stepped' nor a 'Hashed' function, the input value used by each element function is the same
 *  as that provided to the outer function.
 */
@Categories({Category.collections})
@ThreadSafeMapper
public class ListSized implements LongFunction<List<Object>> {

    private final List<LongFunction> valueFuncs;
    private final LongToIntFunction sizeFunc;

    @Example({
            "ListSized(FixedValue(5), NumberNameToString(),NumberNameToString(), WeightedStrings('text:1'))",
            "Create a sized list of object values of each function output. List size function will recursively call the last function till" +
                    "end of the list size functions",
            "output: ['one','one','text','text','text']"
    })
    public ListSized(Object sizeFunc, Object... funcs) {
        if (sizeFunc instanceof Number) {
            int size = ((Number)sizeFunc).intValue();
            this.sizeFunc = s -> size;
        } else {
            this.sizeFunc = VirtDataConversions.adaptFunction(sizeFunc, LongToIntFunction.class);
        }
        this.valueFuncs = VirtDataConversions.adaptFunctionList(funcs, LongFunction.class, Object.class);
    }
    public ListSized(int size, Object... funcs) {
        this.sizeFunc = (s) -> size;
        this.valueFuncs = VirtDataConversions.adaptFunctionList(funcs, LongFunction.class, Object.class);
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
