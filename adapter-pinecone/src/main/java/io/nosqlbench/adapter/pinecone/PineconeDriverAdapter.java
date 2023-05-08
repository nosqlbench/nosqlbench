package io.nosqlbench.adapter.pinecone;

import io.nosqlbench.adapter.pinecone.ops.PineconeOp;
import io.nosqlbench.engine.api.activityimpl.OpMapper;
import io.nosqlbench.engine.api.activityimpl.uniform.BaseDriverAdapter;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.nb.annotations.Service;

@Service(value = DriverAdapter.class, selector = "pinecone")
public class PineconeDriverAdapter extends BaseDriverAdapter<PineconeOp, PineconeSpace> {
    @Override
    public OpMapper<PineconeOp> getOpMapper() {
        return null;
    }
}
