package io.virtdata.libbasics.shared.from_long.to_bigint;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.ThreadSafeMapper;

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
