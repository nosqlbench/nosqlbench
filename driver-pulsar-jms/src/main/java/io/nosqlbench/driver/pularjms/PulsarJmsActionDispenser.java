package io.nosqlbench.driver.pularjms;

import io.nosqlbench.engine.api.activityapi.core.Action;
import io.nosqlbench.engine.api.activityapi.core.ActionDispenser;

public class PulsarJmsActionDispenser implements ActionDispenser {
    private final PulsarJmsActivity activity;

    public PulsarJmsActionDispenser(PulsarJmsActivity activity) {
        this.activity = activity;
    }

    @Override
    public Action getAction(int slot) {
        return new PulsarJmsAction(activity.getActivityDef(),slot,activity);
    }
}
