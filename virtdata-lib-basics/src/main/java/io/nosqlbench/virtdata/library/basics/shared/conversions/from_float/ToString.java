package io.nosqlbench.virtdata.library.basics.shared.conversions.from_float;

import io.nosqlbench.virtdata.annotations.Categories;
import io.nosqlbench.virtdata.annotations.Category;
import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;

import java.util.function.Function;

@Categories({Category.conversion})
@ThreadSafeMapper
public class ToString implements Function<Float,String> {

    public String apply(Float aFloat) {
        return String.valueOf(aFloat.floatValue());
    }
}
