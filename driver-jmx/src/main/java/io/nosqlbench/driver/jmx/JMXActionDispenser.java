package io.nosqlbench.driver.jmx;

import io.nosqlbench.engine.api.activityapi.core.Action;
import io.nosqlbench.engine.api.activityapi.core.ActionDispenser;

public class JMXActionDispenser implements ActionDispenser {
    private final JMXActivity activity;

    public JMXActionDispenser(JMXActivity activity) {
        this.activity = activity;
    }

    @Override
    public Action getAction(int slot) {
        return new JMXAction(activity.getActivityDef(),slot,activity);
    }
}
