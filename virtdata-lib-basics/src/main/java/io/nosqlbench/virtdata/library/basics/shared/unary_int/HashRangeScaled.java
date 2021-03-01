package io.nosqlbench.virtdata.library.basics.shared.unary_int;

import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.IntUnaryOperator;

@ThreadSafeMapper
public class HashRangeScaled implements IntUnaryOperator {

    private final Hash hash = new Hash();
    private final double scalefactor;

    public HashRangeScaled(double scalefactor) {
        this.scalefactor = scalefactor;
    }

    public HashRangeScaled() {
        this.scalefactor = 1.0D;
    }

    @Override
    public int applyAsInt(int operand) {
        if (operand == 0) {
            return 0;
        }
        return (int) ((hash.applyAsInt(operand) % operand) * scalefactor) % Integer.MAX_VALUE;
    }
}
