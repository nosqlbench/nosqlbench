/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.virtdata.library.curves4.continuous;

import io.nosqlbench.virtdata.library.curves4.continuous.long_double.Normal;
import io.nosqlbench.virtdata.library.curves4.continuous.long_double.Uniform;
import org.apache.commons.math4.stat.descriptive.DescriptiveStatistics;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import java.util.Formatter;
import java.util.Locale;
import java.util.function.LongToDoubleFunction;

import static org.assertj.core.api.Assertions.assertThat;

public class RealDistributionsValuesTest {


    @Test
    public void testComputedNormal() {
        RunData runData = iterateMapperDouble(new Normal(10.0,2.0,"compute"), 1000000,1);
        System.out.println(runData);
        assertThat(runData.getFractionalPercentile(0.5D))
                .isCloseTo(10.0D, Offset.offset(0.01D));
        assertThat(runData.getFractionalPercentile(0.4D))
                .isCloseTo(9.49D, Offset.offset(0.01D));
        assertThat(runData.getFractionalPercentile(0.3D))
                .isCloseTo(8.95D, Offset.offset(0.01D));
    }

    @Test
    public void testInterpolatedNormal() {
        RunData runData = iterateMapperDouble(new Normal(10.0,2.0,"interpolate"), 1000000,1);
        System.out.println(runData);
        assertThat(runData.getFractionalPercentile(0.5D))
                .isCloseTo(10.0D, Offset.offset(0.01D));
        assertThat(runData.getFractionalPercentile(0.4D))
                .isCloseTo(9.49D, Offset.offset(0.01D));
        assertThat(runData.getFractionalPercentile(0.3D))
                .isCloseTo(8.95D, Offset.offset(0.01D));
    }

    @Test
    public void testComputedUniform() {
        RunData runData = iterateMapperDouble(new Uniform(0.0,100.0,"compute"), 1000000,1);
        assertThat(runData.getFractionalPercentile(0.33D))
                .isCloseTo(33.33D, Offset.offset(1.0D));
        assertThat(runData.getFractionalPercentile(0.5D))
                .isCloseTo(50.0D, Offset.offset(1.0D));
        assertThat(runData.getFractionalPercentile(0.78D))
                .isCloseTo(78.0D, Offset.offset(1.0D));
        System.out.println(runData);
    }

    @Test
    public void testInterpolatedUniform() {
        RunData runData = iterateMapperDouble(new Uniform(0.0,100.0,"interpolate"), 1000000,1);
        assertThat(runData.getFractionalPercentile(0.33D))
                .isCloseTo(33.33D, Offset.offset(1.0D));
        assertThat(runData.getFractionalPercentile(0.5D))
                .isCloseTo(50.0D, Offset.offset(1.0D));
        assertThat(runData.getFractionalPercentile(0.78D))
                .isCloseTo(78.0D, Offset.offset(1.0D));
        System.out.println(runData);
    }

    @Test
    public void testInterpolatedMappedUniform() {
        Uniform mapper = new Uniform(0.0, 100.0, "map", "interpolate");
        RunData runData = iterateMapperDouble(mapper,10000000,Long.MAX_VALUE/10000000L);

        assertThat(runData.getFractionalPercentile(0.001D))
            .isCloseTo(0.0D, Offset.offset(1.0D));

        assertThat(runData.getFractionalPercentile(0.999D))
                .isCloseTo(099.99D, Offset.offset(1.0D));

        assertThat(mapper.applyAsDouble(Long.MAX_VALUE)).isCloseTo(100.0D, Offset.offset(1.0D));

        System.out.println(runData);
    }

    private RunData iterateMapperDouble(LongToDoubleFunction mapper, int iterations, long funcstep) {
        assertThat(mapper).isNotNull();

        double[] samples = new double[iterations];

        long time_generating = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            samples[i] = mapper.applyAsDouble(i*funcstep);
        }
        long time_generated = System.nanoTime();

        double ms = (double) (time_generated - time_generating) / 1000000.0D;
        return new RunData(iterations, samples, ms);
    }

    private static class RunData {
        public String spec;
        public int iterations;
        public double[] samples;
        public double ms;

        public RunData(int iterations, double[] samples, double ms) {

            this.iterations = iterations;
            this.samples = samples;
            this.ms = ms;
        }
        private DescriptiveStatistics stats;
        private DescriptiveStatistics getStats() {
            if (stats==null) {
                stats = new DescriptiveStatistics(samples);
            }
            return stats;
        }
        public double getFractionalPercentile(double point) {
            return getStats().getPercentile(point*100.0D);
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            DescriptiveStatistics s1 = new DescriptiveStatistics(samples);
            Formatter f = new Formatter(sb, Locale.US);
            f.format("exec time %5fms\n", ms);
            f.format("iterations: %d\n", iterations);
            f.format("iterations/ms: %5f\n", (iterations/ms));
            for (int i = 10; i < 100; i += 10) {
                double pctile = i;
                f.format("pctile %4d  %4f\n", i, s1.getPercentile(pctile));
            }
            return sb.toString();
        }

    }

}
