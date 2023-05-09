package io.nosqlbench.adapter.pinecone.opdispensers;

import io.nosqlbench.adapter.pinecone.PineconeDriverAdapter;
import io.nosqlbench.adapter.pinecone.PineconeSpace;
import io.nosqlbench.adapter.pinecone.ops.PineconeFetchOp;
import io.nosqlbench.adapter.pinecone.ops.PineconeOp;
import io.nosqlbench.engine.api.templating.ParsedOp;
import io.pinecone.PineconeConnection;
import io.pinecone.proto.FetchRequest;

import java.util.Arrays;
import java.util.List;
import java.util.function.LongFunction;

public class PineconeFetchOpDispenser extends PineconeOpDispenser {
    private FetchRequest request;
    private PineconeConnection connection;

    public PineconeFetchOpDispenser(PineconeDriverAdapter adapter,
                                    ParsedOp op,
                                    LongFunction<PineconeSpace> pcFunction,
                                    LongFunction<String> targetFunction) {
        super(adapter, op, pcFunction, targetFunction);

        String indexName = op.getStaticValue("fetch");
        connection = pcFunction.apply(0).getConnection(indexName);
        request = createFetchRequest();
    }

    private FetchRequest createFetchRequest() {
        List<String> ids = Arrays.asList("v1","v2");
        return FetchRequest.newBuilder().addAllIds(ids).setNamespace("default-namespace").build();
    }

    @Override
    public PineconeOp apply(long value) {
        return new PineconeFetchOp(connection, request);
    }
}
