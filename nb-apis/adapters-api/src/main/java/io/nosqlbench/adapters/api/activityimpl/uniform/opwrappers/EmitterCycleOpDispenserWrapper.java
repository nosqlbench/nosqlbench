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
import io.nosqlbench.adapters.api.activityimpl.uniform.Space;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.CycleOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;

public class EmitterCycleOpDispenserWrapper<O,S extends Space,R> extends BaseOpDispenser<CycleOp<R>, S> {

    private final OpDispenser<CycleOp<R>> realDispenser;

    public EmitterCycleOpDispenserWrapper(
        DriverAdapter<? extends CycleOp<R>, S> adapter,
        ParsedOp pop,
        OpDispenser<CycleOp<R>> realDispenser
    ) {
        super(adapter, pop);
        this.realDispenser = realDispenser;
        logger.warn(
            "initialized {} for to emit the result type to stdout. ",
            pop.getName()
        );

    }

    @Override
    public EmitterCycleOp<R> getOp(long cycle) {
        CycleOp<R> cycleOp = realDispenser.getOp(cycle);
        return new EmitterCycleOp(cycleOp);
    }
}
