package io.nosqlbench.adapter.cqld4.opdispensers;

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
import io.nosqlbench.adapter.cqld4.optypes.Cqld4BaseOp;
import io.nosqlbench.adapter.cqld4.optypes.Cqld4CqlOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;

public abstract class Cqld4CqlBaseOpDispenser<T extends Cqld4CqlOp> extends Cqld4BaseOpDispenser<T> {

    public Cqld4CqlBaseOpDispenser(Cqld4DriverAdapter adapter, ParsedOp op) {
        super(adapter, op);
    }

    @Override
    public abstract T getOp(long cycle);
}
