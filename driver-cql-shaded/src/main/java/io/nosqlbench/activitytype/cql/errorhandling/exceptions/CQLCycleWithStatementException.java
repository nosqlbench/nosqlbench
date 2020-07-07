package io.nosqlbench.activitytype.cql.errorhandling.exceptions;

import io.nosqlbench.activitytype.cql.statements.core.ReadyCQLStatement;

/**
 * In internal exception type that is used to saverow exception
 * context from within a CQL activity cycle.
 */
public class CQLCycleWithStatementException extends Exception {

    private final long cycleValue;
    private final long durationNanos;
    private final ReadyCQLStatement readyCQLStatement;

    public CQLCycleWithStatementException(long cycleValue, long durationNanos, Throwable e, ReadyCQLStatement readyCQLStatement) {
        super(e);
        this.cycleValue = cycleValue;
        this.durationNanos = durationNanos;
        this.readyCQLStatement = readyCQLStatement;
    }

    public long getCycleValue() {
        return cycleValue;
    }

    public long getDurationNanos() {
        return durationNanos;
    }

    public ReadyCQLStatement getReadyCQLStatement() {
        return readyCQLStatement;
    }

    public String getStatement() {
        return readyCQLStatement.getQueryString(cycleValue);
    }

}
