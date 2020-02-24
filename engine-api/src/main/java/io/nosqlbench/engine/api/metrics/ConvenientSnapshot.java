/*
 *
 *    Copyright 2016 jshook
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * /
 */

package io.nosqlbench.engine.api.metrics;

import com.codahale.metrics.Snapshot;

import java.io.OutputStream;

public class ConvenientSnapshot extends Snapshot {

    private double NS_PER_S = 1000000000.0D;
    private double NS_PER_MS = 1000000.0D;
    private double NS_PER_US = 1000.0D;

    private Snapshot snapshot;
    ConvenientSnapshot(Snapshot snapshot) {
        this.snapshot = snapshot;
    }

    @Override
    public double getValue(double quantile) {
        return snapshot.getValue(quantile);
    }

    @Override
    public long[] getValues() {
        return snapshot.getValues();
    }

    @Override
    public int size() {
        return snapshot.size();
    }

    @Override
    public long getMax() {
        return snapshot.getMax();
    }

    @Override
    public double getMean() {
        return snapshot.getMean();
    }

    @Override
    public long getMin() {
        return snapshot.getMin();
    }

    @Override
    public double getStdDev() {
        return snapshot.getStdDev();
    }

    @Override
    public void dump(OutputStream output) {
        snapshot.dump(output);
    }


    public double getP50s() { return getValue(0.5D) / NS_PER_S; }
    public double getP75s() { return getValue(0.75D) / NS_PER_S; }
    public double getP90s() { return getValue(0.90D) / NS_PER_S; }
    public double getP95s() { return getValue(0.95D) / NS_PER_S; }
    public double getP98s() { return getValue(0.98D) / NS_PER_S; }
    public double getP99s() { return getValue(0.99D) / NS_PER_S; }
    public double getP999s() { return getValue(0.999D) / NS_PER_S; }
    public double getP9999s() { return getValue(0.9999D) / NS_PER_S; }

    public double getP50ms() { return getValue(0.5D) / NS_PER_MS; }
    public double getP75ms() { return getValue(0.75D) / NS_PER_MS; }
    public double getP90ms() { return getValue(0.90D) / NS_PER_MS; }
    public double getP95ms() { return getValue(0.95D) / NS_PER_MS; }
    public double getP98ms() { return getValue(0.98D) / NS_PER_MS; }
    public double getP99ms() { return getValue(0.99D) / NS_PER_MS; }
    public double getP999ms() { return getValue(0.999D) / NS_PER_MS; }
    public double getP9999ms() { return getValue(0.9999D) / NS_PER_MS; }

    public double getP50us() { return getValue(0.5D) / NS_PER_US; }
    public double getP75us() { return getValue(0.75D) / NS_PER_US; }
    public double getP90us() { return getValue(0.90D) / NS_PER_US; }
    public double getP95us() { return getValue(0.95D) / NS_PER_US; }
    public double getP98us() { return getValue(0.98D) / NS_PER_US; }
    public double getP99us() { return getValue(0.99D) / NS_PER_US; }
    public double getP999us() { return getValue(0.999D) / NS_PER_US; }
    public double getP9999us() { return getValue(0.9999D) / NS_PER_US; }

    public double getP50ns() { return getValue(0.5D); }
    public double getP75ns() { return getValue(0.75D); }
    public double getP90ns() { return getValue(0.90D); }
    public double getP95ns() { return getValue(0.95D); }
    public double getP98ns() { return getValue(0.98D); }
    public double getP99ns() { return getValue(0.99D); }
    public double getP999ns() { return getValue(0.999D); }
    public double getP9999ns() { return getValue(0.9999D); }


}
