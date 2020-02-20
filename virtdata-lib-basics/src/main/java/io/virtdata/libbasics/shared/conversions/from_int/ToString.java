package io.virtdata.libbasics.shared.conversions.from_int;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.ThreadSafeMapper;

import java.util.function.IntFunction;

@Categories({Category.conversion})
@ThreadSafeMapper
public class ToString implements IntFunction<String> {
    public String apply(int i) {
        return String.valueOf(i);
    }
}
