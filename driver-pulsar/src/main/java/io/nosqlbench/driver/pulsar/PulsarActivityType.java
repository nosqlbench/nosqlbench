package io.nosqlbench.driver.pulsar;

import io.nosqlbench.engine.api.activityapi.core.Action;
import io.nosqlbench.engine.api.activityapi.core.ActionDispenser;
import io.nosqlbench.engine.api.activityapi.core.ActivityType;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.nb.annotations.Service;

@Service(value = ActivityType.class, selector = "pulsar")
public class PulsarActivityType implements ActivityType<PulsarActivity> {

    @Override
    public ActionDispenser getActionDispenser(PulsarActivity activity) {
        if (activity.getParams().getOptionalString("async").isPresent()) {
            throw new RuntimeException("The async pulsar driver is not implemented yet.");
        }
        return new PulsarActionDispenser(activity);
    }

    @Override
    public PulsarActivity getActivity(ActivityDef activityDef) {
        return new PulsarActivity(activityDef);
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
