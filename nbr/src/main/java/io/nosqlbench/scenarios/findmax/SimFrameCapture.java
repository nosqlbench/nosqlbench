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
import java.util.function.ToDoubleFunction;

/**
 * This is a helper class that makes it easy to bundle up a combination of measurable
 * factors and get a windowed sample from them. To use it, add your named data sources
 * with their coefficients, and optionally a frameStartCallback which resets the measurement
 * buffers for the next time. When you call {@link #getValue()}, all callbacks
 * are used after the value computation is complete.
 *
 * <P>This is NOT thread safe!</P>
 */
public class SimFrameCapture implements SimFrameResults {
    private final List<Criterion> criteria = new ArrayList<>();
    private final FrameSamples allFrames = new FrameSamples();
    private FrameSampleSet currentFrame;


    /**
     * Direct values are simply measured at the end of a frame.
     *
     * @param name
     *     measure name
     * @param supplier
     *     source of measurement
     * @param weight
     *     coefficient of weight for this measure
     * @param callback
     */
    private void add(String name, EvalType type, ToDoubleFunction<DoubleMap> remix, DoubleSupplier supplier, double weight, Runnable callback) {
        this.criteria.add(new Criterion(name, type, remix, supplier, weight, callback==null? () -> {} : callback));
    }

    /**
     * Direct values are simply measured at the end of a frame.
     *
     * @param name
     *     measure name
     * @param supplier
     *     source of measurement
     * @param weight
     *     coefficient of weight for this measure
     */
    public void addDirect(String name, DoubleSupplier supplier, double weight) {
        add(name, EvalType.direct, null, supplier, weight, null);
    }

    public void addDeltaTime(String name, DoubleSupplier supplier, double weight, Runnable callback) {
        this.criteria.add(new Criterion(name, EvalType.deltaT, null, supplier, weight, callback));
    }

    public void addDeltaTime(String name, DoubleSupplier supplier, double weight) {
        criteria.add(new Criterion(name, EvalType.deltaT, null, supplier, weight, null));
    }

    /**
     * Delta Time values are taken as the differential of the first and last values with respect
     * to time passing.
     *
     * @param name
     * @param supplier
     * @param weight
     */
    public void addDeltaTime(String name, LongSupplier supplier, double weight) {
        addDeltaTime(name, () -> (double) supplier.getAsLong(), weight);
    }

    /**
     * A remix function takes as its input the computed raw values of the other functions, irrespective
     * of their weights or weighting functions. At the end of a frame, each defined value is computed
     * in the order it was added for capture and then added to the results view, where it can be referenced
     * by subsequent functions. Thus, any remix values must be added after those value on which it depends.
     *
     * @param name
     *     The name of the remix value
     * @param remix
     *     A function which relies on previously computed raw values.
     * @param weight
     *     The weight to apply to the result of this value for the final frame sample value.
     * @param callback
     *     An optional callback to invoke when the frame starts
     */
    public void addRemix(String name, ToDoubleFunction<DoubleMap> remix, double weight, Runnable callback) {
        add(name, EvalType.remix, remix, null, weight, callback);
    }

    public void addRemix(String name, ToDoubleFunction<DoubleMap> remix, double weight) {
        add(name, EvalType.remix, remix, null, weight, null);
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
        sb.append("windows:\n").append(allFrames.getLast().toString());
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
        DoubleMap vars = new DoubleMap();
        List<FrameSample> samples = criteria.stream().map(c -> FrameSample.init(c, nextidx, vars).start(now)).toList();
        this.currentFrame = new FrameSampleSet(samples);
//        System.out.println("after start:\n"+ frameCaptureSummary(currentFrame));
    }

    private String frameCaptureSummary(FrameSampleSet currentFrame) {
        StringBuilder sb = new StringBuilder();
        for (FrameSample fs : this.currentFrame) {
            sb.append(fs.index()).append(" T:").append(fs.startAt()).append("-").append(fs.endAt()).append(" V:")
                .append(fs.startval()).append(",").append(fs.endval()).append("\n");
        }
        return sb.toString();
    }

    public void stopWindow() {
        stopWindow(System.currentTimeMillis());
    }

    public void stopWindow(long now) {
        for (int i = 0; i < currentFrame.size(); i++) {
            currentFrame.set(i, currentFrame.get(i).stop(now));
        }
        allFrames.add(currentFrame);
//        System.out.println("after stop:\n"+ frameCaptureSummary(currentFrame));
        currentFrame = null;
    }

    public FrameSampleSet last() {
        return allFrames.getLast();
    }

    public void addRemix(String name, ToDoubleFunction<DoubleMap> remix) {
        addRemix(name, remix, 1.0, null);
    }


    public static class FrameSamples extends ArrayList<FrameSampleSet> {
    }

}
