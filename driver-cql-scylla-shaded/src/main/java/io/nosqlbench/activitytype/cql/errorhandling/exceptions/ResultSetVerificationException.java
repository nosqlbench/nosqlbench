package io.nosqlbench.activitytype.cql.errorhandling.exceptions;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Statement;

public class ResultSetVerificationException extends CQLResultSetException {

    public ResultSetVerificationException(
            long cycle, ResultSet resultSet, Statement statement, Throwable cause) {
        super(cycle, resultSet, statement, cause);
    }

    public ResultSetVerificationException(
            long cycle, ResultSet resultSet, Statement statement, String s) {
        super(cycle, resultSet, statement, s + ", \nquery string:\n" + getQueryString(statement));
    }
}
