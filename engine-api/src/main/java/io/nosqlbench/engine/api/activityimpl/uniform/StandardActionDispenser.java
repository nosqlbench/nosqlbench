package io.nosqlbench.engine.api.activityimpl.uniform;

import io.nosqlbench.engine.api.activityapi.core.ActionDispenser;
import io.nosqlbench.engine.api.activityapi.core.Activity;

public class StandardActionDispenser implements ActionDispenser {
    private final StandardActivity<?,?> activity;

    public <A extends Activity> StandardActionDispenser(StandardActivity<?,?> activity) {
        this.activity = activity;
    }

    @Override
    public StandardAction<?,?> getAction(int slot) {
        return new StandardAction<>(activity,slot);
    }
}
