package io.nosqlbench.activitytype.cql.datamappers.functions.contrib;


import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;

import java.util.function.IntUnaryOperator;

@ThreadSafeMapper
public class WrappedClustering implements IntUnaryOperator {

    @Override
    public int applyAsInt(int operand) {
        long longOperand = operand;
        long longOperandTimes15 = longOperand * 15;
        long integerMax = Integer.MAX_VALUE + 1;
        long integerMin = Integer.MIN_VALUE;
        long sign = (long) Math.pow((-1), longOperandTimes15/integerMax);
        if (sign > 0)
            return (int) (sign * (longOperandTimes15 % integerMax));
        else
            return (int) (integerMin - (sign * (longOperandTimes15 % integerMax)));
    }
}