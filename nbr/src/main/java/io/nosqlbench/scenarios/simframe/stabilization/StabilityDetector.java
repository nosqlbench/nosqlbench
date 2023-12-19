/*
 * Copyright (c) 2023 nosqlbench
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

package io.nosqlbench.scenarios.simframe.stabilization;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.OptionalDouble;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

public class StabilityDetector implements Runnable {
    private final static Logger logger = LogManager.getLogger(StabilityDetector.class);
    private final double timeSliceSeconds;
    private final double threshold;
    private final DoubleSupplier source;
    private final Supplier<String> summary;
    private StatBucket[] buckets;
    private int[] windows;
    private volatile boolean running = true;
    private long startedAt;
    private long nextCheckAt;
    private double detectionTime;

    /**
     * Configure a stability checker that reads values from a source on some timed loop,
     * computes the streaming standard deviation, computes the ratio of stabilization between
     * values from longer windows to shorter windows, and returns from its run method once
     * the computed ratio is higher than the min threshold.
     *
     * @param timeSliceSeconds
     *     How frequently to gather a sample. 0.1 is recommended to start
     * @param minThreshold
     *     The unit interval fractional stability measurement which must be met at a minimum in order to stop polling
     *     for stability
     * @param source
     *     The source of data to be added to the streaming std dev computations
     * @param windows
     *     The size of each window in the set of diminishing sizes. These contain the last N samples by size,
     *     respectively.
     */
    public StabilityDetector(
        double timeSliceSeconds,
        double minThreshold,
        DoubleSupplier source,
        Supplier<String> summary,
        int... windows
    ) {
        if (windows.length < 2) {
            throw new RuntimeException("you must provide at least to summarization windows, ordered in decreasing size.");
        }
        this.timeSliceSeconds = timeSliceSeconds;
        this.threshold = minThreshold;
        this.source = source;
        this.summary = summary;
        this.windows = windows;
        for (int i = 0; i < windows.length - 1; i++) {
            if (windows[i] < windows[i + 1]) {
                throw new RuntimeException("windows must be provided in descending size, but you specified " + List.of(windows));
            }
        }

    }

    private void reset() {
        detectionTime = -1L;
        this.buckets = new StatBucket[windows.length];
        for (int i = 0; i < windows.length; i++) {
            buckets[i] = new StatBucket(windows[i]);
        }
    }

    public void apply(double value) {
        for (StatBucket bucket : buckets) {
            bucket.apply(value);
        }
//        return computeStability();
    }

    private boolean primed() {
        for (StatBucket bucket : buckets) {
            if (!bucket.primed()) {
                return false;
            }
        }
        return true;
    }


    private double computeStability() {
//        System.out.println("priming " + this.buckets[0].count() + "/" + this.buckets[0].ringbuf.size());
        if (!primed()) {
            return -1.0d;
        }
        double[] stddev = new double[buckets.length];
        for (int i = 0; i < buckets.length; i++) {
            stddev[i] = buckets[i].stddev();
        }
        double basis = 1.0d;

        for (int i = 0; i < buckets.length - 1; i++) {
            // if previous bigger window had a higher stddev than the one after, then it is converging
            double reductionFactor = (stddev[i + 1] / stddev[i]);
            basis *= reductionFactor;
        }

        // TODO: investigate why we get NaN sometimes and what it means for stability checks
        // TODO: turn this into a one line summary with some cool unicode characters
        double time = ((double)(nextCheckAt - startedAt))/1000d;


        if (time>10.0) {
            System.out.print(stabilitySummary(stddev));
            System.out.printf("% 4.1fS STABILITY %g :", time, basis);
            for (int i = 0; i < stddev.length; i++) {
                System.out.printf("[%d]: %g ", windows[i], stddev[i]);
            }
            System.out.println("stddevs: "+ Arrays.toString(stddev));
            System.out.printf(this.summary.get());
            System.out.println();
        }
        return basis;

    }

    /**
     * This run method is meant to be reused, since it resets internal state each time
     */
    @Override
    public void run() {
        try {
//            System.out.println("Detector> OPEN");
            updateAndAwait();
        } catch (Exception e) {
//            System.out.println("Detector> ERROR ERROR:" + e.toString());
            throw new RuntimeException(e);
        } finally {
//            System.out.println("Detector> CLOSE");
        }
    }

    // TODO: Add a check for when stddev is lower than some fixed value, or when both
    // (or all) windows are below some small threshold
    // and perhaps add auto-correlation checks for (any of) style unblocking
    private void updateAndAwait() {
        int interval = (int) (this.timeSliceSeconds * 1000);
        startedAt = System.currentTimeMillis();
        reset();

        nextCheckAt = startedAt + interval;

        while (running) {
            long delay = nextCheckAt - System.currentTimeMillis();
            while (delay > 0) {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ignored) {
                    System.out.println("Interrupted>");
                }
                delay = nextCheckAt - System.currentTimeMillis();
            }
            double value = source.getAsDouble();
            apply(value);
            double stabilityFactor = computeStability();
            if (Double.isNaN(stabilityFactor)) {
                throw new RuntimeException("NaN stability factor:" + this);
            }

            if (stabilityFactor > threshold) {
                detectionTime = ((double) (nextCheckAt - startedAt)) / 1000d;
                return;
            }
            nextCheckAt += interval;
        }
    }

    private static final String levels8 = " ▁▂▃▄▅▆▇";
    public String stabilitySummary(double[] stddev) {
        StringBuilder sb = new StringBuilder("[");
        double bias=(1.0d/16.0);
        double max=0.0d;
        for (int i = 0; i < stddev.length; i++) {
            max=Math.max(max,stddev[i]);
        }
        for (int i = 0; i < stddev.length; i++) {
            int idx = Math.min(7,((int)(stddev[i]/max)*levels8.length()));
            char c = levels8.charAt(idx);
            sb.append(c);
        }
        sb.append("] ");
        return sb.toString();
    }

    @Override
    public String toString() {
        if (detectionTime > 0L) {
            return String.format("results converged in % 4.2fS", detectionTime);
        } else {
            return String.format("awaiting convergence for % 4.2fS", (((double) (nextCheckAt - startedAt)) / 1000d));
        }
    }
}
