package io.nosqlbench.virtdata.library.basics.shared.unary_int;

import io.nosqlbench.virtdata.annotations.Example;
import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;

import java.util.function.IntUnaryOperator;

@ThreadSafeMapper
public class Clamp implements IntUnaryOperator {

    private final int min;
    private final int max;

    @Example({"Clamp(1,100)","clamp the output values in the range [1,100], inclusive"})
    public Clamp(int min, int max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public int applyAsInt(int operand) {
        return Integer.min(max,Integer.max(min,operand));
    }
}
