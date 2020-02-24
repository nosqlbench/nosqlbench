package io.nosqlbench.virtdata.library.basics.shared.from_long.to_byte;

import io.nosqlbench.virtdata.annotations.Categories;
import io.nosqlbench.virtdata.annotations.Category;
import io.nosqlbench.virtdata.annotations.DeprecatedFunction;
import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;

import java.util.function.LongFunction;

/**
 * Convert the input long value to a byte, with negative values
 * masked away.
 */
@ThreadSafeMapper
@Categories({Category.conversion})
@DeprecatedFunction("This function is being replaced by ToByte() for naming consistency.")
public class LongToByte implements LongFunction<Byte> {
    @Override
    public Byte apply(long value) {
        return (byte) (value & Byte.MAX_VALUE);
    }
}
