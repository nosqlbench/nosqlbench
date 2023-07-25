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

import com.codahale.metrics.Timer;
import io.nosqlbench.engine.api.activityapi.core.Activity;
import io.nosqlbench.api.engine.util.Unit;

import java.time.Instant;

public class ActivityMetricProgressMeter implements ProgressMeterDisplay, CompletedMeter, RemainingMeter, ActiveMeter {

    private final Activity activity;
    private final Instant startInstant;
    private final Timer bindTimer;
    private final Timer cyclesTimer;

    public ActivityMetricProgressMeter(Activity activity) {
        this.activity = activity;
        this.startInstant = Instant.ofEpochMilli(activity.getStartedAtMillis());
        this.bindTimer = activity.getInstrumentation().getOrCreateBindTimer();
        this.cyclesTimer = activity.getInstrumentation().getOrCreateCyclesServiceTimer();
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
        double totalRecycles = 1.0d + activity.getActivityDef()
            .getParams()
            .getOptionalString("recycles")
            .flatMap(Unit::longCountFor)
            .orElse(0L);
        double totalCycles = activity.getActivityDef().getCycleCount() * totalRecycles;
        return totalCycles;
    }

    @Override
    public double getCurrentValue() {
        return activity.getInstrumentation().getOrCreateCyclesServiceTimer().getCount();
    }

    @Override
    public double getRemainingCount() {
        return getMaxValue()- getCurrentValue();
    }

    @Override
    public double getActiveOps() {
        return bindTimer.getCount()-cyclesTimer.getCount();
    }

    @Override
    public String toString() {
        return getSummary();
    }

    @Override
    public double getCompletedCount() {
        return cyclesTimer.getCount();
    }
}
