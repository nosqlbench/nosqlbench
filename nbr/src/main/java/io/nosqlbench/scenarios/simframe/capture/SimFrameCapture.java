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

package io.nosqlbench.scenarios.simframe.capture;

import io.nosqlbench.scenarios.simframe.stabilization.StabilityDetector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.*;

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
    private FrameSampleSet activeFrame;

    private volatile boolean running = true;

    private final StabilityDetector stabilizer;


    public SimFrameCapture() {
        stabilizer = new StabilityDetector(0.1,0.98,this::getPartialValue, 10,5);
    }

    private double getPartialValue() {
        if (activeFrame ==null)  {
            return 0.0d;
        } else {
            return activeFrame.value();
        }
    }

    public void awaitSteadyState() {
        stabilizer.run();
        System.out.println(stabilizer);
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
     * @param callback
     */
    private void add(String name, EvalType type, ToDoubleFunction<BasisValues> remix, DoubleSupplier supplier, double weight, Runnable callback) {
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
     *     The weight to apply to the samples of this value for the final frame sample value.
     * @param callback
     *     An optional callback to invoke when the frame starts
     */
    public void addRemix(String name, ToDoubleFunction<BasisValues> remix, double weight, Runnable callback) {
        add(name, EvalType.remix, remix, null, weight, callback);
    }

    public void addRemix(String name, ToDoubleFunction<BasisValues> remix, double weight) {
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
    public void restartWindow() {
        restartWindow(System.currentTimeMillis());
    }

    public void restartWindow(long now) {
        int nextidx = this.allFrames.size();
        BasisValues vars = new BasisValues();
        List<FrameSample> samples = criteria.stream().map(c -> new FrameSample(c, nextidx, vars).start(now)).toList();
        this.activeFrame = new FrameSampleSet(samples);
    }

    public void startWindow(long now) {
        if (activeFrame != null) {
            throw new RuntimeException("cant start window twice in a row. Must close window first");
        }
        restartWindow(now);
    }

    private String frameCaptureSummary(FrameSampleSet currentFrame) {
        StringBuilder sb = new StringBuilder();
        for (FrameSample fs : this.activeFrame) {
            sb.append(fs.index()).append(" T:").append(fs.startAt()).append("-").append(fs.endAt()).append(" V:")
                .append(fs.startval()).append(",").append(fs.endval()).append("\n");
        }
        return sb.toString();
    }

    public void stopWindow() {
        stopWindow(System.currentTimeMillis());
    }

    public void stopWindow(long now) {
        for (int i = 0; i < activeFrame.size(); i++) {
            activeFrame.set(i, activeFrame.get(i).stop(now));
        }
        allFrames.add(activeFrame);
//        System.out.println("after stop:\n"+ frameCaptureSummary(currentFrame));
        activeFrame = null;
    }

    public FrameSampleSet last() {
        return allFrames.getLast();
    }

    public void addRemix(String name, ToDoubleFunction<BasisValues> remix) {
        addRemix(name, remix, 1.0, null);
    }

    public FrameSampleSet activeSample() {
        return activeFrame;
    }


    public static class FrameSamples extends ArrayList<FrameSampleSet> {
    }

}
