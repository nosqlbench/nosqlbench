/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.adapter.cqld4.opmappers;

import io.nosqlbench.adapter.cqld4.Cqld4DriverAdapter;
import io.nosqlbench.adapter.cqld4.Cqld4Space;
import io.nosqlbench.adapter.cqld4.opdispensers.Cqld4RawStmtDispenser;
import io.nosqlbench.adapter.cqld4.optypes.Cqld4CqlSimpleStatement;
import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.nb.api.components.core.NBComponent;

import java.util.function.LongFunction;

public class CqlD4RawStmtMapper extends Cqld4CqlBaseOpMapper<Cqld4CqlSimpleStatement> {

    private final LongFunction<String> targetFunction;
    public CqlD4RawStmtMapper(Cqld4DriverAdapter adapter,LongFunction<String> targetFunction) {
        super(adapter);
        this.targetFunction = targetFunction;
    }

    @Override
    public OpDispenser<Cqld4CqlSimpleStatement> apply(NBComponent adapterC, ParsedOp op, LongFunction<Cqld4Space> spaceInitF) {
        return new Cqld4RawStmtDispenser(adapter, targetFunction,op);
    }

    //    @Override
//    public OpDispenser<Cqld4CqlOp> apply(ParsedOp op, LongFunction<Cqld4Space> spaceInitF) {
//        return new Cqld4RawStmtDispenser(adapter, sessionFunc, targetFunction, op);
//    }

//    @Override
//    public OpDispenser<Cqld4CqlOp> apply(ParsedOp parsedOp, LongFunction<Cqld4Space> longFunction) {
//        return new Cqld4RawStmtDispenser(adapter, sessionFunc, targetFunction, parsedOp);
//    }
}
