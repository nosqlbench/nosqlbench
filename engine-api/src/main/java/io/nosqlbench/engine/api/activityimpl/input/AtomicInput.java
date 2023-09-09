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

import com.codahale.metrics.Gauge;
import io.nosqlbench.api.config.NBLabeledElement;
import io.nosqlbench.api.config.NBLabels;
import io.nosqlbench.api.engine.activityimpl.ActivityDef;
import io.nosqlbench.api.engine.activityimpl.CyclesSpec;
import io.nosqlbench.api.engine.metrics.ActivityMetrics;
import io.nosqlbench.api.engine.metrics.instruments.NBFunctionGauge;
import io.nosqlbench.engine.api.activityapi.core.ActivityDefObserver;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleSegment;
import io.nosqlbench.engine.api.activityapi.input.Input;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
public class AtomicInput implements Input, ActivityDefObserver, Gauge<Long>, NBLabeledElement {
    private final static Logger logger = LogManager.getLogger(AtomicInput.class);

    private final AtomicLong cycle_value = new AtomicLong(0L);
    private final AtomicLong cycles_min = new AtomicLong(0L);
    private final AtomicLong cycles_max = new AtomicLong(Long.MAX_VALUE);

    private final AtomicLong recycles_min = new AtomicLong(0L);
    private final AtomicLong recycle_value = new AtomicLong(0L);
    private final AtomicLong recycles_max = new AtomicLong(0L);
    private final long startedAt = System.currentTimeMillis();

    private final ActivityDef activityDef;
    private final NBLabeledElement parent;

    public AtomicInput(NBLabeledElement parent, ActivityDef activityDef) {
        this.parent = parent;
        this.activityDef = activityDef;
        onActivityDefUpdate(activityDef);
        ActivityMetrics.gauge(this, "input_cycles_first", new NBFunctionGauge(this, () -> (double)this.cycles_min.get()));
        ActivityMetrics.gauge(this, "input_cycles_last", new NBFunctionGauge(this, () -> (double)this.cycles_min.get()));
        ActivityMetrics.gauge(this, "input_cycle", new NBFunctionGauge(this, () -> (double)this.cycle_value.get()));
        ActivityMetrics.gauge(this, "input_cycles_total", new NBFunctionGauge(this, this::getTotalCycles));
        ActivityMetrics.gauge(this, "input_recycles_first", new NBFunctionGauge(this, () -> (double)this.recycles_min.get()));
        ActivityMetrics.gauge(this, "input_recycles_last", new NBFunctionGauge(this, () -> (double)this.recycles_max.get()));
        ActivityMetrics.gauge(this, "input_recycle", new NBFunctionGauge(this, () -> (double) this.recycle_value.get()));
        ActivityMetrics.gauge(this, "input_recycles_total", new NBFunctionGauge(this, this::getTotalRecycles));
    }

    private double getTotalRecycles() {
        return 0.0d;
    }

    private double getTotalCycles() {
        return 0.0d;
    }

    @Override
    public CycleSegment getInputSegment(int stride) {
        while (true) {
            long currentStrideStart = this.cycle_value.get();
            long nextStrideStart = currentStrideStart + stride;
            if (nextStrideStart > cycles_max.get()) { // This indicates a stride boundary crossing the end
                recycle_value.getAndIncrement();
                if (recycle_value.get() >= recycles_max.get()) {
                    logger.trace(() -> "Exhausted input for " + activityDef.getAlias() + " at " + currentStrideStart + ", recycle " +
                            "count " + recycle_value.get());
                    return null;
                } else {
                    cycle_value.set(cycles_min.get());
                    logger.trace(() -> "recycling input for " + activityDef.getAlias() + " recycle:" + recycle_value.get());
                    continue;
                }
            }
            if (cycle_value.compareAndSet(currentStrideStart, nextStrideStart)) {
                return new InputInterval.Segment(recycle_value.get(), currentStrideStart, nextStrideStart);
            }
        }
    }

    @Override
    public String toString() {
        return "AtomicInput{" +
                "cycleValue=" + cycle_value +
                ", min=" + cycles_min +
                ", max=" + cycles_max +
                ", activity=" + activityDef.getAlias() +
                '}';
    }

    @Override
    public void onActivityDefUpdate(ActivityDef activityDef) {
        CyclesSpec recyclesSpec = activityDef.getRecyclesSpec();
        CyclesSpec cyclesSpec = activityDef.getCyclesSpec();

        cycles_max.set(cyclesSpec.last_exclusive());
        if (cycles_min.get() != cyclesSpec.first_inclusive()) {
            logger.info(() -> "resetting cycle value to new start: cycle[" + cycles_min.get() + "->" + cyclesSpec.first_inclusive()+"] " +
                    " start["+cycle_value.get()+"->"+ cycles_min.get()+"]");
            cycles_min.set(cyclesSpec.first_inclusive());
            cycle_value.set(cycles_min.get());
        }

        recycles_max.set(recyclesSpec.last_exclusive());
        if (recycles_min.get() != recyclesSpec.first_inclusive()) {
            logger.info(() -> "resetting recycle value to new start: recycle[" + recycles_min.get() + "->" + recyclesSpec.first_inclusive()+"] " +
                    " start["+recycle_value.get()+"->"+ recycles_min.get()+"]");
            recycles_min.set(recyclesSpec.first_inclusive());
            recycle_value.set(recyclesSpec.first_inclusive());
        }
    }

    public long getStartedAtMillis() {
        return this.startedAt;
    }

    @Override
    public boolean isContiguous() {
        return true;
    }

    @Override
    public NBLabels getLabels() {
        return parent.getLabels();
    }

    @Override
    public Long getValue() {
        return this.cycle_value.get();
    }
}
