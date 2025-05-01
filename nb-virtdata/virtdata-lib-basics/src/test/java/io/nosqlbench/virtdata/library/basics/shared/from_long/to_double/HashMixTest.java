package io.nosqlbench.virtdata.library.basics.shared.from_long.to_double;

/*
 * Copyright (c) nosqlbench
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


import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import java.util.function.LongToDoubleFunction;
import java.util.function.LongUnaryOperator;

import static org.assertj.core.api.Assertions.assertThat;

public class HashMixTest {

    private final static LongToDoubleFunction TO_UNIT_INTERVAL =
        (l) -> ((double) l) / ((double) Long.MAX_VALUE);
    private final static Object TO_UNIT_INTERVAL_OBJ = (Object) TO_UNIT_INTERVAL;

    @Test
    public void testLinearMix() {
        DoubleHolder dh = new DoubleHolder();
        LongHolder lh = new LongHolder();
        HashMix um1 = new HashMix(TO_UNIT_INTERVAL, TO_UNIT_INTERVAL,dh, lh);
        for (long i = 0; i >= 0L; i += 1L << 58) {
            double fraction = TO_UNIT_INTERVAL.applyAsDouble(i);
            lh.setValue(i);
            dh.setValue(fraction);
            double actual = um1.applyAsDouble(i);
            double expected = TO_UNIT_INTERVAL.applyAsDouble(i);
            assertThat(actual).isEqualTo(expected, Offset.offset(0.0000001d));
        }
    }

    @Test
    public void testCrossfadeMix() {
        LongToDoubleFunction rampdown1 = l -> 1.0d - TO_UNIT_INTERVAL.applyAsDouble(l);
        LongToDoubleFunction rampdown2 = l -> 2.0d - TO_UNIT_INTERVAL.applyAsDouble(l);
        HashMix um1 = new HashMix(rampdown1, rampdown2);
        for (long i = 0; i >= 0L; i += 1L << 58) {
            double value = um1.applyAsDouble(i);
            assertThat(um1.applyAsDouble(i)).isEqualTo(1.0d, Offset.offset(0.0000001d));
        }
    }

    private class DoubleHolder implements LongToDoubleFunction {

        private double value;

        @Override
        public double applyAsDouble(long value) {
            return this.value;
        }

        public void setValue(double value) {
            this.value = value;
        }
    }

    private class LongHolder implements LongUnaryOperator {
        private long value;

        @Override
        public long applyAsLong(long operand) {
            return this.value;
        }

        public void setValue(long value) {
            this.value = value;
        }
    }
}
