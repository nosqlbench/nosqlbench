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
import io.nosqlbench.adapter.jdbc.optypes.JDBCDMLOp;
import io.nosqlbench.adapter.jdbc.optypes.JDBCOp;
import io.nosqlbench.adapters.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class JDBCBaseOpDispenser extends BaseOpDispenser<JDBCOp, JDBCSpace> {
    protected static final String ERROR_STATEMENT_CREATION =
        "Error while attempting to create the jdbc statement from the connection";

    protected final JDBCSpace jdbcSpace;
    protected  boolean isDdlStatement;
    protected final boolean isPreparedStatement;

    public JDBCBaseOpDispenser(DriverAdapter<JDBCOp, JDBCSpace> adapter,
                               JDBCSpace jdbcSpace,
                               ParsedOp op) {
        super(adapter, op);
        this.jdbcSpace = jdbcSpace;
        this.isPreparedStatement = op.getStaticConfigOr("prepared", false);
    }

    public void checkShutdownEntry(long cycle) {
        if (cycle == (jdbcSpace.getTotalCycleNum()-1)) {
            jdbcSpace.enterShutdownStage();
        }
    }
}
