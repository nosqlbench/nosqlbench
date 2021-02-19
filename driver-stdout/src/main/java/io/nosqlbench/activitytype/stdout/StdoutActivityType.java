package io.nosqlbench.activitytype.stdout;

import io.nosqlbench.engine.api.activityapi.core.Action;
import io.nosqlbench.engine.api.activityapi.core.ActionDispenser;
import io.nosqlbench.engine.api.activityapi.core.ActivityType;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.nb.annotations.Service;

import java.util.Optional;

@Service(value = ActivityType.class, selector = "stdout")
public class StdoutActivityType implements ActivityType<StdoutActivity> {

    @Override
    public StdoutActivity getActivity(ActivityDef activityDef) {
        Optional<String> yaml = activityDef.getParams().getOptionalString("yaml", "workload");


        // sanity check that we have a yaml parameter, which contains our statements and bindings
        if (!yaml.isPresent()) {
            throw new RuntimeException("Currently, the stdout activity type requires yaml/workload activity parameter" +
                ".");
        }

        return new StdoutActivity(activityDef);
    }

    @Override
    public ActionDispenser getActionDispenser(StdoutActivity activity) {
        return new StdoutActionDispenser(activity);
    }

    private static class StdoutActionDispenser implements ActionDispenser {

        private final StdoutActivity activity;

        private StdoutActionDispenser(StdoutActivity activity) {
            this.activity = activity;
        }

        @Override
        public Action getAction(int slot) {
            if (activity.getActivityDef().getParams().getOptionalString("async").isPresent()) {
                return new AsyncStdoutAction(slot, activity);
            }
            return new StdoutAction(slot, activity);
        }
    }
}
