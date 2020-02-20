package io.virtdata.libbasics.shared.from_long.to_long;

import io.virtdata.annotations.ThreadSafeMapper;

import java.util.function.LongUnaryOperator;

/**
 * Return a value within a range, pseudo-randomly. This is equivalent to
 * returning a value with in range between 0 and some maximum value, but
 * with a minimum value added.
 */
@ThreadSafeMapper
public class HashRange implements LongUnaryOperator {

    private final long minValue;
    private final long width;
    private final Hash hash = new Hash();

    public HashRange(long width) {
        this.minValue=0L;
        this.width=width;
    }

    public HashRange(long minValue, long maxValue) {
        this.minValue = minValue;

        if (maxValue<=minValue) {
            throw new RuntimeException("HashRange must have min and max value in that order.");
        }
        this.width = maxValue - minValue;
    }

    @Override
    public long applyAsLong(long operand) {
        return minValue + (hash.applyAsLong(operand) % width);
    }
}
