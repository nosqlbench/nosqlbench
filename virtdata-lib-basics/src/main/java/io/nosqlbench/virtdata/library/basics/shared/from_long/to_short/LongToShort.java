package io.nosqlbench.virtdata.library.basics.shared.from_long.to_short;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.DeprecatedFunction;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

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
