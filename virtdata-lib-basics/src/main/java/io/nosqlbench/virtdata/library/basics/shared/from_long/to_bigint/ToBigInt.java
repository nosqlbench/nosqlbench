package io.nosqlbench.virtdata.library.basics.shared.from_long.to_bigint;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.math.BigInteger;
import java.util.function.LongFunction;

/**
 * Convert the input value to a {@code BigInteger}
 */
@ThreadSafeMapper
@Categories({Category.conversion})
public class ToBigInt implements LongFunction<BigInteger> {
    @Override
    public BigInteger apply(long input) {
        return BigInteger.valueOf(input);
    }
}
