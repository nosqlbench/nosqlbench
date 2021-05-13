package io.nosqlbench.driver.jms;

import io.nosqlbench.engine.api.activityapi.core.Action;
import io.nosqlbench.engine.api.activityapi.core.ActionDispenser;
import io.nosqlbench.engine.api.activityapi.core.ActivityType;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.nb.annotations.Service;

@Service(value = ActivityType.class, selector = "jms")
public class JmsActivityType implements ActivityType<JmsActivity> {
    @Override
    public ActionDispenser getActionDispenser(JmsActivity activity) {
        return new PulsarJmsActionDispenser(activity);
    }

    @Override
    public JmsActivity getActivity(ActivityDef activityDef) {
        return new JmsActivity(activityDef);
    }

    private static class PulsarJmsActionDispenser implements ActionDispenser {
        private final JmsActivity activity;
        public PulsarJmsActionDispenser(JmsActivity activity) {
            this.activity = activity;
        }

        @Override
        public Action getAction(int slot) {
            return new JmsAction(activity, slot);
        }
    }
}
