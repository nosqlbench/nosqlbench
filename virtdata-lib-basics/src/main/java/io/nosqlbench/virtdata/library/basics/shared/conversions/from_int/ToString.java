package io.nosqlbench.virtdata.library.basics.shared.conversions.from_int;

import io.nosqlbench.virtdata.annotations.Categories;
import io.nosqlbench.virtdata.annotations.Category;
import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;

import java.util.function.IntFunction;

@Categories({Category.conversion})
@ThreadSafeMapper
public class ToString implements IntFunction<String> {
    public String apply(int i) {
        return String.valueOf(i);
    }
}
