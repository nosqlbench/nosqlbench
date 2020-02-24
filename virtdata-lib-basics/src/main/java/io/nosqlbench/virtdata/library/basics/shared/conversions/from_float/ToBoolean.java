package io.nosqlbench.virtdata.library.basics.shared.conversions.from_float;

import io.nosqlbench.virtdata.annotations.Categories;
import io.nosqlbench.virtdata.annotations.Category;
import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;

import java.util.function.Function;

@ThreadSafeMapper
@Categories({Category.conversion})
public class ToBoolean implements Function<Float,Boolean> {

    @Override
    public Boolean apply(Float input) {
        return ((input.longValue()) & 1) == 1;
    }
}
