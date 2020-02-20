package io.virtdata.libbasics.shared.from_double.to_double;

import io.virtdata.annotations.Example;

import java.util.function.DoubleUnaryOperator;

/**
 * Combine multiple DoubleUnaryOperators into a single function.
 */
public class DoubleFlow implements DoubleUnaryOperator {

    private final DoubleUnaryOperator[] ops;

    @Example({"StringFlow(Add(3.0D),Mul(10.0D))","adds 3.0 and then multiplies by 10.0"})
    public DoubleFlow(DoubleUnaryOperator... ops) {
        this.ops = ops;
    }

    @Override
    public double applyAsDouble(double operand) {
        double value = operand;
        for (DoubleUnaryOperator op : ops) {
            value = op.applyAsDouble(value);
        }
        return value;
    }
}
