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
 * buffers for the next time. When you call {@link #getValue()}, all callbacks
 * are used after the value computation is complete.
 *
 * <P>This is NOT thread safe!</P>
 */
public class PerfWindowSampler {

    private final List<Criterion> criteria = new ArrayList<>();
    private final WindowSamples windows = new WindowSamples();
    private WindowSample window;


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

    double getValue() {
        if (windows.size() == 0) {
            return Double.NaN;
        }
        return windows.getLast().value();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("PERF VALUE=").append(getValue()).append("\n");
        sb.append("windows:\n" + windows.getLast().toString());
        return sb.toString();
    }

    public void startWindow() {
        startWindow(System.currentTimeMillis());
    }

    public void startWindow(long now) {
        if (window != null) {
            throw new RuntimeException("cant start window twice in a row. Must close window first");
        }
        List<ParamSample> samples = criteria.stream().map(c -> ParamSample.init(c).start(now)).toList();
        this.window = new WindowSample(samples);
    }

    public void stopWindow() {
        stopWindow(System.currentTimeMillis());
    }

    public void stopWindow(long now) {
        for (int i = 0; i < window.size(); i++) {
            window.set(i, window.get(i).stop(now));
        }
        windows.add(window);
        window = null;
    }

    public static record Criterion(
        String name,
        DoubleSupplier supplier,
        double weight,
        Runnable callback,
        boolean delta
    ) {
    }

    public static class WindowSamples extends ArrayList<WindowSample> {
    }

    public static class WindowSample extends ArrayList<ParamSample> {
        public WindowSample(List<ParamSample> samples) {
            super(samples);
        }

        public double value() {
            double product = 1.0;
            for (ParamSample sample : this) {
                product *= sample.weightedValue();
            }

            return product;
        }
    }

    public static record ParamSample(Criterion criterion, long startAt, long endAt, double startval, double endval) {
        public double weightedValue() {
            return rawValue() * criterion().weight;
        }

        private double rawValue() {
            if (criterion.delta()) {
                return endval - startval;
            }
            return endval;
        }

        private double rate() {
            return rawValue() / seconds();
        }

        private double seconds() {
            return ((double) (endAt - startAt)) / 1000d;
        }

        public static ParamSample init(Criterion criterion) {
            return new ParamSample(criterion, 0, 0, Double.NaN, Double.NaN);
        }

        public ParamSample start(long startTime) {
            criterion.callback.run();
            double v1 = criterion.supplier.getAsDouble();
            return new ParamSample(criterion, startTime, 0L, v1, Double.NaN);
        }

        public ParamSample stop(long stopTime) {
            double v2 = criterion.supplier.getAsDouble();
            return new ParamSample(criterion, startAt, stopTime, startval, v2);
        }

        @Override
        public String toString() {
            return "sample[" + criterion.name() + "] "
                + ((Double.isNaN(endval)) ? " incomplete" : "dT:" + seconds() + " dV:" + rawValue() + " rate:" + rate() + " v1:" + startval + " v2:" + endval);
        }

    }
}
