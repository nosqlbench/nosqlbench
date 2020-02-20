package io.virtdata.libbasics.shared.conversions.from_string;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.ThreadSafeMapper;

import java.util.function.Function;

@ThreadSafeMapper
@Categories({Category.conversion})
public class ToInt implements Function<String,Integer> {

    @Override
    public Integer apply(String input) {
        return Integer.valueOf(input);
    }
}
