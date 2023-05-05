package io.nosqlbench.adapter.pinecone.ops;

import io.nosqlbench.engine.api.activityimpl.uniform.flowtypes.RunnableOp;
import io.pinecone.PineconeConnection;

public abstract class PineconeOp implements RunnableOp {
    protected final PineconeConnection connection;

    public PineconeOp(PineconeConnection connection) {
        this.connection = connection;
    }

}
