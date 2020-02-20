package io.virtdata.libbasics.shared.unary_int;

import io.virtdata.annotations.ThreadSafeMapper;

import java.util.function.IntUnaryOperator;

/**
 * Adds a cycle range to the input, producing an increasing sawtooth-like output.
 */
@ThreadSafeMapper
public class AddCycleRange implements IntUnaryOperator {

    private final CycleRange cycleRange;

    public AddCycleRange(int maxValue) {
        this(0, maxValue);
    }

    public AddCycleRange(int minValue, int maxValue) {
        this.cycleRange = new CycleRange(minValue,maxValue);
    }

    @Override
    public int applyAsInt(int operand) {
        return operand + cycleRange.applyAsInt(operand);
    }
}
