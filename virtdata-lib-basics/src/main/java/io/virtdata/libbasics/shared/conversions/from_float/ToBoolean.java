package io.virtdata.libbasics.shared.conversions.from_float;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.ThreadSafeMapper;

import java.util.function.Function;

@ThreadSafeMapper
@Categories({Category.conversion})
public class ToBoolean implements Function<Float,Boolean> {

    @Override
    public Boolean apply(Float input) {
        return ((input.longValue()) & 1) == 1;
    }
}
