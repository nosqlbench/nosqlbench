package io.nosqlbench.driver.pulsar;

import io.nosqlbench.engine.api.activityapi.core.Action;
import io.nosqlbench.engine.api.activityapi.core.ActionDispenser;
import io.nosqlbench.engine.api.activityapi.core.ActivityType;

public class PulsarActivityType implements ActivityType<PulsarActivity> {

    @Override
    public String getName() {
        return "pulsar";
    }

    @Override
    public ActionDispenser getActionDispenser(PulsarActivity activity) {
        if (activity.getParams().getOptionalString("async").isPresent()) {
            throw new RuntimeException("The async pulsar driver is not online yet.");
        }
        return new PulsarActionDispenser(activity);
    }

    private static class PulsarActionDispenser implements ActionDispenser {

        private final PulsarActivity activity;

        public PulsarActionDispenser(PulsarActivity activity) {
            this.activity = activity;
        }

        @Override
        public Action getAction(int slot) {
            return new PulsarAction(activity, slot);
        }
    }
}
