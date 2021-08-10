package io.nosqlbench.adapter.cqld4;

public class UndefinedResultSetException extends RuntimeException {
    private final Cqld4Op cqld4op;

    public UndefinedResultSetException(Cqld4Op cqld4Op) {
        this.cqld4op = cqld4Op;
    }

    @Override
    public String getMessage() {
        return "Attempted to access a result set which was not defined in op " + cqld4op.toString();
    }
}
