package io.virtdata.libbasics.shared.from_long.to_int;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.ThreadSafeMapper;

import java.util.function.LongToIntFunction;

@ThreadSafeMapper
@Categories({Category.conversion})
public class ToInt implements LongToIntFunction{
    @Override
    public int applyAsInt(long value) {
        return (int) (value % Integer.MAX_VALUE);
    }
}
