package io.nosqlbench.adapter.cqld4.opmappers;

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


import io.nosqlbench.adapter.cqld4.Cqld4DriverAdapter;
import io.nosqlbench.adapter.cqld4.Cqld4Space;
import io.nosqlbench.adapter.cqld4.optypes.Cqld4CqlOp;
import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.CycleOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;

import java.util.function.LongFunction;

public abstract class Cqld4CqlBaseOpMapper<T extends Cqld4CqlOp> extends Cqld4BaseOpMapper<T> {

    public Cqld4CqlBaseOpMapper(Cqld4DriverAdapter adapter) {
        super(adapter);
    }

    @Override
    public OpDispenser<T> apply(ParsedOp op, LongFunction<Cqld4Space> spaceInitF) {
        return null;
    }

    //    @Override
//    public abstract OpDispenser<T> apply(ParsedOp op, LongFunction<Cqld4Space> spaceInitF);
}
