package io.nosqlbench.engine.api.activityimpl.uniform;

/*
 * Copyright (c) nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import io.nosqlbench.engine.api.activityapi.core.ActionDispenser;
import io.nosqlbench.engine.api.activityapi.core.MotorDispenser;
import io.nosqlbench.engine.api.activityapi.cyclelog.filters.IntPredicateDispenser;
import io.nosqlbench.engine.api.activityapi.input.InputDispenser;
import io.nosqlbench.engine.api.activityapi.output.OutputDispenser;
import io.nosqlbench.nb.api.engine.activityimpl.ActivityConfig;

public class ActivityWiring {

    private final ActivityConfig activityDef;
    private MotorDispenser<?> motorDispenser;
    private InputDispenser inputDispenser;
    private ActionDispenser actionDispenser;
    private OutputDispenser markerDispenser;
    private IntPredicateDispenser resultFilterDispenser;

    public ActivityWiring(ActivityConfig activityDef) {
        this.activityDef = activityDef;
    }

    public static ActivityWiring of(ActivityConfig activityDef) {
        return new ActivityWiring(activityDef);
    }

    public ActivityConfig getConfig() {
        return activityDef;
    }

    public final MotorDispenser<?> getMotorDispenserDelegate() {
        return motorDispenser;
    }

    public final void setMotorDispenserDelegate(MotorDispenser<?> motorDispenser) {
        this.motorDispenser = motorDispenser;
    }

    public final InputDispenser getInputDispenserDelegate() {
        return inputDispenser;
    }

    public final void setInputDispenserDelegate(InputDispenser inputDispenser) {
        this.inputDispenser = inputDispenser;
    }

    public final ActionDispenser getActionDispenserDelegate() {
        return actionDispenser;
    }

    public final void setActionDispenserDelegate(ActionDispenser actionDispenser) {
        this.actionDispenser = actionDispenser;
    }

    public IntPredicateDispenser getResultFilterDispenserDelegate() {
        return resultFilterDispenser;
    }

    public void setResultFilterDispenserDelegate(IntPredicateDispenser resultFilterDispenser) {
        this.resultFilterDispenser = resultFilterDispenser;
    }

    public OutputDispenser getMarkerDispenserDelegate() {
        return this.markerDispenser;
    }

    public void setOutputDispenserDelegate(OutputDispenser outputDispenser) {
        this.markerDispenser = outputDispenser;
    }

}
