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
import io.nosqlbench.adapter.jdbc.exceptions.JDBCAdapterInvalidParamException;
import io.nosqlbench.adapter.jdbc.optypes.JDBCDDLOp;
import io.nosqlbench.adapter.jdbc.optypes.JDBCOp;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.LongFunction;

public class JDBCDDLOpDispenser extends JDBCBaseOpDispenser {

    private static final Logger logger = LogManager.getLogger(JDBCDDLOpDispenser.class);

    private final LongFunction<String> ddlSqlStrFunc;

    public JDBCDDLOpDispenser(DriverAdapter<JDBCOp, JDBCSpace> adapter,
                              JDBCSpace jdbcSpace,
                              ParsedOp op,
                              LongFunction<String> sqlStrFunc) {
        super(adapter, jdbcSpace, op);
        this.isDdlStatement = true;
        this.ddlSqlStrFunc = sqlStrFunc;

        // For DDL statements, must use autoCommit
        assert(jdbcSpace.isAutoCommit());
        if (isPreparedStatement) {
            throw new JDBCAdapterInvalidParamException("DDL statements can NOT be prepared!");
        }
    }
    @Override
    public JDBCDDLOp getOp(long cycle) {
        String ddlSqlStr = ddlSqlStrFunc.apply(cycle);
        return new JDBCDDLOp(jdbcSpace, ddlSqlStr);
    }
}
