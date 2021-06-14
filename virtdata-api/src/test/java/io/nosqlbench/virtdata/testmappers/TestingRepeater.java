package io.nosqlbench.virtdata.testmappers;

import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.LongUnaryOperator;

@ThreadSafeMapper
public class TestingRepeater implements LongUnaryOperator {
    private int repeat;

    public TestingRepeater(int repeat) {
        this.repeat = repeat;
    }

    @Override
    public long applyAsLong(long operand) {
        return Long.valueOf(String.valueOf(operand).repeat(repeat));
    }
}
