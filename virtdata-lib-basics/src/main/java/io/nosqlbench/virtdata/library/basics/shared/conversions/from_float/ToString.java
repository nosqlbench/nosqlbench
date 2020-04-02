package io.nosqlbench.virtdata.library.basics.shared.conversions.from_float;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.Function;

@Categories({Category.conversion})
@ThreadSafeMapper
public class ToString implements Function<Float,String> {

    public String apply(Float aFloat) {
        return String.valueOf(aFloat.floatValue());
    }
}
