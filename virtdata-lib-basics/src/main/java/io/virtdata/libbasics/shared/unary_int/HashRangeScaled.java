package io.virtdata.libbasics.shared.unary_int;

import io.virtdata.annotations.ThreadSafeMapper;

import java.util.function.IntUnaryOperator;

@ThreadSafeMapper
public class HashRangeScaled implements IntUnaryOperator {

    private final Hash hash = new Hash();

    @Override
    public int applyAsInt(int operand) {
        if (operand==0) { return 0; }
        return hash.applyAsInt(operand) % operand;
    }
}
