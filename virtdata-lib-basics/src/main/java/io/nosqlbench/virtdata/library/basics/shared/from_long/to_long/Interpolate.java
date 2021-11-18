package io.nosqlbench.virtdata.library.basics.shared.from_long.to_long;


import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.LongUnaryOperator;

@ThreadSafeMapper
public class Interpolate implements LongUnaryOperator {

    private final io.nosqlbench.virtdata.library.basics.shared.from_long.to_double.Interpolate basefunc;

    @Example({"Interpolate(0.0d,100.0d)","return a uniform long value between 0L and 100L"})
    @Example({"Interpolate(0.0d,90.0d,95.0d,98.0d,100.0d)","return a weighted long value where the first second and third quartiles are 90.0D, 95.0D, and 98.0D"})
    public Interpolate(double... values) {
        this.basefunc = new io.nosqlbench.virtdata.library.basics.shared.from_long.to_double.Interpolate(values);
    }

    @Override
    public long applyAsLong(long input) {
        return (long) basefunc.applyAsDouble(input);
    }
}
