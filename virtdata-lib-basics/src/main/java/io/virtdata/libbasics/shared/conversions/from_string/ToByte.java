package io.virtdata.libbasics.shared.conversions.from_string;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.ThreadSafeMapper;

import java.util.function.Function;

@ThreadSafeMapper
@Categories({Category.conversion})
public class ToByte implements Function<String,Byte> {
    @Override
    public Byte apply(String input) {
        return Byte.valueOf(input);
    }
}
