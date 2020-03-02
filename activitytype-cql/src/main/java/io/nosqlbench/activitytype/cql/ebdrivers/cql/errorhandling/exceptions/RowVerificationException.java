package io.nosqlbench.activitytype.cql.ebdrivers.cql.errorhandling.exceptions;

import com.datastax.driver.core.Row;

import java.util.Map;

/**
 * This exception is thrown when read verification fails.
 */
public class RowVerificationException extends CqlCycleException {

    private Map<String, Object> expected;
    private Row row;

    public RowVerificationException(long cycle, Row row, Map<String, Object> expected, String detail) {
        super(cycle, detail);
        this.expected = expected;
        this.row = row;
    }

    @Override
    public String getMessage() {
        return "cycle:" + getCycle() + ": " + super.getMessage();
    }

    public Map<String,Object> getExpectedValues() {
        return expected;
    }

    public Row getRow() {
        return row;
    }
}
