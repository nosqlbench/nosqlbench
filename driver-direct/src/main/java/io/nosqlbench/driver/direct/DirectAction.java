package io.nosqlbench.driver.direct;

import io.nosqlbench.engine.api.activityapi.core.SyncAction;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;

public class DirectAction implements SyncAction {
    private final int slot;
    private final DirectActivity activity;

    public DirectAction(int slot, DirectActivity activity) {
        this.slot = slot;
        this.activity = activity;
    }


    @Override
    public int runCycle(long cycle) {

        OpDispenser<? extends Runnable> dispenser = activity.getSequencer().apply(cycle);
        Runnable apply = dispenser.apply(cycle);


        return SyncAction.super.runCycle(cycle);
    }
}
