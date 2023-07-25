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

package io.nosqlbench.adapter.jdbc.opdispensers;

import io.nosqlbench.adapter.jdbc.JDBCSpace;
import io.nosqlbench.adapter.jdbc.optypes.JDBCOp;
import io.nosqlbench.adapters.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.LongFunction;

public abstract class JDBCBaseOpDispenser extends BaseOpDispenser<JDBCOp, JDBCSpace> {
    private static final Logger logger = LogManager.getLogger(JDBCBaseOpDispenser.class);

    protected static final String ERROR_STATEMENT_CREATION = "Error while attempting to create the jdbc statement from the connection";

    protected final LongFunction<String> targetFunction;
    protected final LongFunction<Connection> connectionLongFunction;
    protected final LongFunction<Statement> statementLongFunction;

    public JDBCBaseOpDispenser(DriverAdapter<JDBCOp, JDBCSpace> adapter, LongFunction<Connection> connectionLongFunc, ParsedOp op, LongFunction<String> targetFunction) {
        super(adapter, op);

        this.connectionLongFunction = connectionLongFunc;
        this.targetFunction = targetFunction;
        this.statementLongFunction = createStmtFunc(op);
    }

    protected LongFunction<Statement> createStmtFunc(ParsedOp cmd) {
        try {
            LongFunction<Statement> basefunc = l -> {
                try {
                    return this.connectionLongFunction.apply(l).createStatement();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            };
            return basefunc;
        } catch (Exception ex) {
            logger.error(ERROR_STATEMENT_CREATION, ex);
            throw new RuntimeException(ERROR_STATEMENT_CREATION, ex);
        }
    }
}
