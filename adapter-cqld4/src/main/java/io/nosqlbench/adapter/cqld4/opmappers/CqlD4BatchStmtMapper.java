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

import com.datastax.oss.driver.api.core.CqlSession;
import io.nosqlbench.adapter.cqld4.opdispensers.CqlD4BatchStmtDispenser;
import io.nosqlbench.adapter.cqld4.optypes.Cqld4CqlBatchStatement;
import io.nosqlbench.adapter.cqld4.optypes.Cqld4CqlOp;
import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.activityimpl.OpMapper;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.engine.api.templating.TypeAndTarget;

import java.util.function.LongFunction;

public class CqlD4BatchStmtMapper implements OpMapper<Cqld4CqlOp> {

    private final LongFunction<CqlSession> sessionFunc;
    private final TypeAndTarget<CqlD4OpType, String> target;
    private final DriverAdapter adapter;


    public CqlD4BatchStmtMapper(DriverAdapter adapter, LongFunction<CqlSession> sessionFunc, TypeAndTarget<CqlD4OpType,String> target) {
        this.sessionFunc=sessionFunc;
        this.target = target;
        this.adapter = adapter;
    }

    /**
     * TODO: Make this not require a sub-op element for "uniform batches",
     * but allow a sub-op sequence for custom batches.
     * @param op the function argument
     * @return
     */
    public OpDispenser<Cqld4CqlOp> apply(ParsedOp op) {

        ParsedOp subop = op.getAsSubOp("op_template", ParsedOp.SubOpNaming.ParentAndSubKey);
        int repeat = op.getStaticValue("repeat");
        OpMapper<Cqld4CqlOp> subopMapper = adapter.getOpMapper();
        OpDispenser<? extends Cqld4CqlOp> subopDispenser = subopMapper.apply(subop);
        return new CqlD4BatchStmtDispenser(adapter, sessionFunc, op,repeat, subop, subopDispenser);

    }
}
