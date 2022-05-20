package io.nosqlbench.virtdata.library.basics.shared.from_long.to_double;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */



import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.LongToDoubleFunction;

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
 *
 * Developer Note: This is the canonical implementation of LERPing in NoSQLBench, so is
 * heavily documented. Any other LERP implementations should borrow directly from this,
 * embedding by default.
 */
@ThreadSafeMapper
@Categories({Category.general})
public class Interpolate implements LongToDoubleFunction {

    // How many values we have to pick from
    private final double resolution;

    // The lookup table
    private final double[] lut;

    /**
     * The scale of Long.MAX_VALUE and the unit interval scale factor are pre-combined
     * here to reduce the number of operations later.
     *
     * The LUT size is retained as the number of elements provided (resolution) + 1.
     * The +1 element serves as the N+1 index for when the unit interval sample is
     * 1.0. In other words, the maximum value is not a special case, as a duplicate
     * value is appended to the LUT instead.
     *
     * This size is the scale factor from the unit interval to the array index. Since
     * the input comes in as a long value, it is mapped from [0L, Long.MAX_VALUE] to
     * [0.0D, 1.0D] by multiplying by (1.0/(double)Long.MAX_VALUE). The long input
     * value can then be multiplied directly to yield a double in the range of
     * [0,LUT.length-1], which simplifies all remaining LERP math.
     *
     */
    private final double scaleToLongInterval;


    @Example({"Interpolate(0.0d,100.0d)", "return a uniform double value between 0.0d and 100.0d"})
    @Example({"Interpolate(0.0d,90.0d,95.0d,98.0d,100.0d)", "return a weighted double value where the first second and third quartiles are 90.0D, 95.0D, and 98.0D"})
    public Interpolate(double... values) {
        this.resolution = values.length;
        double[] doubles = new double[values.length + 1];
        System.arraycopy(values,0,doubles,0,values.length);
        doubles[doubles.length - 1] = doubles[doubles.length - 2];
        this.lut = doubles;
        this.scaleToLongInterval = (this.resolution - 1) * (1.0d / (double) Long.MAX_VALUE);
    }

    @Override
    public double applyAsDouble(long input) {
        // scale the input from [0,Long.MAX_VALUE] to [0.0,lut.length-1]
        double samplePoint = scaleToLongInterval * input;
        // truncate the sample point to the left index
        int leftidx = (int) samplePoint;
        // isolate the fractional component
        double fractional = samplePoint - leftidx;
        // take the sum of the left component and right component
        // scaled by closeness to fractional point within the interval, respectively
        double sample = (lut[leftidx] * (1.0d - fractional)) + (lut[leftidx + 1] * fractional);
        return sample;
    }

}
