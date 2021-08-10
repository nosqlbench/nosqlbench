package io.nosqlbench.adapter.cqld4.exceptions;

public abstract class CqlGenericCycleException extends RuntimeException {

    public CqlGenericCycleException(Throwable cause) {
        super(cause);
    }

    public CqlGenericCycleException(String message) {
        super(message);
    }

    public CqlGenericCycleException(String message, Throwable cause) {
        super(message, cause);
    }

}
