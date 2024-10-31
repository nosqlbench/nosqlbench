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
import io.nosqlbench.adapter.cqld4.opdispensers.CqlD4BatchStmtDispenser;
import io.nosqlbench.adapter.cqld4.optypes.Cqld4CqlBatchStatement;
import io.nosqlbench.adapter.cqld4.optypes.Cqld4CqlOp;
import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.engine.api.templating.TypeAndTarget;

import java.util.function.LongFunction;

public class CqlD4BatchStmtMapper extends Cqld4CqlBaseOpMapper<Cqld4CqlBatchStatement> {

    private final TypeAndTarget<CqlD4OpType, String> target;

    public CqlD4BatchStmtMapper(Cqld4DriverAdapter adapter,
                                TypeAndTarget<CqlD4OpType, String> target) {
        super(adapter);
        this.target = target;
    }

    @Override
    public OpDispenser<Cqld4CqlBatchStatement> apply(ParsedOp op, LongFunction<Cqld4Space> spaceInitF) {
        ParsedOp subop = op.getAsSubOp("op_template", ParsedOp.SubOpNaming.ParentAndSubKey);
        int repeat = op.getStaticValue("repeat");
        OpDispenser<Cqld4CqlOp> od = new Cqld4CqlOpMapper(adapter).apply(op, spaceInitF);
        return new CqlD4BatchStmtDispenser(adapter, op, repeat, subop, od);
    }

}
