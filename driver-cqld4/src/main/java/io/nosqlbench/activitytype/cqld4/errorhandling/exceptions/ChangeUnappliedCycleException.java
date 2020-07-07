package io.nosqlbench.activitytype.cqld4.errorhandling.exceptions;

import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.ResultSet;

/**
 * This was added to nosqlbench because the error handling logic was
 * starting to look a bit contrived. Because we need to be able
 * to respond to different result outcomes, it
 * is just simpler to have a single type of error-handling logic for all outcomes.
 */
public class ChangeUnappliedCycleException extends CqlGenericCycleException {

    private final ResultSet resultSet;
    private final String queryString;
    private final AsyncResultSet asyncResultSet;

    public ChangeUnappliedCycleException(long cycle, AsyncResultSet asyncResultSet, String queryString) {
        super(cycle, "Operation was not applied:" + queryString);
        this.asyncResultSet = asyncResultSet;
        this.queryString = queryString;
        this.resultSet=null;
    }
    public ChangeUnappliedCycleException(long cycle, ResultSet resultSet, String queryString) {
        super(cycle, "Operation was not applied:" + queryString);
        this.resultSet = resultSet;
        this.queryString = queryString;
        this.asyncResultSet=null;
    }

    public ResultSet getResultSet() {
        return resultSet;
    }
    public String getQueryString() { return queryString; }
}
