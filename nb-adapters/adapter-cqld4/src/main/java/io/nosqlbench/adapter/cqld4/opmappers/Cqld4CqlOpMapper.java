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
import io.nosqlbench.adapter.cqld4.Cqld4DriverAdapter;
import io.nosqlbench.adapter.cqld4.Cqld4Space;
import io.nosqlbench.adapter.cqld4.optypes.Cqld4BaseOp;
import io.nosqlbench.adapter.cqld4.optypes.Cqld4CqlOp;
import io.nosqlbench.adapter.cqld4.optypes.Cqld4CqlSimpleStatement;
import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.activityimpl.OpMapper;
import io.nosqlbench.adapters.api.activityimpl.uniform.BaseSpace;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.engine.api.templating.TypeAndTarget;
import io.nosqlbench.nb.api.errors.OpConfigError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.LongFunction;

public class Cqld4CqlOpMapper extends Cqld4BaseOpMapper<Cqld4BaseOp> {

    protected final static Logger logger = LogManager.getLogger(Cqld4CqlOpMapper.class);

    public Cqld4CqlOpMapper(Cqld4DriverAdapter adapter) {
        super(adapter);
    }

    @Override
    public OpDispenser<Cqld4BaseOp> apply(ParsedOp op, LongFunction<Cqld4Space> spaceInitF) {
        CqlD4OpType opType = CqlD4OpType.prepared;
        TypeAndTarget<CqlD4OpType, String> target = op.getTypeAndTarget(CqlD4OpType.class, String.class, "type", "stmt");
        logger.info(() -> "Using " + target.enumId + " statement form for '" + op.getName() + "'");

        return switch (target.enumId) {
            case raw -> {
                CqlD4RawStmtMapper cqlD4RawStmtMapper = new CqlD4RawStmtMapper(adapter, target.targetFunction);
                OpDispenser<Cqld4BaseOp> apply = cqlD4RawStmtMapper.apply(op, spaceFunc);
                yield apply;
            }
            case simple -> new CqlD4CqlSimpleStmtMapper(adapter, target.targetFunction).apply(op, spaceFunc);
            case prepared -> new CqlD4PreparedStmtMapper(adapter, target).apply(op, spaceFunc);

            case batch -> new CqlD4BatchStmtMapper(adapter, target).apply(op, spaceFunc);
            default -> throw new OpConfigError("Unsupported op type for CQL category of statement forms:" + target.enumId);
        };
    }


}
