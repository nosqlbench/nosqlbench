package io.virtdata.libbasics.shared.conversions.from_string;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.ThreadSafeMapper;

import java.util.function.Function;

@ThreadSafeMapper
@Categories({Category.conversion})
public class ToShort implements Function<String,Short> {

    @Override
    public Short apply(String input) {
        return Short.valueOf(input);
    }
}
