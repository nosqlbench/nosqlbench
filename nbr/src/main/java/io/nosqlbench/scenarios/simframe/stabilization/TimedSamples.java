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

package io.nosqlbench.scenarios.simframe.stabilization;

import java.util.*;

public class TimedSamples extends LinkedList<TimedSample> {

    private long startAt;
    public TimedSamples(long startTimeMs) {
        this.startAt =startTimeMs;
    }

    public boolean isStable() {
        if ((System.currentTimeMillis()-this.startAt) < 1000) return false;
        if (this.size()<50) return false;

        int[] ranges=new int[]{100,50,25,10};
        double[] avgs = new double[ranges.length];
        for (int i = 0; i < ranges.length; i++) {
            int range = ranges[i];
            if (size()>=range) {
                double avg=0.0d;
                ListIterator<TimedSample> iter = listIterator(size() - range);
                while (iter.hasNext()) avg += (iter.next().value() / range);
                avgs[i]=avg;
            } else {
                avgs[i]=Double.NaN;
            }
        }
        return false;

    }

//    private double[] stdDevDecades(double[] values) {
//        int offset=(values.length%10);
//        int[] decades = new int[values.length/10];
//        for (int d = 0; d<=decades.length; d++) {
//            int start = (d*10)+offset;
//            int end = start+10;
//            for (int idx = start; idx < end; idx++) {
//
//            }
//        }
//        for (int i = offset; i < values.length; i++) {
//
//        }
//        double[] v = new double[values.length];
//        for (int idx = 0; idx <values.length; idx++) {
//            v[idx]=values[(values.length-1)-idx];
//        }
//
//
//    }

    private double stddev(double[] values) {
        double acc= 0.0d;
        for (double value : values) {
            acc+=value;
        }
        var mean = acc/values.length;
        acc=0.0d;
        for(double value : values) {
            acc += (mean-value)*(mean-value);
        }
        var meanDiffs=acc/values.length;
        return Math.sqrt(meanDiffs);
    }

    private static double moving20Avg() {
        return 0;
    }

    public double addAndGet(long milliTime, double newValue) {
        add(new TimedSample(milliTime, newValue));
        return newValue;
    }

}
