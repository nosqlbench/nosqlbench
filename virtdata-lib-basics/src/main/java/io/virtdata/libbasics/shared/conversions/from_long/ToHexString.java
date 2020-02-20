package io.virtdata.libbasics.shared.conversions.from_long;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.ThreadSafeMapper;

import java.util.function.LongFunction;

@Categories(Category.conversion)
@ThreadSafeMapper
public class ToHexString implements LongFunction<String> {

    @Override
    public String apply(long value) {
        return Long.toHexString(value);
    }
}
