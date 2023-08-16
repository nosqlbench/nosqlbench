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
import io.nosqlbench.adapter.cqld4.Cqld4Space;
import io.nosqlbench.api.config.standard.NBConfiguration;
import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.activityimpl.OpMapper;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverSpaceCache;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.Op;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.engine.api.templating.TypeAndTarget;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.LongFunction;

public class Cqld4CoreOpMapper implements OpMapper<Op> {

    private final static Logger logger = LogManager.getLogger(Cqld4CoreOpMapper.class);

    private final DriverSpaceCache<? extends Cqld4Space> cache;
    private final NBConfiguration cfg;
    private final DriverAdapter adapter;

    public Cqld4CoreOpMapper(DriverAdapter adapter, NBConfiguration config, DriverSpaceCache<? extends Cqld4Space> cache) {
        this.cfg = config;
        this.cache = cache;
        this.adapter = adapter;
    }

    /**
     * Determine what type of op dispenser to use for a given parsed op template, and return a new instance
     * for it. Since the operations under the CQL driver 4.* do not follow a common type structure, we use the
     * base types in the NoSQLBench APIs and treat them somewhat more generically than with other drivers.
     *
     * @param op The {@link ParsedOp} which is the parsed version of the user-provided op template.
     *            This contains all the fields provided by the user, as well as explicit knowledge of
     *            which ones are static and dynamic.
     * @return An op dispenser for each provided op command
     */
    public OpDispenser<? extends Op> apply(ParsedOp op) {

        LongFunction<String> spaceName = op.getAsFunctionOr("space", "default");
        // Since the only needed thing in the Cqld4Space is the session, we can short-circuit
        // to it here instead of stepping down from the cycle to the space to the session
        LongFunction<CqlSession> sessionFunc = l -> cache.get(spaceName.apply(l)).getSession();

        CqlD4OpType opType = CqlD4OpType.prepared;

        TypeAndTarget<CqlD4OpType, String> target = op.getTypeAndTarget(CqlD4OpType.class, String.class, "type", "stmt");

        logger.info(() -> "Using " + target.enumId + " statement form for '" + op.getName());

        return switch (target.enumId) {
            case raw -> new CqlD4RawStmtMapper(adapter, sessionFunc, target.targetFunction).apply(op);
            case simple -> new CqlD4CqlSimpleStmtMapper(adapter, sessionFunc, target.targetFunction).apply(op);
            case prepared -> new CqlD4PreparedStmtMapper(adapter, sessionFunc, target).apply(op);
            case gremlin -> new Cqld4GremlinOpMapper(adapter, sessionFunc, target.targetFunction).apply(op);
            case fluent -> new Cqld4FluentGraphOpMapper(adapter, sessionFunc, target).apply(op);
            case rainbow -> new CqlD4RainbowTableMapper(adapter, sessionFunc, target.targetFunction).apply(op);
//            case sst -> new Cqld4SsTableMapper(adapter, sessionFunc, target.targetFunction).apply(op);
        };
    }


}
