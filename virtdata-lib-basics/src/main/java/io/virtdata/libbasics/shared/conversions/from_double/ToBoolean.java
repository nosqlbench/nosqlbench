package io.virtdata.libbasics.shared.conversions.from_double;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.ThreadSafeMapper;

import java.util.function.Function;

@ThreadSafeMapper
@Categories({Category.conversion})
public class ToBoolean implements Function<Double,Boolean> {

    @Override
    public Boolean apply(Double input) {
        return ((input.longValue()) & 1) == 1;
    }
}
