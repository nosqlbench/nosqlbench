package io.nosqlbench.driver.mongodb;

import io.nosqlbench.engine.api.activityapi.core.Action;
import io.nosqlbench.engine.api.activityapi.core.ActionDispenser;
import io.nosqlbench.engine.api.activityapi.core.ActivityType;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.nb.annotations.Service;

@Service(ActivityType.class)
public class MongoActivityType implements ActivityType<MongoActivity> {

    @Override
    public String getName() {
        return "mongodb";
    }

    @Override
    public MongoActivity getActivity(ActivityDef activityDef) {
        return new MongoActivity(activityDef);
    }

    @Override
    public ActionDispenser getActionDispenser(MongoActivity activity) {
        return new MongoActionDispenser(activity);
    }

    private static class MongoActionDispenser implements ActionDispenser {

        private final MongoActivity activity;

        public MongoActionDispenser(MongoActivity activity) {
            this.activity = activity;
        }

        @Override
        public Action getAction(int slot) {
            return new MongoAction(activity, slot);
        }
    }
}
