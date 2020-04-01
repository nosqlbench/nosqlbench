package io.nosqlbench.virtdata.library.basics.shared.conversions.from_long;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.LongFunction;

@Categories(Category.conversion)
@ThreadSafeMapper
public class ToHexString implements LongFunction<String> {

    @Override
    public String apply(long value) {
        return Long.toHexString(value);
    }
}
