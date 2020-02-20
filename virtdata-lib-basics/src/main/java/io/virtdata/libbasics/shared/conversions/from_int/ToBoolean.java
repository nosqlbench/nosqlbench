package io.virtdata.libbasics.shared.conversions.from_int;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.ThreadSafeMapper;

import java.util.function.Function;

@ThreadSafeMapper
@Categories({Category.conversion})
public class ToBoolean implements Function<Integer,Boolean> {

    @Override
    public Boolean apply(Integer input) {
        return ((input & 1) == 1);
    }
}
