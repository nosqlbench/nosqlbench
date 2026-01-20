/*
 * Copyright (c) 2024 nosqlbench
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

import io.nosqlbench.engine.api.activityapi.core.ActionDispenser;
import io.nosqlbench.engine.api.activityapi.core.Activity;
import io.nosqlbench.engine.api.activityapi.core.Motor;
import io.nosqlbench.engine.api.activityapi.core.MotorDispenser;
import io.nosqlbench.engine.api.activityapi.core.StrideAction;
import io.nosqlbench.engine.api.activityapi.input.InputDispenser;
import io.nosqlbench.engine.api.activityapi.output.OutputDispenser;
import io.nosqlbench.nb.api.errors.BasicError;

/// Builds {@link StrideMotor} instances for sync actions implementing {@link StrideAction}.
public class StrideMotorDispenser<D> implements MotorDispenser<D> {

    private final Activity activity;
    private final InputDispenser inputDispenser;
    private final ActionDispenser actionDispenser;
    private final OutputDispenser outputDispenser;

    public StrideMotorDispenser(Activity activity,
                                InputDispenser inputDispenser,
                                ActionDispenser actionDispenser,
                                OutputDispenser outputDispenser) {
        this.activity = activity;
        this.inputDispenser = inputDispenser;
        this.actionDispenser = actionDispenser;
        this.outputDispenser = outputDispenser;
    }

    @Override
    public Motor<D> getMotor(io.nosqlbench.nb.api.engine.activityimpl.ActivityDef activityDef, int slotId) {
        var input = inputDispenser.getInput(slotId);
        StrideAction strideAction = (StrideAction) actionDispenser.getAction(slotId);
        var output = outputDispenser != null ? outputDispenser.getOutput(slotId) : null;
        return new StrideMotor<>(activity, slotId, input, strideAction, output);
    }
}
