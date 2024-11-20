package io.nosqlbench.adapter.prototype;

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


import io.nosqlbench.adapter.prototype.dispensers.ExampleOpDispenserType1;
import io.nosqlbench.adapter.prototype.ops.ExampleOpType1;
import io.nosqlbench.adapter.prototype.ops.ExampleOpTypes;
import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.activityimpl.OpMapper;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.engine.api.templating.TypeAndTarget;
import io.nosqlbench.nb.api.components.core.NBComponent;

import java.util.function.LongFunction;

public class ExampleOpMapper implements OpMapper<ExampleOpType1, ExampleSpace> {

    @Override
    public OpDispenser<ExampleOpType1> apply(
        NBComponent adapterC,
        ParsedOp pop,
        LongFunction<ExampleSpace> spaceInitF
    ) {
        TypeAndTarget<ExampleOpTypes, String> typeAndTarget = pop.getTypeAndTarget(ExampleOpTypes.class, String.class);

        return switch (typeAndTarget.enumId) {
            case type1 -> new ExampleOpDispenserType1(adapterC, pop, spaceInitF);
            case type2 -> new ExampleOpDispenserType1(adapterC, pop, spaceInitF);
        };

    }
}
