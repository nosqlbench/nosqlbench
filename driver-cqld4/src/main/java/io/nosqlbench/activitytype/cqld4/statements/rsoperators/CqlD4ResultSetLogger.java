package io.nosqlbench.activitytype.cqld4.statements.rsoperators;

import com.datastax.oss.driver.api.core.cql.*;
import io.nosqlbench.activitytype.cqld4.api.D4ResultSetCycleOperator;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Logs a trace-level event for the result set, including
 * cycles, rows, fetched row count, and the statement.
 */
public class CqlD4ResultSetLogger implements D4ResultSetCycleOperator {
    private final static Logger logger = LogManager.getLogger(CqlD4ResultSetLogger.class);

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
    public int apply(AsyncResultSet resultSet, Statement statement, long cycle) {
        logger.debug("result-set-logger: "
                + " cycle=" + cycle
                + " remaining=" + resultSet.remaining()
                + " hasmore=" + resultSet.hasMorePages()
                + " statement=" + getQueryString(statement).stripTrailing()
        );
        for (Row row : resultSet.currentPage()) {
            logger.trace(row.toString());
        }
        return 0;
    }
}
