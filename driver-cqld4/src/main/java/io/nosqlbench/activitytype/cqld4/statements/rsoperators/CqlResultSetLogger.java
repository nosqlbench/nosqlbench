package io.nosqlbench.activitytype.cqld4.statements.rsoperators;

import com.datastax.oss.driver.api.core.cql.*;
import io.nosqlbench.activitytype.cqld4.api.ResultSetCycleOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logs a trace-level event for the result set, including
 * cycles, rows, fetched row count, and the statement.
 */
public class CqlResultSetLogger implements ResultSetCycleOperator {
    private final static Logger logger = LoggerFactory.getLogger(CqlResultSetLogger.class);

    private static String getQueryString(Statement stmt) {
        if (stmt instanceof PreparedStatement) {
            return "(prepared) " + ((PreparedStatement) stmt).getQuery();
        } else if (stmt instanceof SimpleStatement) {
            return "(simple) " + ((SimpleStatement) stmt).getQuery();
        } else if (stmt instanceof BoundStatement) {
            return "(bound) " + ((BoundStatement) stmt).getPreparedStatement().getQuery();
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
