package io.nosqlbench.activitytype.cql.statements.rsoperators;

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
