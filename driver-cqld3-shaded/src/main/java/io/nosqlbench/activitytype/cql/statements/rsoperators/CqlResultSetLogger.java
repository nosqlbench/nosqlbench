package io.nosqlbench.activitytype.cql.statements.rsoperators;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import com.datastax.driver.core.*;
import io.nosqlbench.activitytype.cql.api.ResultSetCycleOperator;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Logs a trace-level event for the result set, including
 * cycles, rows, fetched row count, and the statement.
 */
public class CqlResultSetLogger implements ResultSetCycleOperator {
    private final static Logger logger = LogManager.getLogger(CqlResultSetLogger.class);

    private static String getQueryString(Statement stmt) {
        if (stmt instanceof PreparedStatement) {
            return "(prepared) " + ((PreparedStatement) stmt).getQueryString();
        } else if (stmt instanceof SimpleStatement) {
            return "(simple) " + ((SimpleStatement) stmt).getQueryString();
        } else if (stmt instanceof BoundStatement) {
            return "(bound) " + ((BoundStatement) stmt).preparedStatement().getQueryString();
        } else {
            return "(unknown) " + stmt.toString();
        }
    }

    @Override
    public int apply(ResultSet resultSet, Statement statement, long cycle) {
        logger.debug("result-set-logger: "
                + " cycle=" + cycle
                + " rows=" + resultSet.getAvailableWithoutFetching()
                + " fetched=" + resultSet.isFullyFetched()
                + " statement=" + getQueryString(statement).stripTrailing()
        );
        for (Row row : resultSet) {
            logger.trace(row.toString());
        }
        return 0;
    }
}
