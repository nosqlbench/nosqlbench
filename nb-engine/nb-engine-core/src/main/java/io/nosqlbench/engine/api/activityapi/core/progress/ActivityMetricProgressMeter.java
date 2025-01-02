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

package io.nosqlbench.engine.api.activityapi.core.progress;

import io.nosqlbench.engine.api.activityimpl.uniform.Activity;
import io.nosqlbench.nb.api.engine.activityimpl.CyclesSpec;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricTimer;
import io.nosqlbench.nb.api.engine.util.Unit;

import java.time.Instant;

public class ActivityMetricProgressMeter
    implements ProgressMeterDisplay, CompletedMeter, RemainingMeter, ActiveMeter, ConcurrentMeter
{

    private final Activity activity;
    private final Instant startInstant;
    private final NBMetricTimer bindTimer;
    private final NBMetricTimer cyclesTimer;

    public ActivityMetricProgressMeter(Activity activity) {
        this.activity = activity;
        this.startInstant = Instant.ofEpochMilli(activity.getStartedAtMillis());
        this.bindTimer = activity.metrics.bindTimer;
        this.cyclesTimer = activity.metrics.cycleServiceTimer;
    }

    @Override
    public Instant getStartTime() {
        return startInstant;
    }

    @Override
    public String getProgressName() {
        return activity.getAlias();
    }


    @Override
    public double getMaxValue() {
        double total_recycles = CyclesSpec.parse(activity.getConfig().get("recycles"))
            .cycle_count();
        double total_cycles = CyclesSpec.parse(activity.getConfig().get("cycles")).cycle_count();
        double totalCycles = total_recycles * total_cycles;
        return totalCycles;
    }

    @Override
    public double getCurrentValue() {
        return bindTimer.getCount();
    }

    @Override
    public double getRemainingCount() {
        return getMaxValue() - getCurrentValue();
    }

    @Override
    public double getActiveOps() {
        return bindTimer.getCount() - cyclesTimer.getCount();
    }

    @Override
    public String toString() {
        return getSummary().toString();
    }

    @Override
    public double getCompletedCount() {
        return cyclesTimer.getCount();
    }


    @Override
    public int getConcurrency() {
        return activity.getConfig().getThreads();
    }
}
