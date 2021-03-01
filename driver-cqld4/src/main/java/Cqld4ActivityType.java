import io.nosqlbench.engine.api.activityapi.core.Action;
import io.nosqlbench.engine.api.activityapi.core.ActionDispenser;
import io.nosqlbench.engine.api.activityapi.core.ActivityType;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.nb.annotations.Service;

/**
 * A new driver which use the Apache Cassandra CQL driver version 4.
 * To differentiate code in this module from the initial CQL driver,
 * all classes are prefixed with Cqld4. Full docs are in the cqld4.md file.
 */
@Service(value=ActivityType.class,selector = "cqld4")
public class Cqld4ActivityType implements ActivityType<Cqld4Activity> {

    @Override
    public Cqld4Activity getActivity(ActivityDef activityDef) {
        return new Cqld4Activity(activityDef);
    }

    @Override
    public ActionDispenser getActionDispenser(Cqld4Activity activity) {
        if (activity.getParams().getOptionalString("async").isPresent()) {
            throw new RuntimeException("This driver does not support async mode yet.");
        }
        return new Cqld4ActionDispenser(activity);
    }

    private final static class Cqld4ActionDispenser implements ActionDispenser {

        private final Cqld4Activity activity;

        public Cqld4ActionDispenser(Cqld4Activity activity) {
            this.activity = activity;
        }

        @Override
        public Action getAction(int slot) {
            return new Cqld4Action(slot, activity);
        }
    }

}
