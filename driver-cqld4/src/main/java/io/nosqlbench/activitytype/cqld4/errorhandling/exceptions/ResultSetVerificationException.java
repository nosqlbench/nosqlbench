package io.nosqlbench.activitytype.cqld4.errorhandling.exceptions;

import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Statement;

public class ResultSetVerificationException extends CQLResultSetException {

    public ResultSetVerificationException(
            long cycle, AsyncResultSet resultSet, Statement<?> statement, Throwable cause) {
        super(cycle, resultSet, statement, cause);
    }

    public ResultSetVerificationException(
            long cycle, AsyncResultSet resultSet, Statement<?> statement, String s) {
        super(cycle, resultSet, statement, s + ", \nquery string:\n" + getQueryString(statement));
    }
}
