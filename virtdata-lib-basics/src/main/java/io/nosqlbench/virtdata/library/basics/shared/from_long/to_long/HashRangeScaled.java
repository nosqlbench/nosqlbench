package io.nosqlbench.virtdata.library.basics.shared.from_long.to_long;

import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;

import java.util.function.LongUnaryOperator;

/**
 * Return a pseudo-random value which can only be as large as the input.
 */
@ThreadSafeMapper
public class HashRangeScaled implements LongUnaryOperator {

    private Hash hash = new Hash();

    public HashRangeScaled() {
    }

    @Override
    public long applyAsLong(long operand) {
        if (operand==0) { return 0; }
        long hashed = hash.applyAsLong(operand);
        return hashed % operand;
    }
}
