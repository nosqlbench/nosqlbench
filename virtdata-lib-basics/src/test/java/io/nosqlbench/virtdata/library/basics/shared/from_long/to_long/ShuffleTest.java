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

package io.nosqlbench.virtdata.library.basics.shared.from_long.to_long;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class ShuffleTest {
    private final static Logger logger = LogManager.getLogger(ShuffleTest.class);

    /**
     * Worst Case:
     *
     * Explanation of the chart below:
     *
     * Size is the register size used for the shift register test.
     * The first index (zero) in each line is the size of the requested period, not the actual
     * LFSR period being used. Cycle periods are limited to 2^N-1, so we use the next
     * larger register size for 2^N, which is the worst case for down-sampling.
     *
     * The values from left to right, are discrete histograms of how many of the samples taken
     * required that many down-sampling iterations. For example, for size 6, 17 samples required
     * only one LFSR iteration to fall within the range of 0..32, while 7 required 2, 4 required 3,
     * and so forth.
     *
     * This is simply a visual verification of the binomial shape to the down-sampling cost.
     *
     * The strange behavior before N=4 is because the minimum field size is 4, meaning 4 bits
     * in this case.
     * <pre>
     *  size   1: [1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1]
     *  size   2: [2, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1]
     *  size   3: [4, 2, 0, 1, 0, 0, 0, 0, 0, 0, 1]
     *  size   4: [8, 4, 2, 1, 1]
     *  size   5: [16, 9, 3, 2, 1, 0, 1]
     *  size   6: [32, 17, 7, 4, 2, 1, 0, 1]
     *  size   7: [64, 33, 15, 8, 4, 2, 1, 0, 1]
     *  size   8: [128, 64, 33, 15, 8, 4, 2, 1, 0, 1]
     *  size   9: [256, 128, 64, 33, 15, 8, 4, 2, 1, 0, 1]
     *  size  10: [512, 257, 127, 64, 32, 16, 8, 4, 2, 1, 0, 1]
     *  size  11: [1024, 512, 256, 129, 63, 32, 16, 8, 4, 2, 1, 0, 1]
     *  size  12: [2048, 1024, 513, 255, 128, 64, 32, 16, 8, 4, 2, 1, 0, 1]
     *  size  13: [4096, 2049, 1023, 512, 256, 128, 64, 32, 16, 8, 4, 2, 1, 0, 1]
     *  size  14: [8192, 4096, 2049, 1023, 512, 256, 128, 64, 32, 16, 8, 4, 2, 1, 0, 1]
     *  size  15: [16384, 8193, 4095, 2048, 1024, 512, 256, 128, 64, 32, 16, 8, 4, 2, 1, 0, 1]
     *  size  16: [32768, 16384, 8193, 4095, 2048, 1024, 512, 256, 128, 64, 32, 16, 8, 4, 2, 1, 0, 1]
     *  size  17: [65536, 32769, 16383, 8192, 4096, 2048, 1024, 512, 256, 128, 64, 32, 16, 8, 4, 2, 1, 0, 1]
     *  size  18: [131072, 65537, 32767, 16384, 8192, 4096, 2048, 1024, 512, 256, 128, 64, 32, 16, 8, 4, 2, 1, 0, 1]
     *  size  19: [262144, 131073, 65535, 32768, 16384, 8192, 4096, 2048, 1024, 512, 256, 128, 64, 32, 16, 8, 4, 2, 1, 0, 1]
     *  size  20: [524288, 262145, 131071, 65536, 32768, 16384, 8192, 4096, 2048, 1024, 512, 256, 128, 64, 32, 16, 8, 4, 2, 1, 0, 1]
     *  size  21: [1048576, 524289, 262143, 131072, 65536, 32768, 16384, 8192, 4096, 2048, 1024, 512, 256, 128, 64, 32, 16, 8, 4, 2, 1, 0, 1]
     *  size  22: [2097152, 1048577, 524287, 262144, 131072, 65536, 32768, 16384, 8192, 4096, 2048, 1024, 512, 256, 128, 64, 32, 16, 8, 4, 2, 1, 0, 1]
     *  size  23: [4194304, 2097153, 1048575, 524288, 262144, 131072, 65536, 32768, 16384, 8192, 4096, 2048, 1024, 512, 256, 128, 64, 32, 16, 8, 4, 2, 1, 0, 1]
     *  size  24: [8388608, 4194305, 2097151, 1048576, 524288, 262144, 131072, 65536, 32768, 16384, 8192, 4096, 2048, 1024, 512, 256, 128, 64, 32, 16, 8, 4, 2, 1, 0, 1]
     *  size  25: [16777216, 8388609, 4194303, 2097152, 1048576, 524288, 262144, 131072, 65536, 32768, 16384, 8192, 4096, 2048, 1024, 512, 256, 128, 64, 32, 16, 8, 4, 2, 1, 0, 1]
     *  size  26: [33554432, 16777217, 8388607, 4194304, 2097152, 1048576, 524288, 262144, 131072, 65536, 32768, 16384, 8192, 4096, 2048, 1024, 512, 256, 128, 64, 32, 16, 8, 4, 2, 1, 0, 1]
     *  size  27: [67108864, 33554433, 16777215, 8388608, 4194304, 2097152, 1048576, 524288, 262144, 131072, 65536, 32768, 16384, 8192, 4096, 2048, 1024, 512, 256, 128, 64, 32, 16, 8, 4, 2, 1, 0, 1]
     * </pre>
     */
    @Test
    @Disabled
    public void testWorstCaseThrough28Bits() {
        int min=1;
        int max=28;
        int[][] stats = new int[max+1][];
        stats[0]=new int[0];
        for (int i = min; i < max; i++) {
            int n=1<<(i-1);
            stats[i]=testRange(n);
            stats[i][0]=n;
        }
        for (int i = 1; i < max; i++) {
            System.out.format(" size %3d: %s\n", i, Arrays.toString(stats[i]));

        }
    }

    /**
     * Best Case:
     *
     * In contrast to the numbers above, when the requested period size was exactly 2^N-1,
     * down-sampling was not used even a single time above N=4.
     *
     * <pre>
     *  size   2: [1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1]
     *  size   3: [3, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1]
     *  size   4: [7, 3, 2, 1, 0, 1]
     *  size   5: [15, 15]
     *  size   6: [31, 31]
     *  size   7: [63, 63]
     *  size   8: [127, 127]
     *  size   9: [255, 255]
     *  size  10: [511, 511]
     *  size  11: [1023, 1023]
     *  size  12: [2047, 2047]
     *  size  13: [4095, 4095]
     *  size  14: [8191, 8191]
     *  size  15: [16383, 16383]
     *  size  16: [32767, 32767]
     *  size  17: [65535, 65535]
     *  size  18: [131071, 131071]
     *  size  19: [262143, 262143]
     *  size  20: [524287, 524287]
     *  size  21: [1048575, 1048575]
     *  size  22: [2097151, 2097151]
     *  size  23: [4194303, 4194303]
     *  size  24: [8388607, 8388607]
     *  size  25: [16777215, 16777215]
     *  size  26: [33554431, 33554431]
     *  size  27: [67108863, 67108863]
     *  size  28: [134217727, 134217727]
     *  size  29: [268435455, 268435455]
     *  size  30: [536870911, 536870911]
     * </pre>
     */
    @Test
    @Disabled
    public void testBestCaseThrough31Bits() {
        int min=1;
        int max=31;
        int[][] stats = new int[max+1][];
        stats[0]=new int[1];
        for (int i = min; i < max; i++) {
            int n=1<<(i-1);
            stats[i]=testRange(n-1);
            stats[i][0]=n-1;
        }
        for (int i = 1; i < max; i++) {
            System.out.format(" size %3d: %s\n", i, Arrays.toString(stats[i]));
        }
    }

    private int[] testRange(int max) {
        Shuffle shuffle = new Shuffle(0,max);
        long[] r=new long[max];
        for (int i = 1; i <= r.length; i++) {
            r[i-1]=shuffle.applyAsLong(i);
        }
        Arrays.sort(r);
        for (int i = 0; i < r.length; i++) {
            assertThat(r[i]).isEqualTo(i+1);
        }
//        return shuffle.stats;
        return new int[0];
    }

    @Test
    public void test16() {
        int max=16;
        Shuffle shuffle = new Shuffle(0,max);
        long[] r=new long[max];
        for (int i = 1; i <= r.length; i++) {
            r[i-1]=shuffle.applyAsLong(i);
        }
        logger.debug(Arrays.toString(r));
        Arrays.sort(r);

        logger.debug(Arrays.toString(r));
        for (int i = 0; i < r.length; i++) {
            assertThat(r[i]).isEqualTo(i+1);
        }
//        logger.debug("resampling stats for " + max + " values: " + Arrays.toString(shuffle.stats));
    }

    @Test
    @Disabled
    public void test97() {
        int max=97;
        Shuffle shuffle = new Shuffle(0,max);
        long[] r=new long[max];
        for (int i = 1; i <= r.length; i++) {
            r[i-1]=shuffle.applyAsLong(i);
        }
//        logger.debug(Arrays.toString(r));
        Arrays.sort(r);
//        logger.debug(Arrays.toString(r));
        for (int i = 0; i < r.length; i++) {
            assertThat(r[i]).isEqualTo(i+1);
        }
//        logger.debug("resampling stats for " + max + " values: " + Arrays.toString(shuffle.stats));

    }

    @Test
    @Disabled
    public void test1000000() {
        int max=1000000;

        Shuffle shuffle = new Shuffle(0,max);
        long[] r=new long[max];
        for (int i = 1; i <= r.length; i++) {
            r[i-1]=shuffle.applyAsLong(i);
        }
//        logger.debug(Arrays.toString(r));
        Arrays.sort(r);
//        logger.debug(Arrays.toString(r));
        for (int i = 0; i < r.length; i++) {
            assertThat(r[i]).isEqualTo(i+1);
        }
//        logger.debug("resampling stats for " + max + " values: " + Arrays.toString(shuffle.stats));
    }


}
