package io.nosqlbench.virtdata.library.curves4.discrete.common;

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

import java.util.function.DoubleToIntFunction;
import java.util.function.IntToLongFunction;

public class InterpolatingIntLongSampler implements IntToLongFunction {

    private final double[] lut;
    private final DoubleToIntFunction f;
    private Hash hash;
    private final double scaleToIntRanged;

    public InterpolatingIntLongSampler(DoubleToIntFunction icdSource, int resolution, boolean hash) {
        this.f = icdSource;
        if (hash) {
            this.hash = new Hash();
        }
        double[] computed = precompute(resolution);
        double[] padded = new double[computed.length+1];
        System.arraycopy(computed,0,padded,0,computed.length);
        this.scaleToIntRanged = (1.0d / Integer.MAX_VALUE) * (padded.length-2);
        this.lut=padded;
    }

    private double[] precompute(int resolution) {
        double[] precomputed = new double[resolution];
        for (int s = 0; s < resolution; s++) { // not a ranging error
            double rangedToUnit = (double) s / (double) resolution;
            int sampleValue = f.applyAsInt(rangedToUnit);
            precomputed[s] =  sampleValue;
        }
        return precomputed;
    }

    @Override
    public long applyAsLong(int input) {
        if (hash!=null) {
            input = hash.applyAsInt(input);
        }
        double samplePoint = scaleToIntRanged * input;
        int leftidx = (int)samplePoint;
        double fractional = samplePoint - leftidx;
        double sample = (lut[leftidx]* (1.0d-fractional)) + (lut[leftidx+1] * fractional);
        return (long) sample;
    }
}
