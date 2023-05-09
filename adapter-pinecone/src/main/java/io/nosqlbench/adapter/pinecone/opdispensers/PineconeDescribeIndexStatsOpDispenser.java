package io.nosqlbench.adapter.pinecone.opdispensers;

import io.nosqlbench.adapter.pinecone.PineconeDriverAdapter;
import io.nosqlbench.adapter.pinecone.PineconeSpace;
import io.nosqlbench.adapter.pinecone.ops.PineconeDescribeIndexStatsOp;
import io.nosqlbench.adapter.pinecone.ops.PineconeOp;
import io.nosqlbench.engine.api.templating.ParsedOp;
import io.pinecone.PineconeConnection;
import io.pinecone.proto.DescribeIndexStatsRequest;

import java.util.function.LongFunction;

public class PineconeDescribeIndexStatsOpDispenser extends PineconeOpDispenser {
    private DescribeIndexStatsRequest request;
    private PineconeConnection connection;

    public PineconeDescribeIndexStatsOpDispenser(PineconeDriverAdapter adapter,
                                                 ParsedOp op,
                                                 LongFunction<PineconeSpace> pcFunction,
                                                 LongFunction<String> targetFunction) {
        super(adapter, op, pcFunction, targetFunction);

        String indexName = op.getStaticValue("describeIndexStats");
        connection = pcFunction.apply(0).getConnection(indexName);
        request = createDescribeIndexStatsRequest();
    }

    private DescribeIndexStatsRequest createDescribeIndexStatsRequest() {
        return DescribeIndexStatsRequest.newBuilder().build();
    }

    @Override
    public PineconeOp apply(long value) {
        return new PineconeDescribeIndexStatsOp(connection, request);
    }
}
