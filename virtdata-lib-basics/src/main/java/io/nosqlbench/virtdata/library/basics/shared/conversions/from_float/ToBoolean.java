package io.nosqlbench.virtdata.library.basics.shared.conversions.from_float;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.Function;

@ThreadSafeMapper
@Categories({Category.conversion})
public class ToBoolean implements Function<Float,Boolean> {

    @Override
    public Boolean apply(Float input) {
        return ((input.longValue()) & 1) == 1;
    }
}
