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

import javax.sql.DataSource;
import java.sql.Statement;
import java.util.function.LongFunction;

public class JDBCQueryOpDispenser extends BaseOpDispenser<JDBCOp, JDBCSpace> {
    private final static Logger logger = LogManager.getLogger(JDBCQueryOpDispenser.class);
    private final DataSource dataSource;
    private final LongFunction<JDBCOp> jdbcOpLongFunction;
//    private final LongFunction<String> tableNameFunc;
    //private final LongFunction<String> targetFunction;

    public JDBCQueryOpDispenser(DriverAdapter adapter, LongFunction<JDBCSpace> jdbcSpaceLongFunction, ParsedOp op/*, LongFunction<String> targetFunction*/) {
        super(adapter, op);
        this.jdbcOpLongFunction = getOpFunc(jdbcSpaceLongFunction, op);
        //this.targetFunction = targetFunction;
        //TODO -- implement this
        dataSource = null;
    }

    public JDBCQueryOpDispenser(DriverAdapter<JDBCOp, JDBCSpace> adapter, ParsedOp op) {
        super(adapter, op);
        //TODO -- implement this
        this.jdbcOpLongFunction = null;
        this.dataSource = null;
        //this.targetFunction = null;
    }

    protected LongFunction<Statement> createStmtFunc(ParsedOp cmd) {
        LongFunction<Statement> basefunc = l -> null;//targetFunction.apply(l));
        return null;
    }

    private LongFunction<JDBCOp> getOpFunc(LongFunction<JDBCSpace> jdbcSpaceLongFunction, ParsedOp op) {
        //return null;
        LongFunction<JDBCOp> jdbcOpLongFunction = cycle -> new JDBCOp(jdbcSpaceLongFunction.apply(cycle), "DUMMY_STRINGcycle");
        return jdbcOpLongFunction;
    }

    @Override
    public JDBCOp apply(long value) {
        JDBCOp op = this.jdbcOpLongFunction.apply(value);
        return op;
    }
}
