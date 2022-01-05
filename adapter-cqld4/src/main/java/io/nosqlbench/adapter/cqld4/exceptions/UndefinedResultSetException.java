package io.nosqlbench.adapter.cqld4.exceptions;

import io.nosqlbench.adapter.cqld4.optypes.Cqld4CqlOp;

public class UndefinedResultSetException extends RuntimeException {
    private final Cqld4CqlOp cqld4op;

    public UndefinedResultSetException(Cqld4CqlOp cqld4Op) {
        this.cqld4op = cqld4Op;
    }

    @Override
    public String getMessage() {
        return "Attempted to access a result set which was not defined in op " + cqld4op.toString();
    }
}
