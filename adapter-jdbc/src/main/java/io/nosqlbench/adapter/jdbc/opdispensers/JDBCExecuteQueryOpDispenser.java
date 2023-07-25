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
import io.nosqlbench.adapter.jdbc.optypes.JDBCExecuteQueryOp;
import io.nosqlbench.adapter.jdbc.optypes.JDBCOp;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.util.function.LongFunction;

public class JDBCExecuteQueryOpDispenser extends JDBCBaseOpDispenser {
    private static final Logger logger = LogManager.getLogger(JDBCExecuteQueryOpDispenser.class);

    public JDBCExecuteQueryOpDispenser(DriverAdapter<JDBCOp, JDBCSpace> adapter, LongFunction<Connection> connectionLongFunc, ParsedOp op, LongFunction<String> targetFunction) {
        super(adapter, connectionLongFunc, op, targetFunction);
    }

    @Override
    public JDBCExecuteQueryOp apply(long cycle) {
        return new JDBCExecuteQueryOp(this.connectionLongFunction.apply(cycle), this.statementLongFunction.apply(cycle), targetFunction.apply(cycle));
    }
}
