package io.nosqlbench.adapter.pinecone.opdispensers;

import com.google.common.primitives.Floats;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.nosqlbench.adapter.pinecone.PineconeDriverAdapter;
import io.nosqlbench.adapter.pinecone.PineconeSpace;
import io.nosqlbench.adapter.pinecone.ops.PineconeOp;
import io.nosqlbench.adapter.pinecone.ops.PineconeQueryOp;
import io.nosqlbench.engine.api.templating.ParsedOp;
import io.pinecone.PineconeConnection;
import io.pinecone.proto.QueryRequest;
import io.pinecone.proto.QueryVector;

import java.util.function.LongFunction;

public class PineconeQueryOpDispenser extends PineconeOpDispenser {
    private QueryRequest request;
    private PineconeConnection connection;

    public PineconeQueryOpDispenser(PineconeDriverAdapter adapter,
                                    ParsedOp op,
                                    LongFunction<PineconeSpace> pcFunction,
                                    LongFunction<String> targetFunction) {
        super(adapter, op, pcFunction, targetFunction);

        String indexName = op.getStaticValue("query");
        connection = pcFunction.apply(0).getConnection(indexName);
        request = createQueryRequest();
    }

    private QueryRequest createQueryRequest() {
        float[] rawVector = {1.0F, 2.0F, 3.0F};
        QueryVector queryVector = QueryVector.newBuilder()
            .addAllValues(Floats.asList(rawVector))
            .setFilter(Struct.newBuilder()
                .putFields("some_field", Value.newBuilder()
                    .setStructValue(Struct.newBuilder()
                        .putFields("$lt", Value.newBuilder()
                            .setNumberValue(3)
                            .build()))
                    .build())
                .build())
            .setNamespace("default-namespace")
            .build();

        return QueryRequest.newBuilder()
            .addQueries(queryVector)
            .setNamespace("default-namespace")
            .setTopK(2)
            .setIncludeMetadata(true)
            .build();
    }

    @Override
    public PineconeOp apply(long value) {
        return new PineconeQueryOp(connection, request);
    }
}
