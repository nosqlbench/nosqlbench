package io.nosqlbench.driver.jmx;

import io.nosqlbench.engine.api.activityapi.core.ActionDispenser;
import io.nosqlbench.engine.api.activityapi.core.ActivityType;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.nb.annotations.Service;

@Service(value = ActivityType.class, selector = "jmx")
public class JMXActivityType implements ActivityType<JMXActivity> {

    @Override
    public JMXActivity getActivity(ActivityDef activityDef) {
        return new JMXActivity(activityDef);
    }

    @Override
    public ActionDispenser getActionDispenser(JMXActivity activity) {
        return new JMXActionDispenser(activity);
    }
}
