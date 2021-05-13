package io.nosqlbench.driver.grpc;

import io.nosqlbench.driver.grpc.optypes.NullGrpcOp;
import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;

public class GrpcOpMapper implements OpDispenser<GrpcOp> {

    private final OpTemplate opTemplate;

    public GrpcOpMapper(OpTemplate opTemplate) {
        this.opTemplate = opTemplate;
    }

    @Override
    public GrpcOp apply(long value) {
        return new NullGrpcOp();
    }
}
