package io.nosqlbench.activitytype.cql.errorhandling.exceptions;

public abstract class CqlGenericCycleException extends RuntimeException {

    private long cycle;

    public CqlGenericCycleException(long cycle, Throwable cause) {
        super(cause);
        this.cycle = cycle;
    }

    public CqlGenericCycleException(long cycle, String message) {
        super(message);
        this.cycle = cycle;
    }

    public CqlGenericCycleException(long cycle, String message, Throwable cause) {
        super(message, cause);
        this.cycle = cycle;
    }

    public CqlGenericCycleException(long cycle) {
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
