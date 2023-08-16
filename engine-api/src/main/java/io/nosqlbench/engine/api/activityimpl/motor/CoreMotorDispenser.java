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
package io.nosqlbench.engine.api.activityimpl.motor;

import io.nosqlbench.api.engine.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityapi.core.*;
import io.nosqlbench.engine.api.activityapi.input.Input;
import io.nosqlbench.engine.api.activityapi.input.InputDispenser;
import io.nosqlbench.engine.api.activityapi.output.Output;
import io.nosqlbench.engine.api.activityapi.output.OutputDispenser;

import java.util.function.IntPredicate;

/**
 * Produce index ActivityMotor instances with an input and action,
 * given the input and an action factory.
 */
public class CoreMotorDispenser<D> implements MotorDispenser<D> {

    private final Activity activity;
    private final InputDispenser inputDispenser;
    private final ActionDispenser actionDispenser;
    private final OutputDispenser outputDispenser;

    public CoreMotorDispenser(Activity activity,
                              InputDispenser inputDispenser,
                              ActionDispenser actionDispenser,
                              OutputDispenser outputDispenser
                              ) {
        this.activity = activity;
        this.inputDispenser = inputDispenser;
        this.actionDispenser = actionDispenser;
        this.outputDispenser = outputDispenser;
    }

    @Override
    public Motor<D> getMotor(ActivityDef activityDef, int slotId) {
        Action action = actionDispenser.getAction(slotId);
        Input input = inputDispenser.getInput(slotId);
        Output output = null;
        if (outputDispenser !=null) {
            output = outputDispenser.getOutput(slotId);
        }
        IntPredicate resultFilter = null;
        Motor<D> am = new CoreMotor<>(activity, slotId, input, action, output);
        return am;
    }
}
