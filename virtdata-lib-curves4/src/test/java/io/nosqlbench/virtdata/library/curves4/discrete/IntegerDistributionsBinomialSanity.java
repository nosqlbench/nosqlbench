package io.nosqlbench.virtdata.library.curves4.discrete;

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


import io.nosqlbench.virtdata.library.curves4.discrete.common.DiscreteLongLongSampler;
import io.nosqlbench.virtdata.library.curves4.discrete.common.IntegerDistributionICDSource;
import org.apache.commons.statistics.distribution.BinomialDistribution;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class IntegerDistributionsBinomialSanity {

    private static final double[] binomial85steps = new double[]{
            0.00390d, 0.03125d, 0.10937d, 0.21875d, 0.27343d, 0.21875d, 0.10937d, 0.03125d, 0.00390d,
    };

    @Test
    public void testBinomialMappedDist() {
        DiscreteLongLongSampler b85 = new DiscreteLongLongSampler(new IntegerDistributionICDSource(
                new BinomialDistribution(8, 0.5D)
        ),false);
        assertThat(b85.applyAsLong(0L)).isEqualTo(0);
        assertThat(b85.applyAsLong(Long.MAX_VALUE)).isEqualTo(8);
        double[] c = new double[binomial85steps.length];
        c[0]=binomial85steps[0];
        for (int i = 1; i < c.length; i++) {
            c[i] = c[i-1]+binomial85steps[i];
        }
        System.out.println("cumulative density points:"  + Arrays.toString(c));
        long[] t = Arrays.stream(c).mapToLong(d -> (long) (d * Long.MAX_VALUE)).toArray();
        double maxv = (double) Long.MAX_VALUE;

        double phi=0.001D;
        for (int b = 0; b < c.length-1; b++) {

            long beforeBoundary = (long)(Math.max(0.0D,(c[b])-phi)*maxv);
            double beforeDouble = (double)beforeBoundary / (double)Long.MAX_VALUE;
            long vb = b85.applyAsLong(beforeBoundary);

            System.out.println("vb:" + vb + ", before:" + b + " bb:" + beforeBoundary + ", reconverted: " + beforeDouble);
            System.out.flush();
            assertThat(vb).isEqualTo(b);

            long afterBoundary= (long)(Math.min(1.0D,(c[b])+phi)*maxv);
            double afterDouble = (double)afterBoundary / (double)Long.MAX_VALUE;
            long va = b85.applyAsLong(afterBoundary);
            System.out.println("va:" + va + " after:" + b + " ab:" + afterBoundary + ", reconverted: " + afterDouble);
            System.out.flush();
            assertThat(va).isEqualTo(b+1);

        }
//        assertThat(b85.applyAsInt((long)(c[0]*maxv))-1).isEqualTo(0);
//        assertThat(b85.applyAsInt((long)(c[0]*maxv))+1).isEqualTo(1);

    }

    @Disabled
    @Test
    public void showBinomialICDF() {
        DiscreteLongLongSampler b85 = new DiscreteLongLongSampler(new IntegerDistributionICDSource(
                new BinomialDistribution(8,0.5D)),false);
        for (int i = 0; i < 1000; i++) {
            double factor=((double) i / 1000D);
            long v = b85.applyAsLong((long) (factor * (double) Long.MAX_VALUE));
            System.out.println("i:" + i + ",f: " + factor + ", v:" + v);
        }

    }

}
