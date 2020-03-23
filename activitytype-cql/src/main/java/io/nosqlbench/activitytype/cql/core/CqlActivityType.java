package io.nosqlbench.activitytype.cql.core;


import io.nosqlbench.engine.api.activityapi.core.ActionDispenser;
import io.nosqlbench.engine.api.activityapi.core.ActivityType;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.virtdata.annotations.Service;

import java.util.Optional;

@Service(ActivityType.class)
public class CqlActivityType implements ActivityType<CqlActivity> {

    public String getName() {
        return "cql";
    }

    @Override
    public CqlActivity getActivity(ActivityDef activityDef) {

        Optional<String> yaml = activityDef.getParams().getOptionalString("yaml", "workload");

        // sanity check that we have a yaml parameter, which contains our statements and bindings
        if (yaml.isEmpty()) {
            throw new RuntimeException("Currently, the cql activity type requires yaml/workload activity parameter.");
        }

        // allow shortcut: yaml parameter provide the default alias name
        if (activityDef.getAlias().equals(ActivityDef.DEFAULT_ALIAS)) {
            activityDef.getParams().set("alias",yaml.get());
        }

        return new CqlActivity(activityDef);
    }

    /**
     * Returns the per-activity level dispenser. The ActionDispenser can then dispense
     * per-thread actions within the activity instance.
     * @param activity The activity instance which will parameterize this action
     */
    @Override
    public ActionDispenser getActionDispenser(CqlActivity activity) {
        return new CqlActionDispenser(activity);
    }

}
