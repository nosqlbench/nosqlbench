package io.nosqlbench.activitytype.cqld4.errorhandling.exceptions;

import com.datastax.oss.driver.api.core.cql.*;

public abstract class CQLResultSetException extends CqlGenericCycleException {

    private final Statement<?> statement;
    private final AsyncResultSet resultSet;

    public CQLResultSetException(long cycle, AsyncResultSet resultSet, Statement<?> statement, String message,
                                 Throwable cause) {
        super(cycle,message,cause);
        this.resultSet = resultSet;
        this.statement = statement;
    }

    public CQLResultSetException(long cycle, AsyncResultSet resultSet, Statement<?> statement) {
        super(cycle);
        this.resultSet = resultSet;
        this.statement = statement;
    }

    public CQLResultSetException(long cycle, AsyncResultSet resultSet, Statement<?> statement, String message) {
        super(cycle,message);
        this.resultSet = resultSet;
        this.statement=statement;
    }

    public CQLResultSetException(long cycle, AsyncResultSet resultSet, Statement<?> statement, Throwable cause) {
        super(cycle,cause);
        this.resultSet = resultSet;
        this.statement = statement;
    }

    public Statement<?> getStatement() {
        return statement;
    }

    public AsyncResultSet getResultSet() {
        return resultSet;
    }

    protected static String getQueryString(Statement<?> stmt) {
        if (stmt instanceof BoundStatement) {

            return ((BoundStatement)stmt).getPreparedStatement().getQuery();
        } else if (stmt instanceof SimpleStatement) {
            return ((SimpleStatement) stmt).getQuery();
        } else {
            return "UNKNOWN Statement type:" + stmt.getClass().getSimpleName();
        }
    }


}
