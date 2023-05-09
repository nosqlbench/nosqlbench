package io.nosqlbench.adapter.pinecone.opdispensers;

import io.nosqlbench.adapter.pinecone.PineconeDriverAdapter;
import io.nosqlbench.adapter.pinecone.PineconeSpace;
import io.nosqlbench.adapter.pinecone.ops.PineconeDeleteOp;
import io.nosqlbench.adapter.pinecone.ops.PineconeOp;
import io.nosqlbench.engine.api.templating.ParsedOp;
import io.pinecone.PineconeConnection;
import io.pinecone.proto.DeleteRequest;

import java.util.Arrays;
import java.util.function.LongFunction;

public class PineconeDeleteOpDispenser extends PineconeOpDispenser {
    private DeleteRequest request;
    private PineconeConnection connection;

    public PineconeDeleteOpDispenser(PineconeDriverAdapter adapter,
                                     ParsedOp op,
                                     LongFunction<PineconeSpace> pcFunction,
                                     LongFunction<String> targetFunction) {
        super(adapter, op, pcFunction, targetFunction);

        String indexName = op.getStaticValue("delete");
        connection = pcFunction.apply(0).getConnection(indexName);
        request = createDeleteRequest();
    }

    @Override
    public PineconeOp apply(long value) {
        return new PineconeDeleteOp(connection, request);
    }

    private DeleteRequest createDeleteRequest() {
        // TODO: How do I pull these from the ParsedOp?
        String[] idsToDelete = {"v2"};
        String namespace = "ns";

        return DeleteRequest.newBuilder()
            .setNamespace(namespace)
            .addAllIds(Arrays.asList(idsToDelete))
            .setDeleteAll(false)
            .build();
    }


}
