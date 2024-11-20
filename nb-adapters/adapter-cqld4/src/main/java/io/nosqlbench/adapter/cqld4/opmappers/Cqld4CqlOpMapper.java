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
import io.nosqlbench.adapter.cqld4.optypes.Cqld4CqlOp;
import io.nosqlbench.adapter.cqld4.optypes.Cqld4CqlSimpleStatement;
import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.engine.api.templating.TypeAndTarget;
import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.api.errors.OpConfigError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.LongFunction;

public class Cqld4CqlOpMapper extends Cqld4CqlBaseOpMapper<Cqld4CqlOp> {

    protected final static Logger logger = LogManager.getLogger(Cqld4CqlOpMapper.class);

    public Cqld4CqlOpMapper(Cqld4DriverAdapter adapter) {
        super(adapter);
    }

    @Override
    public OpDispenser<Cqld4CqlOp> apply(NBComponent adapterC, ParsedOp op, LongFunction<Cqld4Space> spaceInitF) {
        CqlD4OpType opType = CqlD4OpType.prepared;
        TypeAndTarget<CqlD4OpType, String> target = op.getTypeAndTarget(CqlD4OpType.class, String.class, "type", "stmt");
        logger.info(() -> "Using " + target.enumId + " statement form for '" + op.getName() + "'");

        return (OpDispenser<Cqld4CqlOp>) switch (target.enumId) {
            case raw -> {
                CqlD4RawStmtMapper cqlD4RawStmtMapper = new CqlD4RawStmtMapper(adapter, target.targetFunction);
                OpDispenser<Cqld4CqlSimpleStatement> apply = cqlD4RawStmtMapper.apply(adapterC, op, spaceInitF);
                yield apply;
            }
            case simple -> new CqlD4CqlSimpleStmtMapper(adapter, target.targetFunction).apply(adapterC, op, spaceInitF);
            case prepared -> new CqlD4PreparedStmtMapper(adapter, target).apply(adapterC, op, spaceInitF);

            case batch -> new CqlD4BatchStmtMapper(adapter, target).apply(adapterC, op, spaceInitF);
            default ->
                throw new OpConfigError("Unsupported op type for CQL category of statement forms:" + target.enumId);
        };
    }


}
