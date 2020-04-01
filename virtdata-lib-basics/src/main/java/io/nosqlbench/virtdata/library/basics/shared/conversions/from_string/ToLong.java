package io.nosqlbench.virtdata.library.basics.shared.conversions.from_string;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.Function;

@ThreadSafeMapper
@Categories({Category.conversion})
public class ToLong implements Function<String,Long> {

    @Override
    public Long apply(String input) {
        return Long.valueOf(input);
    }
}
