package io.virtdata.libbasics.shared.conversions.from_float;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.ThreadSafeMapper;

import java.util.function.Function;

@Categories({Category.conversion})
@ThreadSafeMapper
public class ToString implements Function<Float,String> {

    public String apply(Float aFloat) {
        return String.valueOf(aFloat.floatValue());
    }
}
