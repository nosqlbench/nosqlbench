package io.nosqlbench.driver.grpc;

import com.codahale.metrics.Timer;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.activityimpl.uniform.StandardAction;

public class GrpcAction extends StandardAction {

    private final int slot;
    private final GrpcActivity activity;
    private OpSequence<OpDispenser<GrpcOp>> sequencer;

    public GrpcAction(int slot, GrpcActivity activity, OpSequence opsource) {
        super(activity, opsource);
        this.slot = slot;
        this.activity = activity;
    }

    @Override
    public void init() {
        this.sequencer = activity.getSequencer();
    }


    @Override
    public int runCycle(long cycle) {

        GrpcOp op = null;
        try (Timer.Context ctx = activity.getInstrumentation().getOrCreateBindTimer().time()) {

            // Get the template instance from the sequence
            OpDispenser<GrpcOp> opDispenser = sequencer.apply(cycle);

            // Get an executable op from the template instance
            op = opDispenser.apply(cycle);
        }

        int tries = activity.getMaxTries();


        op.run();

        return 0;
    }
}
