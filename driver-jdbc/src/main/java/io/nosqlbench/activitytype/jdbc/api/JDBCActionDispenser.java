package io.nosqlbench.activitytype.jdbc.api;

import io.nosqlbench.activitytype.jdbc.impl.JDBCAction;
import io.nosqlbench.engine.api.activityapi.core.Action;
import io.nosqlbench.engine.api.activityapi.core.ActionDispenser;

public class JDBCActionDispenser implements ActionDispenser {
    private final JDBCActivity activity;

    public JDBCActionDispenser(JDBCActivity a) {
        activity = a;
    }

    @Override
    public Action getAction(int slot) {
        return new JDBCAction(activity, slot);
    }
}
