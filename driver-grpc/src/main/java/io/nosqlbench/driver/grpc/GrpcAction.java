package io.nosqlbench.driver.grpc;

import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.engine.api.activityapi.planning.OpSource;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.activityimpl.uniform.StandardAction;

public class GrpcAction extends StandardAction<GrpcActivity,GrpcOp> {

    private final int slot;
    private final GrpcActivity activity;
    private OpSource<GrpcOp> sequencer;

    public GrpcAction(int slot, GrpcActivity activity, OpSequence<OpDispenser<GrpcOp>> opsource) {
        super(activity, opsource);
        this.slot = slot;
        this.activity = activity;
    }

    @Override
    public void init() {
        this.sequencer = activity.getOpsource();
    }

}
