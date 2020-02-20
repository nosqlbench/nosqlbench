package io.virtdata.libbasics.shared.from_long.to_short;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.DeprecatedFunction;
import io.virtdata.annotations.ThreadSafeMapper;

import java.util.function.LongFunction;

/**
 * Convert the input value from long to short.
 */
@ThreadSafeMapper
@DeprecatedFunction("This function is being replaced by ToShort() for naming consistency.")
@Categories({Category.conversion})
public class LongToShort implements LongFunction<Short> {

    @Override
    public Short apply(long value) {
        return (short) (value & Short.MAX_VALUE);
    }
}
