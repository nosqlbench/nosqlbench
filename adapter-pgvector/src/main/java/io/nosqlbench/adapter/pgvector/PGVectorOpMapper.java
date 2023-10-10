/*
 * Copyright (c) 2023 nosqlbench
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

package io.nosqlbench.adapter.pgvector;

import io.nosqlbench.adapter.pgvector.opdispensers.PGVectorExecuteOpDispenser;
import io.nosqlbench.adapter.pgvector.opdispensers.PGVectorExecuteQueryOpDispenser;
import io.nosqlbench.adapter.pgvector.opdispensers.PGVectorExecuteUpdateOpDispenser;
import io.nosqlbench.adapter.pgvector.optypes.PGVectorOp;
import io.nosqlbench.api.config.standard.NBConfiguration;
import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.activityimpl.OpMapper;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverSpaceCache;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.engine.api.templating.TypeAndTarget;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.util.function.LongFunction;

public class PGVectorOpMapper implements OpMapper<PGVectorOp> {
    private final static Logger logger = LogManager.getLogger(PGVectorOpMapper.class);

    private final DriverAdapter adapter;
    private final NBConfiguration cfg;
    private final DriverSpaceCache<? extends PGVectorSpace> spaceCache;

    public PGVectorOpMapper(DriverAdapter adapter, NBConfiguration cfg, DriverSpaceCache<? extends PGVectorSpace> spaceCache) {
        this.adapter = adapter;
        this.cfg = cfg;
        this.spaceCache = spaceCache;
    }

    @Override
    public OpDispenser<? extends PGVectorOp> apply(ParsedOp op) {
        LongFunction<String> spaceNameF = op.getAsFunctionOr("space", "default");
        LongFunction<PGVectorSpace> spaceFunc = l -> spaceCache.get(spaceNameF.apply(l));

        // Since the only needed thing in the PGVectorSpace is the Connection, we can short-circuit
        // to it here instead of stepping down from the cycle to the space to the connection.
        LongFunction<Connection> connectionLongFunc = l -> spaceCache.get(spaceNameF.apply(l)).getConnection();

        /*
         * If the user provides a body element, then they want to provide the JSON or
         * a data structure that can be converted into JSON, bypassing any further
         * specialized type-checking or op-type specific features
         */
        if (op.isDefined("body")) {
            throw new RuntimeException("This mode is reserved for later. Do not use the 'body' op field.");
        } else {
            TypeAndTarget<PGVectorOpType, String> opType = op.getTypeAndTarget(PGVectorOpType.class, String.class, "type", "stmt");

            logger.info(() -> "Using " + opType.enumId + " statement form for '" + op.getName());

            return switch (opType.enumId) {

                // SELECT uses 'executeQuery' and returns a 'ResultSet'
                // https://jdbc.postgresql.org/documentation/query/#example51processing-a-simple-query-in-jdbc
                case query ->
                    new PGVectorExecuteQueryOpDispenser(adapter, connectionLongFunc, op, opType.targetFunction);

                // INSERT|UPDATE|DELETE uses 'executeUpdate' and returns an 'int'
                // https://jdbc.postgresql.org/documentation/query/#performing-updates
                case update ->
                    new PGVectorExecuteUpdateOpDispenser(adapter, connectionLongFunc, op, opType.targetFunction);

                // CREATE|DROP TABLE|VIEW uses 'execute' (as opposed to 'executeQuery' which returns a 'ResultSet')
                // https://jdbc.postgresql.org/documentation/query/#example54dropping-a-table-in-jdbc
                case execute ->
                    new PGVectorExecuteOpDispenser(adapter, connectionLongFunc, op, opType.targetFunction);
            };
        }
    }
}
