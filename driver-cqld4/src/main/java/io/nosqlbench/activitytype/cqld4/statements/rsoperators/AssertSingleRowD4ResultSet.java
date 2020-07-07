package io.nosqlbench.activitytype.cqld4.statements.rsoperators;

import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Statement;
import io.nosqlbench.activitytype.cqld4.api.D4ResultSetCycleOperator;
import io.nosqlbench.activitytype.cqld4.errorhandling.exceptions.ResultSetVerificationException;

/**
 * Throws a {@link ResultSetVerificationException} unless there is exactly one row in the result set.
 */
public class AssertSingleRowD4ResultSet implements D4ResultSetCycleOperator {

        @Override
        public int apply(AsyncResultSet resultSet, Statement<?> statement, long cycle) {
            int rowsIncoming = resultSet.remaining();
        if (rowsIncoming<1) {
            throw new ResultSetVerificationException(cycle, resultSet, statement, "no row in result set, expected exactly 1");
        }
        if (rowsIncoming>1) {
            throw new ResultSetVerificationException(cycle, resultSet, statement, "more than one row in result set, expected exactly 1");
        }
        return rowsIncoming;
    }

}
