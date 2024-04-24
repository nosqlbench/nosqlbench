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

import java.util.Objects;

public final class StatBucket {
    DoubleRing ringbuf;
    private double mean;
    private double dSquared = 0.0d;

    public StatBucket() {
        this(10);
    }

    public StatBucket(int sampleWindow) {
        this.ringbuf = new DoubleRing(sampleWindow);
    }
    public StatBucket(double[] samples) {
        this.ringbuf = new DoubleRing(samples);
    }

    public StatBucket apply(double value) {
//        System.out.println("stat->" + value + " bucket:" + toString());
        double popped = ringbuf.push(value);
        if (ringbuf.count() == 1) {
            mean = value;
            dSquared = 0.0d;
        } else if (Double.isNaN(popped)) {
            var newMean = mean + ((value - mean) / ringbuf.count());
            var dSquaredIncrement = ((value - newMean) * (value - mean));
            // If this value is too small to be interpreted as a double it gets converted to
            // zero, which is not what we want. So we use the smallest possible double value
            if (dSquaredIncrement == 0) dSquaredIncrement = Double.MIN_VALUE;
            dSquared += dSquaredIncrement;
            mean = newMean;
        } else {
            var meanIncrement = (value - popped) / ringbuf.count();
            var newMean = mean + meanIncrement;
            var dSquaredIncrement = ((value - popped) * (value - newMean + popped - mean));
            // If this value is too small to be interpreted as a double it gets converted to
            // zero, which is not what we want. So we use the smallest possible double value
            if (dSquaredIncrement == 0) dSquaredIncrement = Double.MIN_VALUE;
            var newDSquared = this.dSquared + dSquaredIncrement;
            mean = newMean;
            dSquared = newDSquared;
        }
        return this;
    }

    public double variance() {
        double variance = dSquared / ringbuf.count();
        return (variance < 0) ? Math.abs(variance) : variance;
    }

    public double stddev() {
        return Math.sqrt(variance());
    }

    public int count() {
        return ringbuf.count();
    }

    public double mean() {
        return mean;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (StatBucket) obj;
        return this.ringbuf.count() == that.ringbuf.count() &&
            Double.doubleToLongBits(this.mean) == Double.doubleToLongBits(that.mean);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ringbuf.count(), mean);
    }

    @Override
    public String toString() {
        return "StatBucket[" +
            "count=" + ringbuf.count() + ", " +
            "mean=" + mean + ", " +
            "stddev=" + stddev() + ']';
    }

    public boolean primed() {
        return this.count()== ringbuf.size();
    }
}
