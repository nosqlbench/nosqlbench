package io.nosqlbench.virtdata.library.basics.shared.unary_int;

import io.nosqlbench.virtdata.annotations.Categories;
import io.nosqlbench.virtdata.annotations.Category;
import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;

import java.util.function.IntUnaryOperator;

/**
 * Combine multiple IntUnaryOperators into a single function.
 */
@Categories(Category.functional)
@ThreadSafeMapper
public class IntFlow implements IntUnaryOperator {

    private final IntUnaryOperator[] ops;

    public IntFlow(IntUnaryOperator... ops) {
        this.ops = ops;
    }

    @Override
    public int applyAsInt(int operand) {
        int value = operand;
        for (IntUnaryOperator op : ops) {
            value = op.applyAsInt(value);
        }
        return value;
    }
}
