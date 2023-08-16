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

package io.nosqlbench.adapter.jdbc;

import io.nosqlbench.adapter.jdbc.opdispensers.JDBCExecuteOpDispenser;
import io.nosqlbench.adapter.jdbc.opdispensers.JDBCExecuteQueryOpDispenser;
import io.nosqlbench.adapter.jdbc.optypes.JDBCOp;
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

public class JDBCOpMapper implements OpMapper<JDBCOp> {
    private final static Logger logger = LogManager.getLogger(JDBCOpMapper.class);

    private final DriverAdapter adapter;
    private final NBConfiguration cfg;
    private final DriverSpaceCache<? extends JDBCSpace> spaceCache;

    public JDBCOpMapper(DriverAdapter adapter, NBConfiguration cfg, DriverSpaceCache<? extends JDBCSpace> spaceCache) {
        this.adapter = adapter;
        this.cfg = cfg;
        this.spaceCache = spaceCache;
    }

    @Override
    public OpDispenser<? extends JDBCOp> apply(ParsedOp op) {
        LongFunction<String> spaceNameF = op.getAsFunctionOr("space", "default");
        LongFunction<JDBCSpace> spaceFunc = l -> spaceCache.get(spaceNameF.apply(l));

        // Since the only needed thing in the JDBCSpace is the Connection, we can short-circuit
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
            TypeAndTarget<JDBCOpType, String> opType = op.getTypeAndTarget(JDBCOpType.class, String.class, "type", "stmt");

            logger.info(() -> "Using " + opType.enumId + " statement form for '" + op.getName());

            return switch (opType.enumId) {

                // SELECT uses 'executeQuery' and returns a 'ResultSet'
                // https://jdbc.postgresql.org/documentation/query/#example51processing-a-simple-query-in-jdbc
                case query ->
                    new JDBCExecuteQueryOpDispenser(adapter, connectionLongFunc, op, opType.targetFunction);

                // INSERT|UPDATE|DELETE uses 'executeUpdate' and returns an 'int'
                // https://jdbc.postgresql.org/documentation/query/#performing-updates

                // CREATE|DROP TABLE|VIEW uses 'execute' (as opposed to 'executeQuery' which returns a 'ResultSet')
                // https://jdbc.postgresql.org/documentation/query/#example54dropping-a-table-in-jdbc
                case execute, update ->
                    new JDBCExecuteOpDispenser(adapter, connectionLongFunc, op, opType.targetFunction);
            };
        }
    }
}
