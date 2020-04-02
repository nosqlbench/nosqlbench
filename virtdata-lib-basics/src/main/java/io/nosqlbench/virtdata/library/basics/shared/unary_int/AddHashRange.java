package io.nosqlbench.virtdata.library.basics.shared.unary_int;

import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.IntUnaryOperator;

/**
 * Adds a pseudo-random value within the specified range to the input.
 */
@ThreadSafeMapper
public class AddHashRange implements IntUnaryOperator {

    private final io.nosqlbench.virtdata.library.basics.shared.unary_int.HashRange hashRange;

    public AddHashRange(int maxValue) {
        this(0, maxValue);
    }

    public AddHashRange(int minValue, int maxValue) {
        this.hashRange = new io.nosqlbench.virtdata.library.basics.shared.unary_int.HashRange(minValue, maxValue);
    }

    @Override
    public int applyAsInt(int operand) {
        return operand + hashRange.applyAsInt(operand);
    }
}
