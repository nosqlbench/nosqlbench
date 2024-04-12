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

import org.junit.jupiter.api.Test;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.LongSummaryStatistics;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class TriangularStepTest {

    private static final int LABEL=0;
    private static final int FREQUENCY=1;

    @Test
    public void testStepExample1() {
        TriangularStep e1 = new TriangularStep(100, 20);
        int[] runLengths = this.rleStatsFor(e1, 0, 10000);
//        System.out.println(Arrays.toString(runLengths));
        assertThat(IntStream.of(runLengths).min().orElseThrow()).isEqualTo(80L);
        assertThat(IntStream.of(runLengths).max().orElseThrow()).isEqualTo(120L);
    }

    @Test
    public void testStepExample2() {
        TriangularStep e1 = new TriangularStep(80, 10);
        int[] runLengths = this.rleStatsFor(e1, 0, 10000);
//        System.out.println(Arrays.toString(runLengths));
        assertThat(IntStream.of(runLengths).min().orElseThrow()).isEqualTo(70L);
        assertThat(IntStream.of(runLengths).max().orElseThrow()).isEqualTo(90L);
    }

    @Test
    public void testIncrementalVariance() {
        TriangularStep f = new TriangularStep(100, 0);
        assertThat(f.applyAsLong(0L)).isEqualTo(0L);
        assertThat(f.applyAsLong(1L)).isEqualTo(0L);
        assertThat(f.applyAsLong(99L)).isEqualTo(0L);
        assertThat(f.applyAsLong(100L)).isEqualTo(1L);
    }

    @Test
    public void testVariance() {
        long first=0;
        TriangularStep f = new TriangularStep(100,1);
        var rlestats = rleStatsFor(f, 0, 100000);
        LongSummaryStatistics stats99to101 = statsForRle((int) f.applyAsLong(first),rlestats);
        assertThat(stats99to101.getMin()).isEqualTo(99L);
        assertThat(stats99to101.getMax()).isEqualTo(101L);

        int[][] histo = histoFor(rlestats);
        LongSummaryStatistics histoStats = new LongSummaryStatistics();
        for (int[] ints : histo) {
            histoStats.accept(ints[LABEL]);
        }
        assertThat(histoStats.getAverage()).isEqualTo(100);
    }

    private int[] rleStatsFor(TriangularStep f, long firstTrialIncl, long lastTrialExcl) {
        long firstBucket = f.applyAsLong(firstTrialIncl);
        long lastBucket = f.applyAsLong(lastTrialExcl);
        if (firstBucket>Integer.MAX_VALUE||lastBucket>Integer.MAX_VALUE) {
            throw new InvalidParameterException("can't fit result data into range of ints from long [" + firstBucket + ","+lastBucket+"]");
        }
        int base = (int) firstBucket;
        int[] counts = new int[(((int) lastBucket-(int)firstBucket))+1];
        for (long trial=firstTrialIncl; trial < lastTrialExcl; trial++) {
            long result = f.applyAsLong(trial);
            counts[(int)(result-base)]++;
        }
        // remove last partial, as only the front initial partial is compensated
        counts= Arrays.copyOfRange(counts,0,counts.length-1);
        return counts;
    }

    private int[][] histoFor(int[] counts) {
        var minval = IntStream.of(counts).min().orElseThrow();
        var maxval = IntStream.of(counts).max().orElseThrow();

        int[][] histo = new int[(maxval-minval)+1][2];
        for (int i = 0; i <= histo[LABEL].length; i++) {
            histo[i][LABEL]=i+minval;
        }

        for (int count : counts) {
//            System.out.println(count);
            histo[count-minval][FREQUENCY]++;
        }
        return histo;
    }

    private LongSummaryStatistics statsForRle(int base, int[] counts) {
        LongSummaryStatistics stats = new LongSummaryStatistics();
        for (int element = 0; element < counts.length; element++) {
            int count = counts[element];
            if (count==0) {
                continue;
            }
            stats.accept(count);
        }
        return stats;
    }
}
