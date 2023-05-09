package io.nosqlbench.adapter.pinecone.opdispensers;

import io.nosqlbench.adapter.pinecone.PineconeDriverAdapter;
import io.nosqlbench.adapter.pinecone.PineconeSpace;
import io.nosqlbench.adapter.pinecone.ops.PineconeOp;
import io.nosqlbench.adapter.pinecone.ops.PineconeUpdateOp;
import io.nosqlbench.engine.api.templating.ParsedOp;
import io.pinecone.PineconeConnection;
import io.pinecone.proto.UpdateRequest;

import java.util.function.LongFunction;

public class PineconeUpdateOpDispenser extends PineconeOpDispenser {
    private UpdateRequest request;
    private PineconeConnection connection;

    public PineconeUpdateOpDispenser(PineconeDriverAdapter adapter,
                                     ParsedOp op,
                                     LongFunction<PineconeSpace> pcFunction,
                                     LongFunction<String> targetFunction) {
        super(adapter, op, pcFunction, targetFunction);

        String indexName = op.getStaticValue("update");
        connection = pcFunction.apply(0).getConnection(indexName);
        request = createUpdateRequest();
    }

    private UpdateRequest createUpdateRequest() {
        return UpdateRequest.newBuilder().build();
    }

    @Override
    public PineconeOp apply(long value) {
        return new PineconeUpdateOp(connection, request);
    }
}
