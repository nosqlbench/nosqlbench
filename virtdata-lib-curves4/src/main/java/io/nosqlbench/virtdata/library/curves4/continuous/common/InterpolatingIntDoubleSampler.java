package io.nosqlbench.virtdata.library.curves4.continuous.common;

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


import io.nosqlbench.virtdata.library.basics.shared.unary_int.Hash;

import java.util.Arrays;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntToDoubleFunction;

public class InterpolatingIntDoubleSampler implements IntToDoubleFunction{

    private final double[] lut;
    private final DoubleUnaryOperator f;
    private final boolean clamp;
    private final double clampMin;
    private final double clampMax;
    private final double scaleToIntRanged;
    private Hash hash;

    public InterpolatingIntDoubleSampler(DoubleUnaryOperator icdSource, int resolution, boolean hash, boolean clamp, double clampMin, double clampMax, boolean finite) {
        this.f = icdSource;
        this.clamp = clamp;
        this.clampMin = clampMin;
        this.clampMax = clampMax;
        if (hash) {
            this.hash = new Hash();
        }
        double[] computed = precompute(resolution);
        if (finite) {
            while (computed.length>0 && Double.isInfinite(computed[0])) {
                computed = Arrays.copyOfRange(computed,1,computed.length-1);
            }
            while (computed.length>0 && Double.isInfinite(computed[computed.length-1])) {
                computed = Arrays.copyOfRange(computed,0,computed.length-2);
            }
        }
        double[] padded = new double[computed.length+1];
        System.arraycopy(computed,0,padded,0,computed.length);
        this.scaleToIntRanged = (1.0d/(double)Integer.MAX_VALUE) * ((padded.length-2));
        this.lut = padded;
    }

    private double[] precompute(int resolution) {
        double[] precomputed = new double[resolution];
        for (int s = 0; s < resolution; s++) { // not a ranging error
            double rangedToUnit = (double) s / (double) resolution;
            double sampleValue = f.applyAsDouble(rangedToUnit);
            sampleValue = clamp ? Double.max(clampMin,Double.min(clampMax,sampleValue)) : sampleValue;
            precomputed[s] =  sampleValue;
        }
        return precomputed;
    }

    @Override
    public double applyAsDouble(int input) {
        if (hash!=null) {
            input = hash.applyAsInt(input);
        }
        double samplePoint = scaleToIntRanged * input;
        int leftidx = (int)samplePoint;
        double fractional = samplePoint - leftidx;
        double sample = (lut[leftidx]* (1.0d-fractional)) + (lut[leftidx+1] * fractional);
        return sample;
    }
}
