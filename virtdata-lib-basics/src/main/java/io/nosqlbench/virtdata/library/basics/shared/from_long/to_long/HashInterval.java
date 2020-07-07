package io.nosqlbench.virtdata.library.basics.shared.from_long.to_long;

import io.nosqlbench.nb.api.errors.BasicError;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.LongUnaryOperator;

/**
 * Return a value within a range, pseudo-randomly, using interval semantics,
 * where the range of values return does not include the last value.
 * This function behaves exactly like HashRange except for the exclusion
 * of the last value. This allows you to stack intervals using known
 * reference points without duplicating or skipping any given value.
 *
 * You can specify hash intervals as small as a single-element range, like
 * (5,6), or as wide as the relevant data type allows.
 */
@ThreadSafeMapper
public class HashInterval implements LongUnaryOperator {

    private final long minValue;
    private final long width;
    private final Hash hash = new Hash();

    /**
     * Create a hash interval based on a minimum value of 0 and a specified width.
     * @param width The maximum value, which is excluded.
     */
    @Example({"HashInterval(4L)","return values which could include 0L, 1L, 2L, 3L, but not 4L"})
    public HashInterval(long width) {
        this.minValue=0L;
        this.width=width;
    }

    /**
     * Create a hash interval
     * @param minIncl The minimum value, which is included
     * @param maxExcl The maximum value, which is excluded
     */
    @Example({"HashInterval(2L,5L)","return values which could include 2L, 3L, 4L, but not 5L"})
    public HashInterval(long minIncl, long maxExcl) {
        if (maxExcl<=minIncl) {
            throw new BasicError("HashInterval must have min and max value in that order, where the min is less than the max.");
        }
        this.minValue = minIncl;
        this.width = (maxExcl - minIncl);
    }

    @Override
    public long applyAsLong(long operand) {
        return minValue + (hash.applyAsLong(operand) % width);
    }
}
