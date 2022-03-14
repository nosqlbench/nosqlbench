/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.engine.api.activityimpl;

import io.nosqlbench.engine.api.activityapi.core.ProgressMeter;
import io.nosqlbench.engine.api.activityapi.core.RunState;
import io.nosqlbench.engine.api.activityimpl.input.StateCapable;

public class ProgressAndStateMeter implements ProgressMeter, StateCapable {
    private final ProgressMeter meter;
    private final StateCapable statesrc;

    public ProgressAndStateMeter(ProgressMeter meter, StateCapable statesrc) {
        this.meter = meter;
        this.statesrc = statesrc;
    }

    @Override
    public String getProgressName() {
        return meter.getProgressName();
    }

    @Override
    public long getStartedAtMillis() {
        return meter.getStartedAtMillis();
    }

    @Override
    public long getProgressMin() {
        return meter.getProgressMin();
    }

    @Override
    public long getProgressCurrent() {
        return meter.getProgressCurrent();
    }

    @Override
    public long getProgressMax() {
        return meter.getProgressMax();
    }

    @Override
    public long getRecyclesCurrent() {
        return meter.getRecyclesCurrent();
    }

    @Override
    public long getRecyclesMax() {
        return meter.getRecyclesMax();
    }

    @Override
    public RunState getRunState() {
        return statesrc.getRunState();
    }
}
