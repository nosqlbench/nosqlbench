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

package io.nosqlbench.scenarios.findmax;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.DoubleSupplier;
import java.util.function.LongSupplier;

/**
 * This is a helper class that makes it easy to bundle up a combination of measurable
 * factors and get a windowed sample from them. To use it, add your named data sources
 * with their coefficients, and optionally a callback which resets the measurement
 * buffers for the next time. When you call {@link #getValue()}, all callbacks
 * are used after the value computation is complete.
 *
 * <P>This is NOT thread safe!</P>
 */
public class SimFrameCapture implements SimFrameResults {
    private final List<Criterion> criteria = new ArrayList<>();
    private final FrameSamples allFrames = new FrameSamples();
    private FrameSampleSet currentFrame;


    public void addDirect(String name, DoubleSupplier supplier, double weight, Runnable callback) {
        this.criteria.add(new Criterion(name, supplier, weight, callback, false));
    }

    public void addDirect(String name, DoubleSupplier supplier, double weight) {
        addDirect(name, supplier, weight, () -> {
        });
    }

    public void addDeltaTime(String name, DoubleSupplier supplier, double weight, Runnable callback) {
        this.criteria.add(new Criterion(name, supplier, weight, callback, true));
    }

    public void addDeltaTime(String name, DoubleSupplier supplier, double weight) {
        addDeltaTime(name, supplier, weight, () -> {
        });
    }

    public void addDeltaTime(String name, LongSupplier supplier, double weight) {
        addDeltaTime(name, () -> (double)supplier.getAsLong(), weight);
    }

    @Override
    public List<FrameSampleSet> history() {
        return Collections.unmodifiableList(this.allFrames);
    }
    @Override
    public double getValue() {
        if (allFrames.isEmpty()) {
            return Double.NaN;
        }
        return allFrames.getLast().value();
    }

    @Override
    public int size() {
        return this.allFrames.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("PERF VALUE=").append(getValue()).append("\n");
        sb.append("windows:\n" + allFrames.getLast().toString());
        return sb.toString();
    }

    public void startWindow() {
        startWindow(System.currentTimeMillis());
    }

    public void startWindow(long now) {
        if (currentFrame != null) {
            throw new RuntimeException("cant start window twice in a row. Must close window first");
        }
        int nextidx = this.allFrames.size();
        List<FrameSample> samples = criteria.stream().map(c -> FrameSample.init(c,nextidx).start(now)).toList();
        this.currentFrame = new FrameSampleSet(samples);
    }

    public void stopWindow() {
        stopWindow(System.currentTimeMillis());
    }

    public void stopWindow(long now) {
        for (int i = 0; i < currentFrame.size(); i++) {
            currentFrame.set(i, currentFrame.get(i).stop(now));
        }
        allFrames.add(currentFrame);
        currentFrame = null;
    }

    public static record Criterion(
        String name,
        DoubleSupplier supplier,
        double weight,
        Runnable callback,
        boolean delta
    ) {
    }

    public FrameSampleSet last() {
        return allFrames.getLast();
    }

    public static class FrameSamples extends ArrayList<FrameSampleSet> {
    }

    public static class FrameSampleSet extends ArrayList<FrameSample> {
        public FrameSampleSet(List<FrameSample> samples) {
            super(samples);
        }

        public int index() {
            return getLast().index();
        }
        public double value() {
            double product = 1.0;
            for (FrameSample sample : this) {
                product *= sample.weightedValue();
            }
            return product;
        }


        @Override
        public String toString() {
            StringBuilder sb= new StringBuilder();
            sb.append(String.format("FRAME %05d  VALUE %010.5f\n", index(), value())).append("\n");
            for (FrameSample frameSample : this) {
                sb.append(" > ").append(frameSample.toString()).append("\n");
            }
            return sb.toString();
        }
    }

    public static record FrameSample(Criterion criterion, int index, long startAt, long endAt, double startval, double endval) {
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

        public static FrameSample init(Criterion criterion, int index) {
            return new FrameSample(criterion, index, 0, 0, Double.NaN, Double.NaN);
        }

        public FrameSample start(long startTime) {
            criterion.callback.run();
            double v1 = criterion.supplier.getAsDouble();
            return new FrameSample(criterion, index, startTime, 0L, v1, Double.NaN);
        }

        public FrameSample stop(long stopTime) {
            double v2 = criterion.supplier.getAsDouble();
            return new FrameSample(criterion, index, startAt, stopTime, startval, v2);
        }

        @Override
        public String toString() {
            return String.format(
                "%20s %03d dt[%04.2f] dV[%010.5f] wV=%010.5f",
                criterion.name,
                index,
                seconds(),
                rawValue(),
                weightedValue()
            );
        }
    }

}
