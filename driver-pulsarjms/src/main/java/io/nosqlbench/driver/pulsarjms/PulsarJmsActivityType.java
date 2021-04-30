package io.nosqlbench.driver.pulsarjms;

import io.nosqlbench.engine.api.activityapi.core.Action;
import io.nosqlbench.engine.api.activityapi.core.ActionDispenser;
import io.nosqlbench.engine.api.activityapi.core.ActivityType;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.nb.annotations.Service;

@Service(value = ActivityType.class, selector = "pulsarjms")
public class PulsarJmsActivityType implements ActivityType<PulsarJmsActivity> {
    @Override
    public ActionDispenser getActionDispenser(PulsarJmsActivity activity) {
        return new PulsarJmsActionDispenser(activity);
    }

    @Override
    public PulsarJmsActivity getActivity(ActivityDef activityDef) {
        return new PulsarJmsActivity(activityDef);
    }

    private static class PulsarJmsActionDispenser implements ActionDispenser {
        private final PulsarJmsActivity activity;
        public PulsarJmsActionDispenser(PulsarJmsActivity activity) {
            this.activity = activity;
        }

        @Override
        public Action getAction(int slot) {
            return new PulsarJmsAction(activity, slot);
        }
    }
}
