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

package io.nosqlbench.scenarios;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleSupplier;

/**
 * This is a helper class that makes it easy to bundle up a combination of measurable
 * factors and get a windowed sample from them. To use it, add your named data sources
 * with their coefficients, and optionally a callback which resets the measurement
 * buffers for the next time. When you call {@link #getCurrentWindowValue()}, all callbacks
 * are used after the value computation is complete.
 *
 * <P>This is NOT thread safe!</P>
 */
public class PerfWindowSampler {

    private final List<Criterion> criteria = new ArrayList<>();
    private boolean openWindow = false;

    private final static int STARTS = 0;
    private final static int ENDS = 1;
    private final static int WEIGHTED = 2;
    private final static int START_TIME = 3;
    private final static int END_TIME = 4;
    private final static int ARYSIZE = END_TIME+1;
    /**
     * window, measure, START,STOP,WEIGHTED
     */
    private double[][][] data;
    private int window = -1;


    void addDirect(String name, DoubleSupplier supplier, double weight, Runnable callback) {
        this.criteria.add(new Criterion(name, supplier, weight, callback, false));
    }

    void addDirect(String name, DoubleSupplier supplier, double weight) {
        addDirect(name, supplier, weight, () -> {
        });
    }

    void addDeltaTime(String name, DoubleSupplier supplier, double weight, Runnable callback) {
        this.criteria.add(new Criterion(name, supplier, weight, callback, true));
    }

    void addDeltaTime(String name, DoubleSupplier supplier, double weight) {
        addDeltaTime(name, supplier, weight, () -> {
        });
    }

    double getCurrentWindowValue() {
        if (openWindow) {
            throw new RuntimeException("invalid access to checkpoint value on open window.");
        }
        double product = 1.0d;
        if (data==null) {
            return Double.NaN;
        }
        double[][] values = data[window];

        for (int i = 0; i < criteria.size(); i++) {
            product *= values[i][WEIGHTED];
        }
        return product;
    }
    private double valueOf(int measuredItem) {
        double[] vals = data[window][measuredItem];

        if (criteria.get(measuredItem).delta) {
            return (vals[ENDS] - vals[STARTS]) / (vals[END_TIME] - vals[START_TIME])*1000.0d;
        } else {
            return vals[ENDS];
        }
    }


    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder("PERF " + (openWindow ? "OPENWINDOW! " : "" ) + "sampler value =").append(getCurrentWindowValue()).append("\n");
        for (int i = 0; i < criteria.size(); i++) {
            Criterion criterion = criteria.get(i);
            sb.append("->").append(criterion.name).append(" last=").append(valueOf(i)).append("\n");
        }
        return sb.toString();
    }

    public void startWindow() {
        startWindow(System.currentTimeMillis());

    }
    public void startWindow(long now) {
        openWindow=true;
        window++;
        if (this.data == null) {
            this.data = new double[1][criteria.size()][ARYSIZE];
        }
        if (this.window >=data.length) {
            double[][][] newary = new double[data.length<<1][criteria.size()][ARYSIZE];
            System.arraycopy(data,0,newary,0,data.length);
            this.data = newary;
        }
        for (int i = 0; i < criteria.size(); i++) {
            data[window][i][START_TIME] = now;
            Criterion criterion = criteria.get(i);
            if (criterion.delta) {
                data[window][i][STARTS] = criterion.supplier.getAsDouble();
            } else {
                data[window][i][STARTS] = Double.NaN;
            }
            criterion.callback.run();
        }
        for (Criterion criterion : criteria) {
            criterion.callback.run();
        }
    }

    public void stopWindow() {
        stopWindow(System.currentTimeMillis());
    }
    public void stopWindow(long now) {
        for (int i = 0; i < criteria.size(); i++) {
            data[window][i][END_TIME] = now;
            Criterion criterion = criteria.get(i);
            double endmark = criterion.supplier.getAsDouble();
            data[window][i][ENDS] = endmark;

            double sample = valueOf(i);
            data[window][i][WEIGHTED] = sample* criterion.weight;
        }
        openWindow=false;
    }

    public static record Criterion(
        String name,
        DoubleSupplier supplier,
        double weight,
        Runnable callback,
        boolean delta
    ) { }
}
