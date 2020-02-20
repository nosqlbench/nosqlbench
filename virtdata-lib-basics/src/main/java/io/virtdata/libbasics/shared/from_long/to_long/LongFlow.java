package io.virtdata.libbasics.shared.from_long.to_long;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.Example;
import io.virtdata.annotations.ThreadSafeMapper;

import java.util.function.LongUnaryOperator;

/**
 * Combine multiple LongUnaryOperators into a single function.
 */
@Categories(Category.functional)
@ThreadSafeMapper
public class LongFlow implements LongUnaryOperator {

    private final LongUnaryOperator[] ops;

    @Example({"StringFlow(Add(3),Mul(6))","Create an integer operator which adds 3 and multiplies the result by 6"})
    public LongFlow(LongUnaryOperator... ops) {
        this.ops = ops;
    }

    @Override
    public long applyAsLong(long operand) {
        long value = operand;
        for (LongUnaryOperator op : ops) {
            value = op.applyAsLong(value);
        }
        return value;
    }
}
