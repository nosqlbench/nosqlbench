package io.virtdata.libbasics.shared.unary_int;

import io.virtdata.annotations.Example;
import io.virtdata.annotations.ThreadSafeMapper;

import java.util.function.IntUnaryOperator;

/**
 * Yields a value within a specified range, which rolls over continuously.
 */
@ThreadSafeMapper
public class CycleRange implements IntUnaryOperator {

    private final int minValue;
    private final int width;

    /**
     * Sets the maximum value of the cycle range. The minimum is default to 0.
     * @param maxValue The maximum value in the cycle to be added.
     */
    @Example({"CycleRange(34)","add a rotating value between 0 and 34 to the input"})
    public CycleRange(int maxValue) {
        this(0,maxValue);
    }

    /**
     * Sets the minimum and maximum value of the cycle range.
     * @param minValue minimum value of the cycle to be added.
     * @param maxValue maximum value of the cycle to be added.
     */
    public CycleRange(int minValue, int maxValue) {
        this.minValue = minValue;

        if (maxValue<minValue) {
            throw new RuntimeException("CycleRange must have min and max value in that order.");
        }
        this.width = maxValue - minValue;
    }

    @Override
    public int applyAsInt(int operand) {
        return minValue + (operand % width);
    }
}
