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

package io.virtdata;

import io.nosqlbench.virtdata.core.bindings.DataMapper;
import io.nosqlbench.virtdata.core.bindings.VirtData;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import java.util.Formatter;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

//import org.apache.commons.math4.stat.descriptive.DescriptiveStatistics;

public class IntegratedCurvesTest {

    @Test
    public void testZipf() {
        DataMapper<Long> mapper = VirtData.getMapper("Zipf(1000,2) -> long", long.class);
        RunData runData = iterateMapperLong(mapper, 10000);
        System.out.println(runData);

        assertThat(runData.getStats().getPercentile(0.1d)).isCloseTo(1.0, Offset.offset(0.01d));
        assertThat(runData.getStats().getPercentile(1.0d)).isCloseTo(1.0, Offset.offset(0.01d));
        assertThat(runData.getStats().getPercentile(10.0d)).isCloseTo(1.0, Offset.offset(0.01d));
        assertThat(runData.getStats().getPercentile(90.0d)).isCloseTo(6.0, Offset.offset(0.01d));
        assertThat(runData.getStats().getPercentile(99.0d)).isCloseTo(55.989, Offset.offset(0.01d));
        assertThat(runData.getStats().getPercentile(99.9d)).isCloseTo(202.999, Offset.offset(0.01d));

    }

    private RunData iterateMapperLong(DataMapper<Long> mapper, int iterations) {
        assertThat(mapper).isNotNull();

        double[] samples = new double[iterations];

        long time_generating = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            samples[i] = mapper.get(i);
        }
        long time_generated = System.nanoTime();

        double ms = (double) (time_generated - time_generating) / 1000000.0D;
        return new RunData(iterations, samples, ms);
    }

    private RunData iterateMapperDouble(DataMapper<Double> mapper, int iterations) {
        assertThat(mapper).isNotNull();

        double[] samples = new double[iterations];

        long time_generating = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            samples[i] = mapper.get(i);
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
        private DescriptiveStatistics stats;

        public RunData(int iterations, double[] samples, double ms) {

            this.iterations = iterations;
            this.samples = samples;
            this.ms = ms;
        }

        private DescriptiveStatistics getStats() {
            if (stats == null) {
                stats = new DescriptiveStatistics(samples);
            }
            return stats;
        }

        public double getFractionalPercentile(double point) {
            return getStats().getPercentile(point * 100.0D);
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            DescriptiveStatistics s1 = new DescriptiveStatistics(samples);
            Formatter f = new Formatter(sb, Locale.US);
            f.format("exec time %5fms\n", ms);
            f.format("iterations: %d\n", iterations);
            f.format("iterations/ms: %5f\n", (iterations / ms));
            for (int i = 10; i < 100; i += 10) {
                double pctile = i;
                f.format("pctile %4d  %4f\n", i, s1.getPercentile(pctile));
            }
            return sb.toString();
        }

    }


}
