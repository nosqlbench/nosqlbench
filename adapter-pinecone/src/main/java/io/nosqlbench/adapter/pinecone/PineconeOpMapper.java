package io.nosqlbench.adapter.pinecone;

import io.nosqlbench.adapter.pinecone.ops.PineconeOp;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.activityimpl.OpMapper;
import io.nosqlbench.engine.api.templating.ParsedOp;

public class PineconeOpMapper implements OpMapper<PineconeOp> {
    @Override
    public OpDispenser<? extends PineconeOp> apply(ParsedOp op) {
        return null;
    }
}
