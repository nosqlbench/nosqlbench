package io.nosqlbench.activitytype.cql.errorhandling.exceptions;

public abstract class CqlCycleException extends RuntimeException {

    private long cycle;

    public CqlCycleException(long cycle, Throwable cause) {
        super(cause);
        this.cycle = cycle;
    }

    public CqlCycleException(long cycle, String message) {
        super(message);
        this.cycle = cycle;
    }

    public CqlCycleException(long cycle, String message, Throwable cause) {
        super(message, cause);
        this.cycle = cycle;
    }

    public CqlCycleException(long cycle) {
        super();
        this.cycle = cycle;
    }

    @Override
    public String getMessage() {
        return "cycle:" + cycle + " caused by:" + super.getMessage();
    }

    public long getCycle() {
        return cycle;
    }



}
