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

import io.nosqlbench.adapter.jdbc.opdispensers.JDBCDMLOpDispenser;
import io.nosqlbench.adapter.jdbc.opdispensers.JDBCDDLOpDispenser;
import io.nosqlbench.adapter.jdbc.optypes.JDBCOp;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;
import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.activityimpl.OpMapper;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverSpaceCache;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.engine.api.templating.TypeAndTarget;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
        String spaceName = op.getStaticConfigOr("space", "default");
        JDBCSpace jdbcSpace = spaceCache.get(spaceName);

        /*
         * If the user provides a body element, then they want to provide the JSON or
         * a data structure that can be converted into JSON, bypassing any further
         * specialized type-checking or op-type specific features
         */
        if (op.isDefined("body")) {
            throw new RuntimeException("This mode is reserved for later. Do not use the 'body' op field.");
        } else {
            TypeAndTarget<JDBCOpType, String> opType = op.getTypeAndTarget(JDBCOpType.class, String.class);

            logger.info(() -> "Using " + opType.enumId + " statement form for '" + op.getName());

            return switch (opType.enumId) {
                // https://jdbc.postgresql.org/documentation/query/#example54dropping-a-table-in-jdbc
                case ddl->
                    new JDBCDDLOpDispenser(adapter, jdbcSpace, op, opType.targetFunction);

                // https://jdbc.postgresql.org/documentation/query/#performing-updates
                case dmlwrite ->
                    new JDBCDMLOpDispenser(adapter, jdbcSpace, op, false, opType.targetFunction);

                // https://jdbc.postgresql.org/documentation/query/#example51processing-a-simple-query-in-jdbc
                case dmlread ->
                    new JDBCDMLOpDispenser(adapter, jdbcSpace, op, true, opType.targetFunction);
            };
        }
    }
}
