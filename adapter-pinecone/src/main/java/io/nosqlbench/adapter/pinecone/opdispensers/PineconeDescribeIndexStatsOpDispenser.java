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
    private final LongFunction<DescribeIndexStatsRequest> indexStatsRequestFunc;

    public PineconeDescribeIndexStatsOpDispenser(PineconeDriverAdapter adapter,
                                                 ParsedOp op,
                                                 LongFunction<PineconeSpace> pcFunction,
                                                 LongFunction<String> targetFunction) {
        super(adapter, op, pcFunction, targetFunction);
        indexNameFunc = op.getAsRequiredFunction("indexStatsRequest", String.class);
        indexStatsRequestFunc = createDescribeIndexStatsRequestFunction(op);
    }

    private LongFunction<DescribeIndexStatsRequest> createDescribeIndexStatsRequestFunction(ParsedOp op) {
        LongFunction<DescribeIndexStatsRequest.Builder> rFunc = l -> DescribeIndexStatsRequest.newBuilder();
        return l -> rFunc.apply(l).build();
    }

    @Override
    public PineconeOp apply(long value) {
        return new PineconeDescribeIndexStatsOp(pcFunction.apply(value).getConnection(indexNameFunc.apply(value)),
            indexStatsRequestFunc.apply(value));
    }
}
