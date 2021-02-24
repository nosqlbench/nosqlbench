package io.nosqlbench.virtdata.library.basics.shared.from_long.to_long;

import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.LongUnaryOperator;

/**
 * Return a pseudo-random value which can only be as large as the input times
 * a scale factor, with a default scale factor of 1.0d
 */
@ThreadSafeMapper
public class HashRangeScaled implements LongUnaryOperator {

    private final double scalefactor;
    private final Hash hash = new Hash();

    public HashRangeScaled(double scalefactor) {
        this.scalefactor = scalefactor;
    }

    public HashRangeScaled() {
        this.scalefactor = 1.0D;
    }

    @Override
    public long applyAsLong(long operand) {
        if (operand == 0) {
            return 0;
        }
        long hashed = hash.applyAsLong(operand);
        return (long) ((hashed % operand) * scalefactor);
    }
}
