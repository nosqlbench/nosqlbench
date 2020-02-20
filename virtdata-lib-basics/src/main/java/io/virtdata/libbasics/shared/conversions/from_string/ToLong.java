package io.virtdata.libbasics.shared.conversions.from_string;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.ThreadSafeMapper;

import java.util.function.Function;

@ThreadSafeMapper
@Categories({Category.conversion})
public class ToLong implements Function<String,Long> {

    @Override
    public Long apply(String input) {
        return Long.valueOf(input);
    }
}
