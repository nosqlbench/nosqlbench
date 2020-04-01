package io.nosqlbench.virtdata.library.basics.shared.from_long.to_long;


import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.LongUnaryOperator;

/**
 * Return a value along an interpolation curve. This allows you to sketch a basic
 * density curve and describe it simply with just a few values. The number of values
 * provided determines the resolution of the internal lookup table that is used for
 * interpolation. The first value is always the 0.0 anchoring point on the unit interval.
 * The last value is always the 1.0 anchoring point on the unit interval. This means
 * that in order to subdivide the density curve in an interesting way, you need to provide
 * a few more values in between them. Providing two values simply provides a uniform
 * sample between a minimum and maximum value.
 *
 * The input range of this function is, as many of the other functions in this library,
 * based on the valid range of positive long values, between 0L and Long.MAX_VALUE inclusive.
 * This means that if you want to combine interpolation on this curve with the effect of
 * pseudo-random sampling, you need to put a hash function ahead of it in the flow.
 */
@ThreadSafeMapper
public class Interpolate implements LongUnaryOperator {

    private io.nosqlbench.virtdata.library.basics.shared.from_long.to_double.Interpolate basefunc;

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
