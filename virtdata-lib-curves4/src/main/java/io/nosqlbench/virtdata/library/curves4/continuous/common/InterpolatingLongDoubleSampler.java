/*
 * Copyright (c) 2022-2023 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.virtdata.library.curves4.continuous.common;

import io.nosqlbench.virtdata.library.curves4.discrete.common.ThreadSafeHash;

import java.util.Arrays;
import java.util.function.DoubleUnaryOperator;
import java.util.function.LongToDoubleFunction;

/**
 * See {@link io.nosqlbench.virtdata.library.basics.shared.from_long.to_double.Interpolate} for
 * details on implementation.
 *
 * For the 6 implementations of interpolating samplers which use inverse cumulative distribution tables,
 * care should be given to the following:
 * <UL>
 *     <LI>Input Ranging - ensure that the input type is appropriate for the curve; pre-scaling needs to be matched
 *     to the input type</LI>
 *     <LI>resolution, scale, and LUT length; T</LI>
 *     <LI>+1 LUT padding for U=1.0</LI>
 *     <LI>Uniform LERP code in main function</LI>
 * </UL>>
 */
public class InterpolatingLongDoubleSampler implements LongToDoubleFunction {

    private static final double MAX_LONG_AS_DOUBLE = Long.MAX_VALUE;

    private final double[] lut;
    private final DoubleUnaryOperator f;
    private final boolean clamp;
    private final double clampMin;
    private final double clampMax;
    private final double scaleToLong;

    /**
     * If finite, then remove any infinite values from either end of the LUT due to long-tail approximation effects
     */
    private final boolean finite;
    private ThreadSafeHash hash;

    public InterpolatingLongDoubleSampler(DoubleUnaryOperator icdSource, int resolution, boolean hash, boolean clamp, double clampMin, double clampMax, boolean finite) {
        this.f = icdSource;
        if (hash) {
            this.hash = new ThreadSafeHash();
        }
        this.clamp=clamp;
        this.finite = finite;
        this.clampMin=clampMin;
        this.clampMax=clampMax;
        double[] computed = precompute(resolution);
        double[] padded = new double[computed.length+1];
        System.arraycopy(computed,0,padded,0,computed.length);
        this.scaleToLong = (1.0d / (double) Long.MAX_VALUE) * (padded.length-2);
        this.lut = padded;
    }

    private double[] precompute(int resolution) {
        double[] precomputed = new double[resolution];
        for (int s = 0; s < resolution; s++) { // not a ranging error
            double rangedToUnit = (double) s / (double) resolution;
            double sampleValue = f.applyAsDouble(rangedToUnit);
            sampleValue = clamp ? Double.max(clampMin,Double.min(clampMax,sampleValue)) : sampleValue ;
            precomputed[s] =  sampleValue;
        }
        int start = 0;
        int end = precomputed.length;
        while (finite && Double.isInfinite(precomputed[start])) {
            start++;
        }
        while (finite && Double.isInfinite(precomputed[end-1])) {
            end--;
        }
        precomputed = Arrays.copyOfRange(precomputed,start,end);
        return precomputed;
    }

    @Override
    public double applyAsDouble(long input) {
        if (hash!=null) {
            input = hash.applyAsLong(input);
        }
        double samplePoint = scaleToLong * input;
        int leftidx = (int)samplePoint;
        double fractional = samplePoint - leftidx;
        double sample = (lut[leftidx]* (1.0d-fractional)) + (lut[leftidx+1] * fractional);
        return sample;
    }
}
