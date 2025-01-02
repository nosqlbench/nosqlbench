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
import io.nosqlbench.engine.api.activityimpl.uniform.Activity;
import io.nosqlbench.nb.api.config.standard.*;
import io.nosqlbench.nb.api.engine.activityimpl.ActivityConfig;
import io.nosqlbench.nb.api.engine.activityimpl.CyclesSpec;
import io.nosqlbench.nb.api.components.core.NBBaseComponent;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleSegment;
import io.nosqlbench.engine.api.activityapi.input.Input;
import io.nosqlbench.nb.api.engine.metrics.instruments.MetricCategory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.atomic.AtomicLong;

/**
 <p>TODO: This documentation is out of date as of 2.0.0
 <p>This input will provide threadsafe access to a sequence of long values.</p>
 <p>Changes to the cycles or the targetrate will affect the provided inputs.
 If the min or max cycle is changed, then these are re-applied first to the
 max cycle and then to the min cycle. If the min cycle is changed, then the
 next cycle value is set to the assigned min value. Otherwise, the cycle
 will continue as usual till it reaches the max value. The ability to start
 the input while running by applying a new set of parameters makes it possible
 to re-trigger a sequence of inputs during a test.</p>
 <p>This input, and Inputs in general do not actively prevent usage of values
 after the max value. They simply expose it to callers. It is up to the
 caller to check the value to determine when the input is deemed "used up."</p> */
