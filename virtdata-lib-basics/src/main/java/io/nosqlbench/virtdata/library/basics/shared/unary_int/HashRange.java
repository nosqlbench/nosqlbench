package io.nosqlbench.virtdata.library.basics.shared.unary_int;

import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;

import java.util.function.IntUnaryOperator;

@ThreadSafeMapper
public class HashRange implements IntUnaryOperator {

    private final int minValue;
    private final int  width;
    private final Hash hash = new Hash();

    public HashRange(int width) {
        this.minValue=0;
        this.width=width;
    }

    public HashRange(int minValue, int maxValue) {
        this.minValue = minValue;

        if (maxValue<=minValue) {
            throw new RuntimeException("HashRange must have min and max value in that order.");
        }
        this.width = maxValue - minValue;
    }

    @Override
    public int applyAsInt(int operand) {
        return minValue + (hash.applyAsInt(operand) & width);
    }
}
