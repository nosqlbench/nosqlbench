package io.nosqlbench.virtdata.library.basics.shared.from_long.to_int;

import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.Hash;

import java.util.function.LongToIntFunction;

@ThreadSafeMapper
public class HashRangeScaled implements LongToIntFunction {

    private final Hash hash = new Hash();

    @Override
    public int applyAsInt(long operand) {
        if (operand==0) { return 0; }
        long l = hash.applyAsLong(operand);
        return (int) ((l % operand) % Integer.MAX_VALUE);
    }
}
