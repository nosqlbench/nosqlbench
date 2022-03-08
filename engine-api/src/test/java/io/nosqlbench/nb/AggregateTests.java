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

package io.nosqlbench.nb;

import org.HdrHistogram.DoubleHistogram;
import org.HdrHistogram.DoubleRecorder;
import org.junit.jupiter.api.Test;

import java.util.DoubleSummaryStatistics;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * These tests are for numerical demonstration of how aggregates can or can not be combined
 * when summarizing results. This is just easier to show people rather than go through
 * a proof, so we can get on with testing!
 */
public class AggregateTests {
    double[][] data = new double[][]{
        {1, 1, 1, 1, 1, 1, 1, 1, 1, 91},
        {15, 15, 15, 15, 15, 5, 5, 5, 5, 5},
        {1, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 4, 4, 5}
    };
    double[] mins = {1000000, 9, 9, 9, 9, 9, 9, 9, 9, 9, 8, 8, 8, 8, 8, 8, 8, 8, 7, 7, 7, 7, 7, 7, 7, 6, 6, 6, 6, 6, 6, 5, 5, 5, 5, 5, 4, 4, 4, 4, 3, 3, 3, 2, 2, 1, 0, 0, 0, 0};
    double[] medians = {0, 0, 1, 2, 3, 3, 4, 4, 5, 5, 6, 6, 6, 7, 7, 7, 8, 8, 8, 8, 9, 9, 9, 9, 9, 1000000, 9, 9, 9, 9, 8, 8, 8, 8, 7, 7, 7, 7, 6, 6, 6, 5, 5, 5, 4, 4, 3, 2, 0, 0};
    double[] maxes = {0, 0, 0, 0, 1, 2, 2, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 7, 8, 8, 8, 8, 8, 8, 8, 8, 9, 9, 9, 9, 9, 9, 9, 9, 9, 1000000};

    /**
     * Even though it may be counter-intuitive at first glance, averages of averages are not
     * true averages of the whole dataset. This example demonstrates that.
     */
    @Test
    public void testAvgAvg() {
        DoubleSummaryStatistics allstats = new DoubleSummaryStatistics();
        for (double[] datum : data) {
            for (double v : datum) {
                allstats.accept(v);
            }
        }

        DoubleSummaryStatistics aggstats = new DoubleSummaryStatistics();
        for (double[] datum : data) {
            DoubleSummaryStatistics series = new DoubleSummaryStatistics();
            for (double v : datum) {
                series.accept(v);
            }
            aggstats.accept(series.getAverage());
        }

        System.out.println("aggstats avg:" + aggstats.getAverage());
        System.out.println("allstats avg:" + allstats.getAverage());

        assertThat(aggstats.getAverage()).isNotEqualTo(allstats.getAverage());
    }

    /**
     * In contrast, the average of a given percentile *should* be close to the percentile of the whole dataset.
     * However, it will not be highly accurate. It should be useful for fingerprinting results, but not for
     * high-precision work. This test shows how very similar (exact in substance in this case) data can lead
     * you to believe that this is a numerically precise method, when it is not. When data is similar from histogram
     * to histogram, the results are more stable.
     */
    @Test
    public void testAvgPctiles() {
        testCurves(mins,medians,maxes);

    }

    private void testCurves(double[]... inputarys) {
        DoubleRecorder all = new DoubleRecorder(5);
        DoubleHistogram[] snapshots = new DoubleHistogram[inputarys.length];

        for (int i = 0; i < inputarys.length; i++) {
            DoubleRecorder recorder = new DoubleRecorder(5);
            for (double v : inputarys[i]) {
                recorder.recordValue(v);
                all.recordValue(v);
            }
            snapshots[i]=recorder.getIntervalHistogram();
            System.out.println(snapshot(snapshots[i],"ary[" + i + "]"));
        }

        DoubleHistogram histoall = all.getIntervalHistogram();
        System.out.println(snapshot(histoall, "all"));

        for (double pctile : new double[]{10, 20, 30, 40, 50, 60, 70, 80, 90, 95, 99, 99.9, 99.99}) {
            DoubleSummaryStatistics avgOfInputs = new DoubleSummaryStatistics();
            for (DoubleHistogram snapshot : snapshots) {
                avgOfInputs.accept(snapshot.getValueAtPercentile(pctile));
            }
            System.out.println("avg of " + pctile + " => " + String.format("%.3f",avgOfInputs.getAverage()) + " (min,max)=("+String.format("%.3f",avgOfInputs.getMin()) + "," +
                String.format("%.3f",avgOfInputs.getMax())+ ")");
            System.out.println("direct " + pctile + " => " + String.format("%.3f",histoall.getValueAtPercentile(pctile)));
            System.out.println();

        }

    }


