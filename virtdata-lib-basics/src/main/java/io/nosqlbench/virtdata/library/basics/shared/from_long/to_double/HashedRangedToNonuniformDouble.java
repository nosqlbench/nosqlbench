package io.nosqlbench.virtdata.library.basics.shared.from_long.to_double;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.Hash;

import java.util.function.LongToDoubleFunction;

/**
 * This provides a random sample of a double in a range, without
 * accounting for the non-uniform distribution of IEEE double representation.
 * This means that values closer to high-precision areas of the IEEE spec
 * will be weighted higher in the output. However, NaN and positive and
 * negative infinity are filtered out via oversampling. Results are still
 * stable for a given input value.
 */
@ThreadSafeMapper
@Categories({Category.general})
public class HashedRangedToNonuniformDouble implements LongToDoubleFunction {

    private final long min;
    private final long max;
    private final double length;
    private final Hash hash;

    public HashedRangedToNonuniformDouble(long min, long max) {
        this.hash = new Hash();
        if (max<=min) {
            throw new RuntimeException("max must be >= min");
        }
        this.min = min;
        this.max = max;
        this.length = (double) max - min;
    }

    public String toString() {
        return getClass().getSimpleName() + ":" + min + ":" + max;
    }

    @Override
    public double applyAsDouble(long input) {
        long bitImage = hash.applyAsLong(input);
        double value = Math.abs(Double.longBitsToDouble(bitImage));
        while (!Double.isFinite(value)) {
            input++;
            bitImage = hash.applyAsLong(input);
            value = Math.abs(Double.longBitsToDouble(bitImage));
        }
        value %= length;
        value += min;
        return value;
    }
}

