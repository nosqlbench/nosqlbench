package io.nosqlbench.driver.direct;

import io.nosqlbench.engine.api.activityapi.core.Action;
import io.nosqlbench.engine.api.activityapi.core.ActionDispenser;
import io.nosqlbench.engine.api.activityapi.core.ActivityType;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.nb.annotations.Service;

/**
 * This activity type driver allows you to dynamically map any available
 * Java API which is exposed to the NoSQLBench runtime, executing methods
 * on this API by name, (optionally) storing named results, and re-using
 * these named results as arguments to subsequent calls.
 *
 * It supports static method dispatch, instance methods, and per-thread
 * object scoping.
 */
@Service(value = ActivityType.class,selector = "direct")
public class DirectActivityType implements ActivityType<DirectActivity> {

    @Override
    public DirectActivity getActivity(ActivityDef activityDef) {
        return new DirectActivity(activityDef);
    }

    @Override
    public ActionDispenser getActionDispenser(DirectActivity activity) {
        return new DirectActionDispenser(activity);
    }

    private static class DirectActionDispenser implements ActionDispenser {

        private final DirectActivity activity;

        public DirectActionDispenser(DirectActivity activity) {
            this.activity = activity;
        }

        @Override
        public Action getAction(int slot) {
            return new DirectAction(slot, activity);
        }
    }
}
