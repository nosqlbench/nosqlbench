package io.nosqlbench.adapter.prototype.dispensers;

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


import io.nosqlbench.adapter.prototype.ExampleSpace;
import io.nosqlbench.adapter.prototype.ops.ExampleOpType1;
import io.nosqlbench.adapters.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.nb.api.components.core.NBComponent;

import java.util.function.LongFunction;

public class ExampleOpDispenserType1 extends BaseOpDispenser<ExampleOpType1, ExampleSpace> {

    public ExampleOpDispenserType1(
        NBComponent adapter,
        ParsedOp pop,
        LongFunction<? extends ExampleSpace> spaceInitF
    ) {
        super(adapter, pop, spaceInitF);
    }

    @Override
    public ExampleOpType1 getOp(long cycle) {
        return new ExampleOpType1(cycle);
    }
}
