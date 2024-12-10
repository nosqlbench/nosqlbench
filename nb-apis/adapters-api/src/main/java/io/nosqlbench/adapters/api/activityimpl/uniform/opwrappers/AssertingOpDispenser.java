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
import io.nosqlbench.adapters.api.activityimpl.uniform.Validator;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.CycleOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;

public class AssertingOpDispenser<S extends Space, RESULT> extends BaseOpDispenser<CycleOp<RESULT>, S> {

    private final OpDispenser<CycleOp<RESULT>> realDispenser;
    private final Validator<RESULT> validator;

    public AssertingOpDispenser(
        DriverAdapter<CycleOp<RESULT>, S> adapter,
        ParsedOp pop,
        OpDispenser<CycleOp<RESULT>> realDispenser,
        Validator<RESULT> validator
    ) {
        super(adapter, pop, adapter.getSpaceFunc(pop));
        this.realDispenser = realDispenser;
        this.validator = validator;
        logger.debug("initialized {} for result validation", pop.getName());
    }

    @Override
    public CycleOp<RESULT> getOp(long cycle) {
        CycleOp<RESULT> op = realDispenser.getOp(cycle);
        return new AssertingOp<>(op, validator);
    }
}
