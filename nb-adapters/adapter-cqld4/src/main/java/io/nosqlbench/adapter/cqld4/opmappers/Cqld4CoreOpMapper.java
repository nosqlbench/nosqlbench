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
import io.nosqlbench.adapter.cqld4.optypes.Cqld4BaseOp;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.CycleOp;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;
import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.engine.api.templating.TypeAndTarget;
import io.nosqlbench.nb.api.errors.OpConfigError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.LongFunction;

public class Cqld4CoreOpMapper extends Cqld4BaseOpMapper<Cqld4BaseOp<?>> {

    private final static Logger logger = LogManager.getLogger(Cqld4CoreOpMapper.class);

    public Cqld4CoreOpMapper(Cqld4DriverAdapter adapter, NBConfiguration config) {
        super(adapter);
    }

    /**
     * Determine what type of op dispenser to use for a given parsed op template, and return a new instance
     * for it. Since the operations under the CQL driver 4.* do not follow a common type structure, we use the
     * base types in the NoSQLBench APIs and treat them somewhat more generically than with other drivers.
     *
     * @param op
     *     The {@link ParsedOp} which is the parsed version of the user-provided op template.
     *     This contains all the fields provided by the user, as well as explicit knowledge of
     *     which ones are static and dynamic.
     * @return An op dispenser for each provided op command
     */

    @Override
    public OpDispenser<Cqld4BaseOp<?>> apply(ParsedOp op, LongFunction<Cqld4Space> cqld4SpaceLongFunction) {
        CqlD4OpType opType = CqlD4OpType.prepared;
        TypeAndTarget<CqlD4OpType, String> target = op.getTypeAndTarget(CqlD4OpType.class, String.class, "type", "stmt");
        logger.info(() -> "Using " + target.enumId + " statement form for '" + op.getName() + "'");

        return (OpDispenser<Cqld4BaseOp<?>>) switch (target.enumId) {
            case raw, simple, prepared, batch -> new Cqld4CqlOpMapper(adapter).apply(op, spaceFunc);
            case gremlin -> new Cqld4GremlinOpMapper(adapter, target.targetFunction).apply(op, spaceFunc);
            case fluent -> new Cqld4FluentGraphOpMapper(adapter, target).apply(op, spaceFunc);
            case rainbow ->
                new CqlD4RainbowTableMapper(adapter, sessionFunc, target.targetFunction).apply(op, spaceFunc);
            default -> throw new OpConfigError("Unsupported op type " + opType);
//            case sst -> new Cqld4SsTableMapper(adapter, sessionFunc, target.targetFunction).apply(op);
        };
    }

}
