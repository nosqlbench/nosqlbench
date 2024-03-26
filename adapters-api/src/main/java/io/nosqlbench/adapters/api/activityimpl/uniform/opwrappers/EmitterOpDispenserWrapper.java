/*
 * Copyright (c) 2022-2024 nosqlbench
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

package io.nosqlbench.adapters.api.activityimpl.uniform.opwrappers;

import io.nosqlbench.adapters.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.CycleOp;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.Op;
import io.nosqlbench.adapters.api.templating.ParsedOp;

public class EmitterOpDispenserWrapper extends BaseOpDispenser<Op, Object> {

    private final OpDispenser<? extends CycleOp<?>> realDispenser;

    public EmitterOpDispenserWrapper(DriverAdapter<Op,Object> adapter, ParsedOp pop, OpDispenser<? extends CycleOp<?>> realDispenser) {
        super(adapter, pop);
        this.realDispenser = realDispenser;
    }
    @Override
    public EmitterOp getOp(long cycle) {
        CycleOp<?> cycleOp = realDispenser.getOp(cycle);
        return new EmitterOp(cycleOp);
    }
}
