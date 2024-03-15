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

package io.nosqlbench.engine.api.activityimpl.varcap;

import io.nosqlbench.adapters.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.CycleOp;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.Op;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.VariableCapture;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.virtdata.core.templates.CapturePoint;

import java.util.List;
import java.util.function.LongFunction;

public class VarCapOpDispenserWrapper extends BaseOpDispenser<Op, Object> {
    private final OpDispenser<? extends Op> realDispenser;
    private final LongFunction<CycleOp<?>> opFunc;
    private final List<CapturePoint> capturePoints;

    public VarCapOpDispenserWrapper(DriverAdapter<Op, Object> adapter, ParsedOp pop, OpDispenser<? extends Op> dispenser) {
        super(adapter, pop);
        this.realDispenser = dispenser;
        this.capturePoints = pop.getCaptures();
        Op exampleOp = realDispenser.apply(0L);


        if (exampleOp instanceof CycleOp<?> cop) {
            opFunc = l -> new VarCapCycleOp((CycleOp<?>)realDispenser.apply(l), capturePoints);
        } else {
            throw new RuntimeException("Invalid type for varcap:" + exampleOp.getClass().getCanonicalName());
        }


    }

    @Override
    public CycleOp<?> apply(long value) {
        return opFunc.apply(value);
    }
}
