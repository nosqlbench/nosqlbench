package io.nosqlbench.virtdata.library.basics.shared.conversions.from_string;

import io.nosqlbench.virtdata.annotations.Categories;
import io.nosqlbench.virtdata.annotations.Category;
import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;

import java.util.function.Function;

@ThreadSafeMapper
@Categories({Category.conversion})
public class ToByte implements Function<String,Byte> {
    @Override
    public Byte apply(String input) {
        return Byte.valueOf(input);
    }
}
