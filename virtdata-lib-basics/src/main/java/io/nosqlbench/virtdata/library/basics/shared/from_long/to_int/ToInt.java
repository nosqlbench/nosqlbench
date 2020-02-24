package io.nosqlbench.virtdata.library.basics.shared.from_long.to_int;

import io.nosqlbench.virtdata.annotations.Categories;
import io.nosqlbench.virtdata.annotations.Category;
import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;

import java.util.function.LongToIntFunction;

@ThreadSafeMapper
@Categories({Category.conversion})
public class ToInt implements LongToIntFunction{
    @Override
    public int applyAsInt(long value) {
        return (int) (value % Integer.MAX_VALUE);
    }
}
