package io.nosqlbench.activitytype.cqld4.errorhandling.exceptions;

import com.datastax.driver.core.BoundStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.oss.driver.api.core.cql.Statement;

public abstract class CQLResultSetException extends CqlGenericCycleException {

    private final Statement statement;
    private final ResultSet resultSet;

    public CQLResultSetException(long cycle, ResultSet resultSet, Statement statement, String message, Throwable cause) {
        super(cycle,message,cause);
        this.resultSet = resultSet;
        this.statement = statement;
    }

    public CQLResultSetException(long cycle, ResultSet resultSet, Statement statement) {
        super(cycle);
        this.resultSet = resultSet;
        this.statement = statement;
    }

    public CQLResultSetException(long cycle, ResultSet resultSet, Statement statement, String message) {
        super(cycle,message);
        this.resultSet = resultSet;
        this.statement=statement;
    }

    public CQLResultSetException(long cycle, ResultSet resultSet, Statement statement, Throwable cause) {
        super(cycle,cause);
        this.resultSet = resultSet;
        this.statement = statement;
    }

    public Statement getStatement() {
        return statement;
    }

    public ResultSet getResultSet() {
        return resultSet;
    }

    protected static String getQueryString(Statement stmt) {
        if (stmt instanceof BoundStatement) {
            return ((BoundStatement)stmt).preparedStatement().getQueryString();
        } else if (stmt instanceof SimpleStatement) {
            return ((SimpleStatement) stmt).getQueryString();
        } else {
            return "UNKNOWN Statement type:" + stmt.getClass().getSimpleName();
        }
    }


}
