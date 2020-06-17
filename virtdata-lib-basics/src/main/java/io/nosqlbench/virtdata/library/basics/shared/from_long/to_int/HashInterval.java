package io.nosqlbench.virtdata.library.basics.shared.from_long.to_int;

import io.nosqlbench.nb.api.errors.BasicError;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.Hash;

import java.util.function.LongToIntFunction;

@ThreadSafeMapper
public class HashInterval implements LongToIntFunction {

    private final long minValue;
    private final long width;
    private final Hash hash = new Hash();

    /**
     * Create a hash interval based on a minimum value of 0 and a specified width.
     * @param width The maximum value, which is excluded.
     */
    @Example({"HashInterval(4)","return values which could include 0, 1, 2, 3, but not 4"})
    public HashInterval(int width) {
        this.width=width;
        this.minValue=0L;
    }

    /**
     * Create a hash interval
     * @param minIncl The minimum value, which is included
     * @param maxExcl The maximum value, which is excluded
     */
    @Example({"HashInterval(2,5)","return values which could include 2, 3, 4, but not 5"})
    public HashInterval(int minIncl, int maxExcl) {
        if (maxExcl<=minIncl) {
            throw new BasicError("HashInterval must have min and max value in that order, where the min is less than the max.");
        }
        this.minValue = minIncl;
        this.width = (maxExcl - minIncl);
    }

    @Override
    public int applyAsInt(long operand) {
        return (int) ((minValue + (hash.applyAsLong(operand) % width)) & Integer.MAX_VALUE);
    }
}
