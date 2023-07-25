/*
 * Copyright (c) 2022-2023 nosqlbench
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
package io.nosqlbench.engine.api.activityimpl.input;

import io.nosqlbench.engine.api.activityapi.core.progress.ProgressMeterDisplay;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleSegment;
import io.nosqlbench.engine.api.activityapi.core.ActivityDefObserver;
import io.nosqlbench.engine.api.activityapi.core.progress.CycleMeter;
import io.nosqlbench.engine.api.activityapi.core.progress.ProgressCapable;
import io.nosqlbench.engine.api.activityapi.input.Input;
import io.nosqlbench.api.engine.activityimpl.ActivityDef;
import io.nosqlbench.api.engine.util.Unit;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.security.InvalidParameterException;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <p>TODO: This documentation is out of date as of 2.0.0
 * <p>This input will provide threadsafe access to a sequence of long values.</p>
 * <p>Changes to the cycles or the targetrate will affect the provided inputs.
 * If the min or max cycle is changed, then these are re-applied first to the
 * max cycle and then to the min cycle. If the min cycle is changed, then the
 * next cycle value is set to the assigned min value. Otherwise, the cycle
 * will continue as usual till it reaches the max value. The ability to start
 * the input while running by applying a new set of parameters makes it possible
 * to re-trigger a sequence of inputs during a test.</p>
 * <p>This input, and Inputs in general do not actively prevent usage of values
 * after the max value. They simply expose it to callers. It is up to the
 * caller to check the value to determine when the input is deemed "used up."</p>
 */
public class AtomicInput implements Input, ActivityDefObserver, ProgressCapable {
    private final static Logger logger = LogManager.getLogger(AtomicInput.class);

    private final AtomicLong cycleValue = new AtomicLong(0L);
    private final AtomicLong min = new AtomicLong(0L);
    private final AtomicLong max = new AtomicLong(Long.MAX_VALUE);

    private final AtomicLong recycleValue = new AtomicLong(0L);
    private final AtomicLong recycleMax = new AtomicLong(0L);
    private final long startedAt = System.currentTimeMillis();

    private final ActivityDef activityDef;

    public AtomicInput(ActivityDef activityDef) {
        this.activityDef = activityDef;
        onActivityDefUpdate(activityDef);
    }

    @Override
    public CycleSegment getInputSegment(int stride) {
        while (true) {
            long current = this.cycleValue.get();
            long next = current + stride;
            if (next > max.get()) {
                if (recycleValue.get() >= recycleMax.get()) {
                    logger.trace(() -> "Exhausted input for " + activityDef.getAlias() + " at " + current + ", recycle " +
                        "count " + recycleValue.get());
                    return null;
                } else {
                    if (cycleValue.compareAndSet(current, min.get() + stride)) {
                        recycleValue.getAndIncrement();
                        logger.trace(() -> "recycling input for " + activityDef.getAlias() + " recycle:" + recycleValue.get());
                        return new InputInterval.Segment(min.get(), min.get() + stride);
                    }
                }
            }
            if (cycleValue.compareAndSet(current, next)) {
                return new InputInterval.Segment(current, next);
            }
        }
    }

//    @Override
//    public double getProgress() {
//        return (double) (cycleValue.get() - min.get());
//    }
//
//    @Override
//    public double getTotal() {
//        return (double) (max.get() - min.get());
//    }
//
//    @Override
//    public AtomicInputProgress.Range getRange() {
//        return new Range(this);
//    }
//

    @Override
    public String toString() {
        return "AtomicInput{" +
            "cycleValue=" + cycleValue +
            ", min=" + min +
            ", max=" + max +
            ", activity=" + activityDef.getAlias() +
            '}';
    }

    @Override
    public void onActivityDefUpdate(ActivityDef activityDef) {

        if (activityDef.getCycleCount() == 0) {
            if (activityDef.getParams().containsKey("cycles")) {
                throw new RuntimeException("You specified cycles, but the range specified means zero cycles: " + activityDef.getParams().get("cycles"));
            }
        }

        long startCycle = activityDef.getStartCycle();
        long endCycle = activityDef.getEndCycle();
        if (startCycle > endCycle) {
            throw new InvalidParameterException("min (" + min + ") must be less than or equal to max (" + max + ")");
        }

        if (max.get() != endCycle) {
            max.set(endCycle);
        }

        if (min.get() != startCycle) {
            min.set(startCycle);
            cycleValue.set(min.get());
        }

        long recycles = activityDef.getParams().getOptionalString("recycles").flatMap(Unit::longCountFor).orElse(0L);
        this.recycleMax.set(recycles);
    }

    public long getStarteAtMillis() {
        return this.startedAt;
    }

    @Override
    public boolean isContiguous() {
        return true;
    }

    @Override
    public ProgressMeterDisplay getProgressMeter() {
        return new AtomicInputProgress(activityDef.getAlias(), this);
    }

    private static class AtomicInputProgress implements ProgressMeterDisplay, CycleMeter {
        private final AtomicInput input;
        private final String name;

        public AtomicInputProgress(String name, AtomicInput input) {
            this.name = name;
            this.input = input;
        }

        @Override
        public String getProgressName() {
            return name;
        }

        @Override
        public Instant getStartTime() {
            return Instant.ofEpochMilli(input.getStarteAtMillis());
        }

        @Override
        public double getMaxValue() {
            return ((double)input.recycleMax.get()+1.0d)*((double)input.max.get()-(double)input.min.get());
        }

        @Override
        public double getCurrentValue() {
            return ((double)input.recycleValue.get())*((double)input.max.get()-(double)input.min.get())
                +(double)input.cycleValue.get()-(double)input.min.get();
        }

        @Override
        public long getMinInputCycle() {
            return input.min.get();
        }

        @Override
        public long getCurrentInputCycle() {
            return input.cycleValue.get();
        }

        @Override
        public long getMaxInputCycle() {
            return input.max.get();
        }

        @Override
        public long getRecyclesCurrent() {
            return input.recycleValue.get();
        }

        @Override
        public long getRecyclesMax() {
            return input.recycleMax.get();
        }
    }
}
