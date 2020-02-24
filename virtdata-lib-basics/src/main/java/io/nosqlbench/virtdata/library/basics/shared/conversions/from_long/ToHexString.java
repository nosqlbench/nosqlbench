package io.nosqlbench.virtdata.library.basics.shared.conversions.from_long;

import io.nosqlbench.virtdata.annotations.Categories;
import io.nosqlbench.virtdata.annotations.Category;
import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;

import java.util.function.LongFunction;

@Categories(Category.conversion)
@ThreadSafeMapper
public class ToHexString implements LongFunction<String> {

    @Override
    public String apply(long value) {
        return Long.toHexString(value);
    }
}
