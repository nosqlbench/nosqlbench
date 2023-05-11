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
    private final LongFunction<UpdateRequest> updateRequestFunc;

    public PineconeUpdateOpDispenser(PineconeDriverAdapter adapter,
                                     ParsedOp op,
                                     LongFunction<PineconeSpace> pcFunction,
                                     LongFunction<String> targetFunction) {
        super(adapter, op, pcFunction, targetFunction);

        indexNameFunc = op.getAsRequiredFunction("update", String.class);
        updateRequestFunc = createUpdateRequestFunction(op);
    }

    private LongFunction<UpdateRequest> createUpdateRequestFunction(ParsedOp op) {
        LongFunction<UpdateRequest.Builder> rFunc = l -> UpdateRequest.newBuilder();
        return l -> rFunc.apply(l).build();
    }

    @Override
    public PineconeOp apply(long value) {
        return new PineconeUpdateOp(pcFunction.apply(value).getConnection(indexNameFunc.apply(value)),
            updateRequestFunc.apply(value));
    }
}
