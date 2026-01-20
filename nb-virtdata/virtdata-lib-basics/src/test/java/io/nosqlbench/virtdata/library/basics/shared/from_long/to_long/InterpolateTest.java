/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.virtdata.library.basics.shared.from_long.to_long;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.util.function.LongToDoubleFunction;
import java.util.function.LongUnaryOperator;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
public class InterpolateTest {
    private final static Logger logger = LogManager.getLogger(InterpolateTest.class);
    private static int iterations = 1000000;

    @Test
    public void testRanging() {
        io.nosqlbench.virtdata.library.basics.shared.from_long.to_double.Interpolate interpolate =
            new io.nosqlbench.virtdata.library.basics.shared.from_long.to_double.Interpolate (0.0d, 1.0d);
        DescriptiveStatistics dss = tabulate(new Hash(),interpolate, iterations);
        assertThat(dss.getPercentile(0.000001)).isCloseTo(0.0, Offset.offset(0.01));
        assertThat(dss.getPercentile(50.0)).isCloseTo(0.5,Offset.offset(0.01));
        assertThat(dss.getPercentile(99.99999)).isCloseTo(1.0, Offset.offset(0.01));
    }

    @Test
    public void testShaping() {
        double[] shape = new double[]{0.0,0.9,0.95,1.0};
        io.nosqlbench.virtdata.library.basics.shared.from_long.to_double.Interpolate interpolate =
            new io.nosqlbench.virtdata.library.basics.shared.from_long.to_double.Interpolate (shape);
        DescriptiveStatistics dss = tabulate(new Hash(),interpolate, iterations);
        assertThat(dss.getPercentile(0.000001)).isCloseTo(0.0, Offset.offset(0.01));
        assertThat(dss.getPercentile(50.0)).isCloseTo(0.5,Offset.offset(0.925));
        assertThat(dss.getPercentile(99.99999)).isCloseTo(1.0, Offset.offset(0.01));
    }

    public static DescriptiveStatistics tabulate(LongUnaryOperator bias,
        LongToDoubleFunction f, int count) {
        DescriptiveStatistics dss = new DescriptiveStatistics();
        for (long i = 0; i < count; i++) {
            long input = (long) (Long.MAX_VALUE * ((double)i/(double)count));
            long prn = bias.applyAsLong(input);
            double v = f.applyAsDouble(prn);
            dss.addValue(v);
        }
        return dss;
    }

    @Test
    public void testDeciles() {
        long topvalue = 1_000_000_000L;

        Interpolate t = new Interpolate(10L, 100L);
        long mint = t.applyAsLong(0L);
        assertThat(mint).isEqualTo(10L);
        long maxt = t.applyAsLong(Long.MAX_VALUE);
        assertThat(maxt).isEqualTo(100L);

        Interpolate f = new Interpolate(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, topvalue);
        long min = f.applyAsLong(0L);
        assertThat(min).isEqualTo(0L);

        long expected = (long)(((double)topvalue) * .8);
        logger.debug("expected long at 80% of maximum value:" + expected);

        long highvalue = (long) (Long.MAX_VALUE * 0.98d);
        long high = f.applyAsLong(highvalue);
        assertThat(high).isEqualTo(expected);
        logger.debug(" -> was " + high);

        long highervalue = (long) (Long.MAX_VALUE * 0.9999d);
        long higher = f.applyAsLong(highervalue);
        assertThat(higher).isEqualTo(999000000L);

        long max = f.applyAsLong(Long.MAX_VALUE);
        assertThat(max).isEqualTo(1000000000L);

    }
}
