package io.nosqlbench.driver.pularjms;

import io.nosqlbench.engine.api.activityapi.core.ActionDispenser;
import io.nosqlbench.engine.api.activityapi.core.ActivityType;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.nb.annotations.Service;

@Service(value = ActivityType.class, selector = "pulsar_jms")
public class PulsarJmsActivityType implements ActivityType<PulsarJmsActivity> {

    @Override
    public PulsarJmsActivity getActivity(ActivityDef activityDef) {
        return new PulsarJmsActivity(activityDef);
    }

    @Override
    public ActionDispenser getActionDispenser(PulsarJmsActivity activity) {
        return new PulsarJmsActionDispenser(activity);
    }
}
