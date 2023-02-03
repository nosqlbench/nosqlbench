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
import io.nosqlbench.engine.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.engine.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.LongFunction;

public class JDBCDDLOpDispenser extends BaseOpDispenser<JDBCOp, JDBCSpace> {
    private static final Logger logger = LogManager.getLogger(JDBCDDLOpDispenser.class);
    private final LongFunction<String> targetFunction;
    private final LongFunction<Connection> connectionLongFunction;
    private final LongFunction<Statement> statementLongFunction;

    public JDBCDDLOpDispenser(DriverAdapter<JDBCOp, JDBCSpace> adapter, LongFunction<Connection> connectionLongFunc, ParsedOp op, LongFunction<String> targetFunction) {
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
        } catch(Exception ex) {
            String err_msg = "Error while attempting to create the jdbc statement from the connection";
            logger.error(err_msg, ex);
            throw new RuntimeException(err_msg, ex);
        }
    }

    @Override
    public JDBCOp apply(long cycle) {
        return new JDBCOp(this.connectionLongFunction.apply(cycle), this.statementLongFunction.apply(cycle), targetFunction.apply(cycle));
    }
}
