package io.nosqlbench.virtdata.library.basics.shared.from_long.to_long;

import io.nosqlbench.nb.api.errors.BasicError;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.LongUnaryOperator;

/**
 * Return a value within a range, pseudo-randomly. This is equivalent to
 * returning a value with in range between 0 and some maximum value, but
 * with a minimum value added.
 *
 * You can specify hash ranges as small as a single-element range, like
 * (5,5), or as wide as the relevant data type allows.
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
        if (maxValue<minValue) {
            throw new BasicError("HashRange must have min and max value in that order.");
        }
        this.minValue = minValue;
        this.width = (maxValue - minValue)+1;
    }

    @Override
    public long applyAsLong(long operand) {
        return minValue + (hash.applyAsLong(operand) % width);
    }
}