public class AtomicInput extends NBBaseComponent
    implements Input, NBConfigurable, NBReconfigurable, Gauge<Long>
{
    private final static Logger logger = LogManager.getLogger(AtomicInput.class);

    private final AtomicLong cycle_value = new AtomicLong(0L);
    private final AtomicLong cycles_min = new AtomicLong(0L);
    private final AtomicLong cycles_max = new AtomicLong(Long.MAX_VALUE);

    private final AtomicLong recycles_min = new AtomicLong(0L);
    private final AtomicLong recycle_value = new AtomicLong(0L);
    private final AtomicLong recycles_max = new AtomicLong(0L);
    private final long startedAt = System.currentTimeMillis();
    private NBConfiguration config;

    public AtomicInput(Activity parent) {
        super(parent);
        applyConfig(parent.getConfig());
        create().gauge(
            "input_cycles_first", () -> (double) this.cycles_min.get(), MetricCategory.Config,
            "The first cycle of the cycle interval, inclusive");
        create().gauge(
            "input_cycles_last", () -> (double) this.cycles_max.get(), MetricCategory.Config,
            "The last cycle of the cycle interval, exclusive");
        create().gauge(
            "input_cycle", () -> (double) this.cycle_value.get(), MetricCategory.Core,
            "The next input cycle that will be dispatched to a thread");
        create().gauge(
            "input_cycles_total", this::getTotalCycles, MetricCategory.Config,
            "The total number of cycles to be executed");
        create().gauge(
            "input_recycles_first", () -> (double) this.recycles_min.get(), MetricCategory.Config,
            "The first recycle value, inclusive");
        create().gauge(
            "input_recycles_last", () -> (double) this.recycles_max.get(), MetricCategory.Config,
            "The last recycle value, exclusive");
        create().gauge(
            "input_recycle", () -> (double) this.recycle_value.get(), MetricCategory.Core,
            "The next recycle value that will be dispatched once cycles are completed");
        create().gauge(
            "input_recycles_total", this::getTotalRecycles, MetricCategory.Config,
            "The total number of recycles to be executed, within which each set of cycles will be executed");
    }

    private double getTotalRecycles() {
        return ((double) this.recycles_max.get()) - ((double) this.recycles_min.get());
    }

    private double getTotalCycles() {
        return ((double) this.cycles_max.get()) - ((double) this.cycles_min.get());
    }

    @Override
    public CycleSegment getInputSegment(int stride) {
        while (true) {
            long currentStrideStart = this.cycle_value.get();
            long nextStrideStart = currentStrideStart + stride;
            if (nextStrideStart >
                cycles_max.get())
            { // This indicates a stride boundary crossing the end
                recycle_value.getAndIncrement();
                if (recycle_value.get() >= recycles_max.get()) {
                    logger.trace(() -> "Exhausted input for " +
                                       description() +
                                       " at " +
                                       currentStrideStart +
                                       ", recycle " +
                                       "count " +
                                       recycle_value.get());
                    return null;
                } else {
                    cycle_value.set(cycles_min.get());
                    logger.trace(() -> "recycling input for " +
                                       description() +
                                       " recycle:" +
                                       recycle_value.get());
                    continue;
                }
            }
            if (cycle_value.compareAndSet(currentStrideStart, nextStrideStart)) {
                return new InputInterval.Segment(
                    recycle_value.get(), currentStrideStart,
                    nextStrideStart);
            }
        }
    }

    @Override
    public String toString() {
        return "AtomicInput(" +
               description() +
               "){cycleValue=" +
               cycle_value +
               ", min=" +
               cycles_min +
               ", max=" +
               cycles_max +
               '}';
    }


    @Override
    public void applyConfig(NBConfiguration cfg) {
        this.config = this.getConfigModel().matchConfig(cfg);
        CyclesSpec recyclesSpec = CyclesSpec.parse(cfg.get(ActivityConfig.FIELD_RECYCLES));
        CyclesSpec cyclesSpec = CyclesSpec.parse(cfg.get(ActivityConfig.FIELD_CYCLES));

        if (cyclesSpec.cycle_count() == 0) {
            throw new RuntimeException("You specified cycles, but the range specified means zero " +
                                       "cycles: " +
                                       cyclesSpec);
        }

        if (recyclesSpec.cycle_count() == 0) {
            throw new RuntimeException("You specified recycles, but the range specified means " +
                                       "zero " +
                                       "recycles: " +
                                       recyclesSpec);
        }

        cycles_max.set(cyclesSpec.last_exclusive());
        if (cycles_min.get() != cyclesSpec.first_inclusive()) {
            logger.info(() -> "resetting first cycle (inclusive) value to: cycle[" +
                              cycles_min.get() +
                              "->" +
                              cyclesSpec.first_inclusive() +
                              "] " +
                              " start[" +
                              cycle_value.get() +
                              "->" +
                              cycles_min.get() +
                              "]");
            cycles_min.set(cyclesSpec.first_inclusive());
            cycle_value.set(cycles_min.get());
        }
        if (cycles_max.get() != cyclesSpec.last_exclusive()) {
            logger.info(() -> "resetting last cycle (exclusive) value to: cycle[" +
                              cycles_max.get() +
                              "->" +
                              cyclesSpec.last_exclusive() +
                              "]");
            cycles_max.set(cyclesSpec.last_exclusive());
        }

        recycles_max.set(recyclesSpec.last_exclusive());
        if (recycles_min.get() != recyclesSpec.first_inclusive()) {
            logger.info(() -> "resetting recycle value to new start: recycle[" +
                              recycles_min.get() +
                              "->" +
                              recyclesSpec.first_inclusive() +
                              "] " +
                              " start[" +
                              recycle_value.get() +
                              "->" +
                              recycles_min.get() +
                              "]");
            recycles_min.set(recyclesSpec.first_inclusive());
            recycle_value.set(recyclesSpec.first_inclusive());
        }
        if (recycles_max.get() != recyclesSpec.last_exclusive()) {
            logger.info(() -> "resetting last recycle (exclusive) value to: recycle[" +
                              recycles_max.get() +
                              "->" +
                              recyclesSpec.last_exclusive() +
                              "]");
            recycles_max.set(recyclesSpec.last_exclusive());
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
    public Long getValue() {
        return this.cycle_value.get();
    }

    @Override
    public void applyReconfig(NBConfiguration recfg) {
        this.applyConfig(recfg);
    }

    @Override
    public NBConfigModel getReconfigModel() {
        return getConfigModel();
    }


    @Override
    public NBConfigModel getConfigModel() {
        return ConfigModel.of(AtomicInput.class)
            .add(Param.required(ActivityConfig.FIELD_CYCLES, String.class))
            .add(Param.required(ActivityConfig.FIELD_RECYCLES, String.class)).asReadOnly();
    }
}
