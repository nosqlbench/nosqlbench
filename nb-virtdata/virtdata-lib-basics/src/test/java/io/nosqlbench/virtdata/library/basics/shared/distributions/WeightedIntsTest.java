package io.nosqlbench.virtdata.library.basics.shared.distributions;

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


import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class WeightedIntsTest {
    @Test
    public void testWeightedInts() {
        WeightedInts weightedInts = new WeightedInts("10:10 20:20: 30:30 40:40", "map");
        assertThat(weightedInts.applyAsInt(0L)).isEqualTo(10);
        assertThat(weightedInts.applyAsInt(1L)).isEqualTo(10);
        assertThat(weightedInts.applyAsInt(Long.MAX_VALUE)).isEqualTo(40);
        assertThat(weightedInts.applyAsInt(Long.MAX_VALUE-1L)).isEqualTo(40);
    }

    @Test
    public void testDistributionError() {
        WeightedInts weightedInts = new WeightedInts("10:10 20:20: 30:30 40:40");
        double[] weights =new double[100];

        long count = 1000000;
        for (long i = 0; i < count; i++) {
            int value = weightedInts.applyAsInt(i);
            weights[value]++;
        }

        // Verify that each label has been sampled at a frequency which is within
        // 0.1% of the expected value.
        Offset offset = Offset.offset(((double)count)/1000d);

        assertThat(weights[10]).isCloseTo(((double)count)*(10.d/100.d), offset);
        assertThat(weights[20]).isCloseTo(((double)count)*(20.d/100.d), offset);
        assertThat(weights[30]).isCloseTo(((double)count)*(30.d/100.d), offset);
        assertThat(weights[40]).isCloseTo(((double)count)*(40.d/100.d), offset);

    }

    @Test
    @Disabled("leaving here to show boundary check logic for PHI")
    public void boundaryCheck() {
        for (long i = 0; i < 100000000; i++) {
            double pad = ((double) i)*1.0;
            double denominator = ((double) Long.MAX_VALUE) + pad;
            double scaled = ((double) Long.MAX_VALUE) / denominator;
            if (scaled < 1.0d) {
                System.out.println("phi:" + i);
                break;
            }
        }

    }
}
