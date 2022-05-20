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


import java.util.function.DoubleToIntFunction;
import java.util.function.LongUnaryOperator;

public class InterpolatingLongLongSampler implements LongUnaryOperator {

    private final double[] lut;
    private final DoubleToIntFunction f;
    private ThreadSafeHash hash;
    private final double scaleToLong;

    public InterpolatingLongLongSampler(DoubleToIntFunction icdSource, int resolution, boolean hash) {
        this.f = icdSource;
        if (hash) {
            this.hash = new ThreadSafeHash();
        }
        double[] computed = precompute(resolution);
        double[] padded = new double[computed.length+1];
        System.arraycopy(computed,0,padded,0,computed.length);
        padded[padded.length-1] = padded[padded.length-2];
        scaleToLong = (1.0d/Long.MAX_VALUE) * ((double)(padded.length-2));
        this.lut = padded;
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
    public long applyAsLong(long input) {
        if (hash!=null) {
            input = hash.applyAsLong(input);
        }
        double samplePoint = scaleToLong * input;
        int leftidx = (int)samplePoint;
        double fractional = samplePoint - leftidx;
        double sample = (lut[leftidx]* (1.0d-fractional)) + (lut[leftidx+1] * fractional);
        return (long)sample;
    }
}
