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

package io.nosqlbench.nb.api.stats;

public class DoubleRing {
    private final double[] dbuf;
    private int count;
    private int idx;

    public DoubleRing(int size) {
        this.dbuf = new double[size];
        this.count = 0;
    }

    public DoubleRing(double[] samples) {
        this.dbuf=samples;
        this.count =samples.length;
    }

    public double push(double value) {
        double ejected = (count == dbuf.length) ? dbuf[idx] : Double.NaN;
        count += (count < dbuf.length) ? 1 : 0;

        dbuf[idx] = value;
        idx = (idx + 1) % dbuf.length;
        return ejected;
    }

    public int size() {
        return dbuf.length;
    }

    public int count() {
        return count;
    }

    public double min() {
        double min = Double.MAX_VALUE;
        for (int i = 0; i < count; i++) {
            min = Math.min(min,dbuf[i]);
        }
        return min;
    }

    public double max() {
        double max = Double.MIN_VALUE;
        for (int i = 0; i < count; i++) {
            max = Math.max(max,dbuf[i]);
        }
        return max;
    }

}
