package io.nosqlbench.activitytype.cqld4.errorhandling.exceptions;

import com.datastax.oss.driver.api.core.cql.Row;

import java.util.Map;

/**
 * This exception is thrown when read verification fails.
 */
public class RowVerificationException extends CqlGenericCycleException {

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