    /**
     * This method uses a random seeding to create 10 random curves with reference points that are
     * then extrapolated to 10 resampled curves of 100 points each. This method will show
     * that when the histograms curves are not similar, the accuracy falls off.
     */
    @Test
    public void testAvgPctileRandomCurves() {
        Random r = new Random(System.currentTimeMillis());
        double[][] series = new double[10][];
        for (int i = 0; i < series.length; i++) {
            double[] prototype = new double[10];
            for (int j = 0; j < prototype.length; j++) {
                prototype[j]=r.nextDouble()*100;
            }

            System.out.print("proto[" + i + "] = ");
            for (double v : prototype) {
                System.out.print(String.format("% 3.0f ",v));
            }
            System.out.println();
            series[i]=resampleCurve(prototype,100);
        }

        testCurves(series);
    }


    @Test
    public void testResampler() {
        assertThat(resampleCurve(new double[]{1, 2, 3, 4, 5}, 5)).isEqualTo(new double[]{1, 2, 3, 4, 5});
        assertThat(resampleCurve(new double[]{1, 2, 3, 4, 400000}, 9)).isEqualTo(new double[]{1, 1.5, 2, 2.5, 3, 3.5, 4, 200002.0, 400000});
        assertThat(resampleCurve(new double[]{1, 2, 3, 4, 5}, 9)).isEqualTo(new double[]{1, 1.5, 2, 2.5, 3, 3.5, 4, 4.5, 5});
        assertThat(resampleCurve(new double[]{1, 2, 3, 4, 5}, 7)).isEqualTo(new double[]{1, 1.6666666666666665, 2.333333333333333, 3, 3.6666666666666665, 4.333333333333334, 5});
    }

    private static double[] resampleCurve(double[] input, int points) {
        if (points < input.length || input.length < 2) {
            throw new RuntimeException("Resampling only works for equal or larger numbers of samples greater than 2.");
        }

        double[] resampled = new double[points];

        // handle the index+1 for the only case which could be out of bounds
        // Set endpoints, interpolate everything else
        resampled[resampled.length - 1] = input[input.length - 1];

        for (int outidx = 0; outidx < points - 1; outidx++) {
            double ratio = ((double) outidx / ((double) resampled.length - 1));
            double samplepoint = ((double) (input.length - 1)) * ratio;
            double fractional = samplepoint - (long) samplepoint;
            int leftidx = (int) samplepoint;
            double leftComponent = input[leftidx] * (1.0d - fractional);
            double rightComponent = input[leftidx + 1] * fractional;
            resampled[outidx] = leftComponent + rightComponent;
        }

        return resampled;
    }


    private static String snapshot(DoubleHistogram h, String... preambles) {
        StringBuilder sb = new StringBuilder();
        for (String preamble : preambles) {
            sb.append(preamble).append("\n");
        }
        sb.append(String.format(" (count, min, mean, max) = (%d, %.3f, %.3f, %.3f)\n",
            h.getTotalCount(), h.getMinValue(), h.getMean(), h.getMaxValue()));
        sb.append("         p(25,50,75) = (" + getPctlSummary(h, 25, 50, 75) + ")\n");
        sb.append(" p(90,99,9.99,99.99) = (" + getPctlSummary(h, 90, 99, 99.9, 99.99) + ")");
        return sb.toString();
    }

    private static String getPctlSummary(DoubleHistogram h, double... percentiles) {
        StringBuilder sb = new StringBuilder();
        for (double percentile : percentiles) {
            sb.append(String.format("%.3f", h.getValueAtPercentile(percentile))).append(",");
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

}
