package io.nosqlbench.activitytype.cql.ebdrivers.cql.statements.rsoperators;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Statement;
import io.nosqlbench.activitytype.cql.ebdrivers.cql.api.ResultSetCycleOperator;
import io.nosqlbench.activitytype.cql.ebdrivers.cql.errorhandling.exceptions.ResultSetVerificationException;

public class AssertSingleRowResultSet implements ResultSetCycleOperator {

    @Override
    public int apply(ResultSet resultSet, Statement statement, long cycle) {
        int rowsIncoming = resultSet.getAvailableWithoutFetching();
        if (rowsIncoming<1) {
            throw new ResultSetVerificationException(cycle, resultSet, statement, "no row in result set, expected exactly 1");
        }
        if (rowsIncoming>1) {
            throw new ResultSetVerificationException(cycle, resultSet, statement, "more than one row in result set, expected exactly 1");
        }
        return rowsIncoming;
    }

}
