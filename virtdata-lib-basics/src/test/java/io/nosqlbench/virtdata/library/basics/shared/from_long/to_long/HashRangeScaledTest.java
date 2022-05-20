package io.nosqlbench.virtdata.library.basics.shared.from_long.to_long;

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


import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.Test;

import java.util.LongSummaryStatistics;

import static org.assertj.core.api.Assertions.assertThat;

public class HashRangeScaledTest {

    @Test
    public void testRanging() {
        HashRangeScaled hrs = new HashRangeScaled();
        for (long i = 0; i < 100; i++) {
            long l = hrs.applyAsLong(i);
            assertThat(l).isBetween(0L, i);
        }
    }

    @Test
    public void testHashRangeScaledLongs() {
        // This presumes a sliding triangular distribution in the data
        HashRangeScaled hrs = new HashRangeScaled();
        LongSummaryStatistics lss = new LongSummaryStatistics();

        long top = 1000000;
        for (long i = 0; i < top; i++) {
            lss.accept(hrs.applyAsLong(i));
        }
        System.out.println(lss);
        assertThat(lss.getAverage()).isCloseTo(top / 4d, Percentage.withPercentage(1d));
    }

    @Test
    public void testHashRangeScaledLongsHalf() {
        // This presumes a sliding triangular distribution in the data
        HashRangeScaled hrs = new HashRangeScaled(0.5d);
        LongSummaryStatistics lss = new LongSummaryStatistics();

        long top = 1000000;
        for (long i = 0; i < top; i++) {
            lss.accept(hrs.applyAsLong(i));
        }
        System.out.println(lss);
        assertThat(lss.getAverage()).isCloseTo(top / 8d, Percentage.withPercentage(1d));
    }

    @Test
    public void testHashRangeScaledLongsDoubled() {
        // This presumes a sliding triangular distribution in the data
        HashRangeScaled hrs = new HashRangeScaled(2d);
        LongSummaryStatistics lss = new LongSummaryStatistics();

        long top = 1000000;
        for (long i = 0; i < top; i++) {
            lss.accept(hrs.applyAsLong(i));
        }
        System.out.println(lss);
        assertThat(lss.getAverage()).isCloseTo(top / 2d, Percentage.withPercentage(1d));
    }


}
