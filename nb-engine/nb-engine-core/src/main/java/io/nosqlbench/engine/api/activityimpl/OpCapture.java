package io.nosqlbench.engine.api.activityimpl;

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


import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.activityimpl.uniform.Space;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.CycleOp;
import io.nosqlbench.adapters.api.activityimpl.uniform.opwrappers.CapturingOpDispenser;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.nb.api.errors.OpConfigError;
import io.nosqlbench.virtdata.core.templates.CapturePoints;
import io.nosqlbench.virtdata.core.templates.UniformVariableCapture;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.function.Function;

/// This is a functional wrapper layer which will upgrade a basic [CycleOp] to
/// one that has a specialized variable extractor, given
/// 1. The adapter implements [io.nosqlbench.virtdata.core.templates.UniformVariableCapture]
/// 2. The op template has captures defined. Captures can be defined using the
/// capture points as described in [io.nosqlbench.virtdata.core.templates.CapturePointParser],
/// _or_ they can be directly defined on the op template with the reserved op field `capture`.
public class OpCapture {

    public final static Logger logger = LogManager.getLogger(OpCapture.class);

    public static <OP extends CycleOp<?>, SPACE extends Space> OpDispenser<? extends OP> wrapOptionally(
        DriverAdapter<? extends OP, ? extends SPACE> adapter, OpDispenser<? extends OP> dispenser,
        ParsedOp pop
    ) {

        CapturePoints captures = pop.getCaptures();
        if (captures.isEmpty()) {
            return dispenser;
        }
        OP op = dispenser.getOp(0L);

        if (op instanceof UniformVariableCapture<?> captureF) {
            Function<?, Map<String,?>> function = captureF.initCaptureF(captures);
            return new CapturingOpDispenser(adapter, pop, dispenser, function);
        } else {
            throw new OpConfigError(
                "variable capture configuration failed because op " + op.getClass().getSimpleName() + " in adapter " + adapter + " does not " +
                    "implement " + UniformVariableCapture.class.getSimpleName()
            );
        }
    }
}
