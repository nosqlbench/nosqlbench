package io.nosqlbench.activitytype.cql.errorhandling.exceptions;

import com.datastax.driver.core.ResultSet;

/**
 * This was added to nosqlbench because the error handling logic was
 * starting to look a bit contrived. Because we need to be able
 * to respond to different result outcomes, it
 * is just simpler to have a single type of error-handling logic for all outcomes.
 */
public class ChangeUnappliedCycleException extends CqlGenericCycleException {

    private final ResultSet resultSet;
    private final String queryString;

    public ChangeUnappliedCycleException(long cycle, ResultSet resultSet, String queryString) {
        super(cycle, "Operation was not applied:" + queryString);
        this.resultSet = resultSet;
        this.queryString = queryString;
    }

    public ResultSet getResultSet() {
        return resultSet;
    }
    public String getQueryString() { return queryString; }
}
